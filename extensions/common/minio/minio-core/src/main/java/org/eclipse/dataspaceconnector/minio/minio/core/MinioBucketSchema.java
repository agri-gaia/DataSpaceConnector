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

package org.eclipse.dataspaceconnector.minio.minio.core;

public interface MinioBucketSchema {
    String TYPE = "AmazonS3";
    String REGION = "region";
    String ENDPOINT = "endpoint";
    String BUCKET_NAME = "bucketName";
    String ASSET_NAME = "assetName";
    String ACCESS_KEY_ID = "accessKeyId";
    String SECRET_ACCESS_KEY = "secretAccessKey";
}
