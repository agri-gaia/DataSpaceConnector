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

plugins {
    `java-library`
}

val awsVersion: String by project
val failsafeVersion: String by project


dependencies {
    api(project(":spi:control-plane:transfer-spi"))

    api("dev.failsafe:failsafe:${failsafeVersion}")

    api("software.amazon.awssdk:sts:${awsVersion}")
    api("software.amazon.awssdk:iam:${awsVersion}")
    api("software.amazon.awssdk:s3:${awsVersion}")
    api("com.amazonaws:aws-java-sdk:1.12.290")
}

publishing {
    publications {
        create<MavenPublication>("minio-core") {
            artifactId = "minio-core"
            from(components["java"])
        }
    }
}
