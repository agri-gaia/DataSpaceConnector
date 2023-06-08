/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.sample.extension.policy;


import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.spi.policy.PolicyEngine;
import org.eclipse.dataspaceconnector.spi.policy.RuleBindingRegistry;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import static org.eclipse.dataspaceconnector.spi.policy.PolicyEngine.ALL_SCOPES;

public class PolicyFunctionsExtension implements ServiceExtension {
    private final String policyTimeKey = "POLICY_EVALUATION_TIME";

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Inject
    private PolicyEngine policyEngine;


    @Override
    public String name() {
        return "Policy - contract-negotiation policies";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

       ruleBindingRegistry.bind("USE", ALL_SCOPES);
       ruleBindingRegistry.bind(policyTimeKey, ALL_SCOPES);
       policyEngine.registerFunction(ALL_SCOPES, Permission.class, policyTimeKey, new TimeIntervalFunction(monitor));

       context.getMonitor().info("Policy Extension for Policy Sample (contract-negotiation) initialized!");
    }

}
