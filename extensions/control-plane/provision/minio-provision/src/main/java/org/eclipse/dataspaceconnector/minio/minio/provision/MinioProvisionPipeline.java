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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioClientProvider;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;


public class MinioProvisionPipeline {

    private final MinioClientProvider clientProvider;
    private final Monitor monitor;

    private MinioProvisionPipeline(MinioClientProvider clientProvider,
                                   Monitor monitor) {
        this.clientProvider = clientProvider;
        this.monitor = monitor;
    }

    /**
     * Performs a non-blocking provisioning operation.
     */
    public CompletableFuture<MinioProvisionResponse> provision(MinioBucketResourceDefinition resourceDefinition) {
        monitor.debug("S3ProvisionPipeline: " + resourceDefinition);
        var s3AsyncClient = clientProvider.s3AsyncClient(resourceDefinition.getEndpoint());

        /*
        var request = CreateBucketRequest.builder()
                .bucket(resourceDefinition.getBucketName())
                .createBucketConfiguration(CreateBucketConfiguration.builder().build())
                .build();

        s3AsyncClient.createBucket(request);*/

        AWSCredentials credentials = clientProvider.admin_credentials();

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");
        AWSSecurityTokenService stsClient =
                AWSSecurityTokenServiceClientBuilder.standard()
                        .withEndpointConfiguration(new
                                AwsClientBuilder.EndpointConfiguration(clientProvider.getBaseEndpointOverride(), resourceDefinition.getRegionId()))
                        .withClientConfiguration(clientConfiguration)
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .build();

        AssumeRoleRequest assumeRequest = new AssumeRoleRequest()
                .withRoleArn("role")
                .withDurationSeconds(3600)
                .withRoleSessionName("sessionName")
                .withPolicy("{\"Version\":\"2012-10-17\",\"Statement\":[{\"Sid\":\"Stmt1\",\"Effect\":\"Allow\",\"Action\":\"s3:*\",\"Resource\":\"arn:aws:s3:::*\"}]}");
        AssumeRoleResult assumeResult =
                stsClient.assumeRole(assumeRequest);
        monitor.debug("AssumeResult: " + assumeResult);

        CompletableFuture<MinioProvisionResponse> completableFuture = new CompletableFuture<>();
        Executors.newCachedThreadPool().submit(() -> {
            completableFuture.complete(new MinioProvisionResponse(Role.builder().build(),
                    Credentials.builder().accessKeyId(assumeResult.getCredentials().getAccessKeyId())
                    .secretAccessKey(assumeResult.getCredentials().getSecretAccessKey())
                    .expiration(assumeResult.getCredentials().getExpiration().toInstant())
                    .sessionToken(assumeResult.getCredentials().getSessionToken()).build()));
            return null;
        });

        return completableFuture;
    }

    static class Builder {
        private Monitor monitor;
        private MinioClientProvider clientProvider;

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder monitor(Monitor monitor) {
            this.monitor = monitor;
            return this;
        }

        public Builder clientProvider(MinioClientProvider clientProvider) {
            this.clientProvider = clientProvider;
            return this;
        }

        public MinioProvisionPipeline build() {
            Objects.requireNonNull(clientProvider);
            Objects.requireNonNull(monitor);
            return new MinioProvisionPipeline(clientProvider, monitor);
        }
    }

}
