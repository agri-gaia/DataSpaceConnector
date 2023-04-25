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
import org.eclipse.dataspaceconnector.dataplane.spi.pipeline.ParallelSink;
import org.eclipse.dataspaceconnector.spi.response.StatusResult;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.spi.response.ResponseStatus.FATAL_ERROR;

class MinioDataSink extends ParallelSink {

    private S3Client client;
    private String bucketName;
    private String assetName;
    private String keyName;
    private int chunkSize;

    private MinioDataSink() {}

    @Override
    protected StatusResult<Void> transferParts(List<DataSource.Part> parts) {
        System.out.println(bucketName);
        System.out.println(assetName);
        for (var part : parts) {
            try (var input = part.openStream()) {
                PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(assetName).build();

                client.putObject(request, RequestBody.fromBytes(input.readAllBytes()));

            } catch (Exception e) {
                return uploadFailure(e, assetName);
            }
        }

        return StatusResult.success();
    }

    @Override
    protected StatusResult<Void> complete() {
        var completeKeyName = keyName + ".complete";
        var request = PutObjectRequest.builder().bucket(bucketName).key(completeKeyName).build();
        try {
            client.putObject(request, RequestBody.empty());
            return super.complete();
        } catch (Exception e) {
            return uploadFailure(e, completeKeyName);
        }

    }

    @NotNull
    private StatusResult<Void> uploadFailure(Exception e, String keyName) {
        var message = format("Error writing the %s object on the %s bucket: %s", keyName, bucketName, e.getMessage());
        monitor.severe(message, e);
        return StatusResult.failure(FATAL_ERROR, message);
    }

    public static class Builder extends ParallelSink.Builder<Builder, MinioDataSink> {

        private Builder() {
            super(new MinioDataSink());
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder client(S3Client client) {
            sink.client = client;
            return this;
        }

        public Builder bucketName(String bucketName) {
            sink.bucketName = bucketName;
            return this;
        }

        public Builder keyName(String keyName) {
            sink.keyName = keyName;
            return this;
        }

        public Builder assetName(String assetName) {
            sink.assetName = assetName;
            return this;
        }

        public Builder chunkSizeBytes(int chunkSize) {
            sink.chunkSize = chunkSize;
            return this;
        }

        @Override
        protected void validate() {}
    }
}
