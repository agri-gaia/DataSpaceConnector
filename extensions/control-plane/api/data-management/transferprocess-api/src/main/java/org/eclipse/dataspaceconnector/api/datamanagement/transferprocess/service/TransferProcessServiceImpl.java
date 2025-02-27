/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.api.datamanagement.transferprocess.service;

import org.eclipse.dataspaceconnector.api.result.ServiceResult;
import org.eclipse.dataspaceconnector.spi.query.QuerySpec;
import org.eclipse.dataspaceconnector.spi.query.QueryValidator;
import org.eclipse.dataspaceconnector.spi.result.AbstractResult;
import org.eclipse.dataspaceconnector.spi.transaction.TransactionContext;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ProvisionedContentResource;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ProvisionedDataAddressResource;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ProvisionedDataDestinationResource;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ProvisionedResource;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.command.CancelTransferCommand;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.command.DeprovisionRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;

public class TransferProcessServiceImpl implements TransferProcessService {
    private final TransferProcessStore transferProcessStore;
    private final TransferProcessManager manager;
    private final TransactionContext transactionContext;
    private final QueryValidator queryValidator;

    public TransferProcessServiceImpl(TransferProcessStore transferProcessStore, TransferProcessManager manager, TransactionContext transactionContext) {
        this.transferProcessStore = transferProcessStore;
        this.manager = manager;
        this.transactionContext = transactionContext;
        queryValidator = new QueryValidator(TransferProcess.class, getSubtypes());
    }

    @Override
    public @Nullable TransferProcess findById(String transferProcessId) {
        return transactionContext.execute(() -> transferProcessStore.find(transferProcessId));
    }

    @Override
    public ServiceResult<Stream<TransferProcess>> query(QuerySpec query) {
        var result = queryValidator.validate(query);

        if (result.failed()) {
            return ServiceResult.badRequest(format("Error validating schema: %s", result.getFailureDetail()));
        }
        return ServiceResult.success(transactionContext.execute(() -> transferProcessStore.findAll(query)));
    }

    @Override
    public @Nullable String getState(String transferProcessId) {
        return transactionContext.execute(() -> {
            var process = transferProcessStore.find(transferProcessId);
            return Optional.ofNullable(process).map(p -> TransferProcessStates.from(p.getState()).name()).orElse(null);
        });
    }

    @Override
    public @NotNull ServiceResult<TransferProcess> cancel(String transferProcessId) {
        return apply(transferProcessId, this::cancelImpl);
    }

    @Override
    public @NotNull ServiceResult<TransferProcess> deprovision(String transferProcessId) {
        return apply(transferProcessId, this::deprovisionImpl);
    }

    @Override
    public @NotNull ServiceResult<String> initiateTransfer(DataRequest request) {
        return transactionContext.execute(() -> {
            var transferInitiateResult = manager.initiateConsumerRequest(request);
            return Optional.ofNullable(transferInitiateResult)
                    .filter(AbstractResult::succeeded)
                    .map(AbstractResult::getContent)
                    .map(ServiceResult::success)
                    .orElse(ServiceResult.conflict("Request couldn't be initialised."));
        });
    }

    private Map<Class<?>, List<Class<?>>> getSubtypes() {
        return Map.of(
                ProvisionedResource.class, List.of(ProvisionedDataAddressResource.class),
                ProvisionedDataAddressResource.class, List.of(ProvisionedDataDestinationResource.class, ProvisionedContentResource.class)
        );
    }

    private ServiceResult<TransferProcess> apply(String transferProcessId, Function<TransferProcess, ServiceResult<TransferProcess>> function) {
        return transactionContext.execute(() -> {
            var transferProcess = transferProcessStore.find(transferProcessId);
            return Optional.ofNullable(transferProcess)
                    .map(function)
                    .orElse(ServiceResult.notFound(format("TransferProcess %s does not exist", transferProcessId)));
        });
    }

    private ServiceResult<TransferProcess> cancelImpl(TransferProcess transferProcess) {
        // Attempt the transition only to verify that the transition is allowed.
        // The updated transfer process is not persisted at this point, and is discarded.
        try {
            transferProcess.transitionCancelled();
        } catch (IllegalStateException e) {
            return ServiceResult.conflict(format("TransferProcess %s cannot be canceled as it is in state %s", transferProcess.getId(), TransferProcessStates.from(transferProcess.getState())));
        }

        manager.enqueueCommand(new CancelTransferCommand(transferProcess.getId()));

        return ServiceResult.success(transferProcess);
    }

    private ServiceResult<TransferProcess> deprovisionImpl(TransferProcess transferProcess) {
        // Attempt the transition only to verify that the transition is allowed.
        // The updated transfer process is not persisted at this point, and is discarded.
        try {
            transferProcess.transitionDeprovisioning();
        } catch (IllegalStateException e) {
            return ServiceResult.conflict(format("TransferProcess %s cannot be deprovisioned as it is in state %s", transferProcess.getId(), TransferProcessStates.from(transferProcess.getState())));
        }

        manager.enqueueCommand(new DeprovisionRequest(transferProcess.getId()));

        return ServiceResult.success(transferProcess);
    }
}
