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

import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.AtomicConstraintFunction;
import org.eclipse.dataspaceconnector.spi.policy.PolicyContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeIntervalFunction implements AtomicConstraintFunction<Permission> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final Monitor monitor;

    public TimeIntervalFunction(Monitor monitor) {
        this.monitor = monitor;
    }



    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        try {
            var policyDate = DATE_FORMAT.parse((String) rightValue);
            var nowDate = new Date();
            return switch (operator) {
                case LT -> nowDate.before(policyDate);
                case LEQ -> nowDate.before(policyDate) || nowDate.equals(policyDate);
                case GT -> nowDate.after(policyDate);
                case GEQ -> nowDate.after(policyDate) || nowDate.equals(policyDate);
                case EQ -> nowDate.equals(policyDate);
                case NEQ -> !nowDate.equals(policyDate);
                default -> false;
            };
        } catch (ParseException e) {
            monitor.severe("Failed to parse right value of constraint to date.");
            return false;
        }
    }
}


