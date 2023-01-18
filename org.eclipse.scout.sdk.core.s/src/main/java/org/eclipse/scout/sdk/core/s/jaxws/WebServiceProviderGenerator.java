/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link WebServiceProviderGenerator}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceProviderGenerator<TYPE extends WebServiceProviderGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_portType;

  @Override
  protected void setup() {
    this
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
    return thisInstance();
  }

}
