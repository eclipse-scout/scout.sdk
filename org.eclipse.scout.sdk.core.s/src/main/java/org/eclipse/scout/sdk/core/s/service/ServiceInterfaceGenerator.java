/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.service;

import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;

/**
 * <h3>{@link ServiceInterfaceGenerator}</h3>
 *
 * @since 5.2.0
 */
public class ServiceInterfaceGenerator<TYPE extends ServiceInterfaceGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    mainType
        .asPublic()
        .asInterface()
        .withInterface(IScoutRuntimeTypes.IService)
        .withAnnotation(ScoutAnnotationGenerator.createTunnelToServer());
  }
}
