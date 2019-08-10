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
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link WebServiceProviderGenerator}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceProviderGenerator<TYPE extends WebServiceProviderGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_portType;

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    mainType
        .withInterface(portType()
            .orElseThrow(() -> newFail("PortType must be specified")))
        .withAnnotation(ScoutAnnotationGenerator.createApplicationScoped())
        .withAllMethodsImplemented();
  }

  public Optional<String> portType() {
    return Strings.notBlank(m_portType);
  }

  public TYPE withPortType(String portType) {
    m_portType = portType;
    return currentInstance();
  }

}
