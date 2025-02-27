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

package org.eclipse.dataspaceconnector.iam.oauth2.spi;

import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.ExtensionPoint;
import org.eclipse.dataspaceconnector.spi.jwt.JwtDecoratorRegistry;

/**
 * Registry for Oauth2 jwt decorators.
 */
@ExtensionPoint
public interface Oauth2JwtDecoratorRegistry extends JwtDecoratorRegistry {
}
