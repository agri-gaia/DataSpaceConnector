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

import org.eclipse.dataspaceconnector.dataplane.spi.pipeline.PipelineService;
import org.eclipse.dataspaceconnector.minio.minio.core.MinioClientProvider;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.util.concurrent.Executors;

public class DataPlaneMinioExtension implements ServiceExtension {

    @Inject
    private PipelineService pipelineService;

    @Inject
    private MinioClientProvider awsClientProvider;

    @Inject
    private Vault vault;

    @Override
    public String name() {
        return "Data Plane S3 Storage";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        System.out.println("Initializing Minio");
        var executorService = Executors.newFixedThreadPool(10); // TODO make configurable

        var monitor = context.getMonitor();

        var sourceFactory = new MinioDataSourceFactory(awsClientProvider);
        pipelineService.registerFactory(sourceFactory);

        var sinkFactory = new MinioDataSinkFactory(awsClientProvider, executorService, monitor, vault, context.getTypeManager());
        pipelineService.registerFactory(sinkFactory);
    }
}
