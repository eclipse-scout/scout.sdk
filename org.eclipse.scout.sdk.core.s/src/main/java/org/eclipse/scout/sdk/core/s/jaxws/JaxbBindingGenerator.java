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

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;

/**
 * <h3>{@link JaxbBindingGenerator}</h3>
 *
 * @since 7.0.0
 */
public class JaxbBindingGenerator implements ISourceGenerator<ISourceBuilder<?>> {
  @Override
  public void generate(ISourceBuilder<?> builder) {
    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").nl();
    builder.append("<!-- binding to customize xsd schema artifacts (jaxb-namespace: ").append(JaxWsUtils.JAX_B_NAMESPACE).append(") -->").nl();
    builder.append("<bindings xmlns=\"").append(JaxWsUtils.JAX_B_NAMESPACE).append("\" version=\"2.1\">").nl();
    builder.append("</bindings>").nl();
  }
}
