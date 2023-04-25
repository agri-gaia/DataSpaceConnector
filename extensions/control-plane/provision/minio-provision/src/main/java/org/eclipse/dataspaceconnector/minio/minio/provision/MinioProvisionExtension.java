/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
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

package org.eclipse.dataspaceconnector.minio.minio.provision;

import dev.failsafe.RetryPolicy;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioClientProvider;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioTemporarySecretToken;
import org.eclipse.dataspaceconnector.spi.EdcSetting;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.provision.ProvisionManager;
import org.eclipse.dataspaceconnector.spi.transfer.provision.ResourceManifestGenerator;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.StatusCheckerRegistry;

/**
 * Provides data transfer {@link org.eclipse.dataspaceconnector.spi.transfer.provision.Provisioner}s backed by AWS services.
 */
public class MinioProvisionExtension implements ServiceExtension {

    @EdcSetting
    private static final String PROVISION_MAX_RETRY = "edc.aws.provision.retry.retries.max";

    @EdcSetting
    private static final String PROVISION_MAX_ROLE_SESSION_DURATION = "edc.aws.provision.role.duration.session.max";

    @Inject
    private Vault vault;
    @Inject
    private Monitor monitor;
    @Inject
    private MinioClientProvider clientProvider;

    @Override
    public String name() {
        return "AWS Provision";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();

        var provisionManager = context.getService(ProvisionManager.class);

        @SuppressWarnings("unchecked") var retryPolicy = (RetryPolicy<Object>) context.getService(RetryPolicy.class);

        int maxRetries = context.getSetting(PROVISION_MAX_RETRY, 10);
        int roleMaxSessionDuration = context.getSetting(PROVISION_MAX_ROLE_SESSION_DURATION, 3600);
        var provisionerConfiguration = new MinioBucketProvisionerConfiguration(maxRetries, roleMaxSessionDuration);
        var s3BucketProvisioner = new MinioBucketProvisioner(clientProvider, monitor, retryPolicy, provisionerConfiguration, vault);
        provisionManager.register(s3BucketProvisioner);

        // register the generator
        var manifestGenerator = context.getService(ResourceManifestGenerator.class);
        manifestGenerator.registerGenerator(new MinioConsumerResourceDefinitionGenerator());

        var statusCheckerReg = context.getService(StatusCheckerRegistry.class);
        statusCheckerReg.register(MinioBucketSchema.TYPE, new MinioStatusChecker(clientProvider, retryPolicy));

        registerTypes(context.getTypeManager());
    }

    @Override
    public void shutdown() {
        try {
            clientProvider.shutdown();
        } catch (Exception e) {
            monitor.severe("Error closing S3 client provider", e);
        }
    }

    private void registerTypes(TypeManager typeManager) {
        typeManager.registerTypes(MinioBucketProvisionedResource.class, MinioBucketResourceDefinition.class, MinioTemporarySecretToken.class);
    }


}


