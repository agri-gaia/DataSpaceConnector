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

import org.eclipse.dataspaceconnector.dataplane.spi.pipeline.DataSource;
import org.eclipse.dataspaceconnector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.dataspaceconnector.minio.dataplane.minio.validation.S3DataAddressCredentialsValidationRule;
import org.eclipse.dataspaceconnector.minio.dataplane.minio.validation.S3DataAddressValidationRule;
import org.eclipse.dataspaceconnector.minio.dataplane.minio.validation.ValidationRule;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioClientProvider;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioSecretToken;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.eclipse.dataspaceconnector.spi.types.domain.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataFlowRequest;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.ACCESS_KEY_ID;
import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.BUCKET_NAME;
import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.REGION;
import static org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema.SECRET_ACCESS_KEY;

public class MinioDataSourceFactory implements DataSourceFactory {

    private final ValidationRule<DataAddress> validation = new S3DataAddressValidationRule();
    private final ValidationRule<DataAddress> credentialsValidation = new S3DataAddressCredentialsValidationRule();
    private final MinioClientProvider clientProvider;

    public MinioDataSourceFactory(MinioClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public boolean canHandle(DataFlowRequest request) {
        System.out.println("DataSourceFactory: " + request.getSourceDataAddress().getType());
        return MinioBucketSchema.TYPE.equals(request.getSourceDataAddress().getType());
    }

    @Override
    public @NotNull Result<Boolean> validate(DataFlowRequest request) {
        var source = request.getSourceDataAddress();

        return validation.apply(source).map(it -> true);
    }

    @Override
    public DataSource createSource(DataFlowRequest request) {
        var validationResult = validate(request);
        if (validationResult.failed()) {
            throw new EdcException(String.join(", ", validationResult.getFailureMessages()));
        }

        var source = request.getSourceDataAddress();

        var secretToken = new MinioSecretToken(source.getProperty(ACCESS_KEY_ID), source.getProperty(SECRET_ACCESS_KEY));

        var client = credentialsValidation.apply(source).succeeded()
                ? clientProvider.s3Client(source.getProperty(REGION), secretToken)
                : clientProvider.s3Client(source.getProperty(REGION));

        System.out.println(source);

        return MinioDataSource.Builder.newInstance()
                .bucketName(source.getProperty(BUCKET_NAME))
                .keyName(source.getKeyName())
                .client(client)
                .build();
    }

}
