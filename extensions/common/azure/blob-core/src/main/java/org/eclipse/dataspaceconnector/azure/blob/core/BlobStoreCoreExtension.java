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
 *       Amadeus - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.azure.blob.core;

import org.eclipse.dataspaceconnector.azure.blob.core.api.BlobStoreApi;
import org.eclipse.dataspaceconnector.azure.blob.core.api.BlobStoreApiImpl;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.EdcSetting;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Extension;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provides;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

@Provides(BlobStoreApi.class)
@Extension(value = BlobStoreCoreExtension.NAME)
public class BlobStoreCoreExtension implements ServiceExtension {

    @EdcSetting
    public static final String EDC_BLOBSTORE_ENDPOINT_TEMPLATE = "edc.blobstore.endpoint.template";
    public static final String NAME = "Azure BlobStore Core";

    @Inject
    private Vault vault;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var blobstoreEndpointTemplate = context
                .getSetting(EDC_BLOBSTORE_ENDPOINT_TEMPLATE, "https://%s.blob.core.windows.net");

        var blobStoreApi = new BlobStoreApiImpl(vault, blobstoreEndpointTemplate);
        context.registerService(BlobStoreApi.class, blobStoreApi);
    }
}
