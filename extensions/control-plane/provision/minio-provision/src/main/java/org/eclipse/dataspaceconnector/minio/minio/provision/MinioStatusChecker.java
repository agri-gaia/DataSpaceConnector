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

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioBucketSchema;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioClientProvider;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ProvisionedResource;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.StatusChecker;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;
import java.util.concurrent.CompletionException;

import static java.lang.String.format;

public class MinioStatusChecker implements StatusChecker {
    private final MinioClientProvider clientProvider;
    private final RetryPolicy<Object> retryPolicy;

    public MinioStatusChecker(MinioClientProvider clientProvider, RetryPolicy<Object> retryPolicy) {
        this.clientProvider = clientProvider;
        this.retryPolicy = retryPolicy;
    }

    @Override
    public boolean isComplete(TransferProcess transferProcess, List<ProvisionedResource> resources) {
        if (resources.isEmpty()) {
            var destination = transferProcess.getDataRequest().getDataDestination();
            var bucketName = destination.getProperty(MinioBucketSchema.BUCKET_NAME);
            var region = destination.getProperty(MinioBucketSchema.REGION);
            var endpoint = destination.getProperty(MinioBucketSchema.ENDPOINT);
            return checkBucket(bucketName, region, endpoint);
        } else {
            System.out.println(resources);
            for (var resource : resources) {
                if (resource instanceof MinioBucketProvisionedResource) {
                    var provisionedResource = (MinioBucketProvisionedResource) resource;
                    try {
                        var bucketName = provisionedResource.getBucketName();
                        var region = provisionedResource.getRegion();
                        var endpoint = provisionedResource.getEndpoint();
                        return checkBucket(bucketName, region, endpoint);
                    } catch (CompletionException cpe) {
                        if (cpe.getCause() instanceof NoSuchBucketException) {
                            return false;
                        }
                        throw cpe;
                    }

                }
            }

        }

        // otherwise, we have an implementation error
        throw new EdcException(format("No bucket resource was associated with the transfer process: %s - cannot determine completion.", transferProcess.getId()));
    }

    private boolean checkBucket(String bucketName, String region, String endpoint) {
        try {
            var s3client = clientProvider.s3AsyncClient(endpoint);

            var rq = ListObjectsRequest.builder().bucket(bucketName).build();
            var response = Failsafe.with(retryPolicy)
                    .getStageAsync(() -> s3client.listObjects(rq))
                    .join();
            return response.contents().stream().anyMatch(s3object -> s3object.key().endsWith(".complete"));
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof S3Exception) {
                return false;
            } else {
                throw ex;
            }
        }
    }

}
