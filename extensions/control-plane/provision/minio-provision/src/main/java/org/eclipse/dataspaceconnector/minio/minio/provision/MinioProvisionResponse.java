/*
 *  Copyright (c) 2022 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - Initial implementation
 *
 */

package org.eclipse.dataspaceconnector.minio.minio.provision;

import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.sts.model.Credentials;

public class MinioProvisionResponse {
    private final Role role;
    private final Credentials credentials;

    public MinioProvisionResponse(Role role, Credentials credentials) {
        this.role = role;
        this.credentials = credentials;
    }

    public Role getRole() {
        return role;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
