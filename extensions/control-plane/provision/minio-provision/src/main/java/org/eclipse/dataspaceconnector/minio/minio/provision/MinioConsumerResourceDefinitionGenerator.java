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

import org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.transfer.provision.ConsumerResourceDefinitionGenerator;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ResourceDefinition;

import static java.util.UUID.randomUUID;

/**
 * Generates S3 buckets on the consumer (requesting connector) that serve as data destinations.
 */
public class MinioConsumerResourceDefinitionGenerator implements ConsumerResourceDefinitionGenerator {

    @Override
    public ResourceDefinition generate(DataRequest dataRequest, Policy policy) {
        if (dataRequest.getDestinationType() != null) {
            System.out.println("S3ConsumerResourceDefinitionGenerator: " + dataRequest.getDestinationType());
            if (!MinioBucketSchema.TYPE.equals(dataRequest.getDestinationType())) {
                return null;
            }
            // FIXME generate region from policy engine
            var destination = dataRequest.getDataDestination();
            return MinioBucketResourceDefinition.Builder.newInstance()
                    .id(randomUUID().toString())
                    .bucketName(destination.getProperty(MinioBucketSchema.BUCKET_NAME))
                    .assetName(destination.getProperty(MinioBucketSchema.ASSET_NAME))
                    .endpoint(destination.getProperty(MinioBucketSchema.ENDPOINT))
                    .regionId(destination.getProperty(MinioBucketSchema.REGION)).build();

        } else if (dataRequest.getDataDestination() == null || !(dataRequest.getDataDestination().getType().equals(MinioBucketSchema.TYPE))) {
            return null;
        }
        var destination = dataRequest.getDataDestination();
        var id = randomUUID().toString();
        System.out.println("S3ConsumerResourceDefinitionGenerator: " + destination);
        return MinioBucketResourceDefinition.Builder.newInstance().id(id).bucketName(destination.getProperty(MinioBucketSchema.BUCKET_NAME)).regionId(destination.getProperty(MinioBucketSchema.REGION)).build();
    }
}
