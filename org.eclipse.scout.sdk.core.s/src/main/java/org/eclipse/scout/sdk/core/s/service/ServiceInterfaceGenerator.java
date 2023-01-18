/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.service;

import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;

/**
 * <h3>{@link ServiceInterfaceGenerator}</h3>
 *
 * @since 5.2.0
 */
public class ServiceInterfaceGenerator<TYPE extends ServiceInterfaceGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  @Override
  protected void setup() {
    asPublic()
        .asInterface()
        .withInterfaceFrom(IScoutApi.class, api -> api.IService().fqn())
        .withAnnotation(ScoutAnnotationGenerator.createTunnelToServer());
  }
}
