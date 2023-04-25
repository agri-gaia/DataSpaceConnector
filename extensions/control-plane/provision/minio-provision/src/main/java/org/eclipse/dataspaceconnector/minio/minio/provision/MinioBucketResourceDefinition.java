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

import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ResourceDefinition;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * An S3 bucket and access credentials to be provisioned.
 */
public class MinioBucketResourceDefinition extends ResourceDefinition {
    private String regionId;
    private String bucketName;
    private String endpoint;
    private String assetName;
    private Supplier<Boolean> checker;

    private MinioBucketResourceDefinition() {
    }

    public String getRegionId() {
        return regionId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getKeyName() {
        return assetName;
    }

    public String getAssetName() {
        return assetName;
    }

    @Override
    public String toString() {
        return "S3BucketResourceDefinition{" +
                "regionId='" + regionId + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", keyName='" + assetName + '\'' +
                ", checker=" + checker +
                ", id='" + id + '\'' +
                ", transferProcessId='" + transferProcessId + '\'' +
                '}';
    }

    public static class Builder extends ResourceDefinition.Builder<MinioBucketResourceDefinition, Builder> {

        private Builder() {
            super(new MinioBucketResourceDefinition());
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder regionId(String regionId) {
            resourceDefinition.regionId = regionId;
            return this;
        }

        public Builder bucketName(String bucketName) {
            resourceDefinition.bucketName = bucketName;
            return this;
        }

        public Builder endpoint(String endpoint) {
            resourceDefinition.endpoint = endpoint;
            return this;
        }

        public Builder assetName(String assetName) {
            resourceDefinition.assetName = assetName;
            return this;
        }

        @Override
        protected void verify() {
            super.verify();
            Objects.requireNonNull(resourceDefinition.regionId, "regionId");
            Objects.requireNonNull(resourceDefinition.bucketName, "bucketName");
            Objects.requireNonNull(resourceDefinition.bucketName, "endpoint");
            Objects.requireNonNull(resourceDefinition.assetName, "assetName");
        }
    }

}
