/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 */

package org.eclipse.dataspaceconnector.minio.dataplane.minio;

import org.eclipse.dataspaceconnector.dataplane.spi.pipeline.DataSink;
import org.eclipse.dataspaceconnector.dataplane.spi.pipeline.DataSinkFactory;
import org.eclipse.dataspaceconnector.minio.dataplane.minio.validation.S3DataAddressCredentialsValidationRule;
import org.eclipse.dataspaceconnector.minio.dataplane.minio.validation.S3DataAddressValidationRule;
import org.eclipse.dataspaceconnector.minio.dataplane.minio.validation.ValidationRule;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioClientProvider;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioSecretToken;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioTemporarySecretToken;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataFlowRequest;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.concurrent.ExecutorService;

import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.ACCESS_KEY_ID;
import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.ASSET_NAME;
import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.BUCKET_NAME;
import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.ENDPOINT;
import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.REGION;
import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.SECRET_ACCESS_KEY;

public class MinioDataSinkFactory implements DataSinkFactory {

    private static final int CHUNK_SIZE_IN_BYTES = 1024 * 1024 * 500; // 500MB chunk size

    private final ValidationRule<DataAddress> validation = new S3DataAddressValidationRule();
    private final ValidationRule<DataAddress> credentialsValidation = new S3DataAddressCredentialsValidationRule();
    private final MinioClientProvider clientProvider;
    private final ExecutorService executorService;
    private final Monitor monitor;
    private Vault vault;
    private TypeManager typeManager;

    public MinioDataSinkFactory(MinioClientProvider clientProvider, ExecutorService executorService, Monitor monitor, Vault vault, TypeManager typeManager) {
        this.clientProvider = clientProvider;
        this.executorService = executorService;
        this.monitor = monitor;
        this.vault = vault;
        this.typeManager = typeManager;
    }

    @Override
    public boolean canHandle(DataFlowRequest request) {
        System.out.println("DataSinkFactory: " + request.getSourceDataAddress().getType());
        return MinioBucketSchema.TYPE.equals(request.getDestinationDataAddress().getType());
    }

    @Override
    public @NotNull Result<Boolean> validate(DataFlowRequest request) {
        var destination = request.getDestinationDataAddress();

        return validation.apply(destination).map(it -> true);
    }

    @Override
    public DataSink createSink(DataFlowRequest request) {
        var validationResult = validate(request);
        if (validationResult.failed()) {
            throw new EdcException(String.join(", ", validationResult.getFailureMessages()));
        }

        var destination = request.getDestinationDataAddress();
        System.out.println("S3DataSinkFactory: " + destination);

        S3Client client;
        var secret = vault.resolveSecret(destination.getKeyName());
        System.out.println("S3DataSinkFactory: " + secret);
        if (secret != null) {
            System.out.println("S3DataSinkFactory: " + 1);
            var secretToken = typeManager.readValue(secret, MinioTemporarySecretToken.class);
            client = clientProvider.s3Client(destination.getProperty(REGION), secretToken, destination.getProperty(ENDPOINT));
        } else if (credentialsValidation.apply(destination).succeeded()) {
            System.out.println("S3DataSinkFactory: " + 2);
            var secretToken = new MinioSecretToken(destination.getProperty(ACCESS_KEY_ID), destination.getProperty(SECRET_ACCESS_KEY));
            client = clientProvider.s3Client(destination.getProperty(REGION), secretToken);
        } else {
            System.out.println("S3DataSinkFactory: " + 3);
            client = clientProvider.s3Client(destination.getProperty(REGION));
        }

        return MinioDataSink.Builder.newInstance()
            .bucketName(destination.getProperty(BUCKET_NAME)).assetName(destination.getProperty(ASSET_NAME))
            .keyName(destination.getKeyName())
            .requestId(request.getId())
            .executorService(executorService)
            .monitor(monitor)
            .client(client)
            .chunkSizeBytes(CHUNK_SIZE_IN_BYTES)
            .build();
    }

}
