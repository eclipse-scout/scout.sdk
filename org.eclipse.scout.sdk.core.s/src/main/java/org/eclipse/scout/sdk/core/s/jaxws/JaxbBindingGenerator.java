/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JaxbBindingGenerator}</h3>
 *
 * @since 7.0.0
 */
public class JaxbBindingGenerator implements ISourceGenerator<ISourceBuilder<?>> {

  private String m_jaxBNamespace;
  private String m_jaxBVersion;

  @Override
  public void generate(ISourceBuilder<?> builder) {
    var jaxBNamespace = jaxBNamespace().orElseThrow();
    var jaxBVersion = jaxBVersion().orElseThrow();

    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").nl();
    builder.append("<!-- binding to customize xsd schema artifacts (jaxb-namespace: ").append(jaxBNamespace).append(") -->").nl();
    builder.append("<bindings xmlns=\"").append(jaxBNamespace).append("\" version=\"").append(jaxBVersion).append("\">").nl();
    builder.append("</bindings>").nl();
  }

  public Optional<String> jaxBNamespace() {
    return Strings.notBlank(m_jaxBNamespace);
  }

  public JaxbBindingGenerator withJaxBNamespace(String jaxBNamespace) {
    m_jaxBNamespace = jaxBNamespace;
    return this;
  }

  public Optional<String> jaxBVersion() {
    return Strings.notBlank(m_jaxBVersion);
  }

  public JaxbBindingGenerator withJaxBVersion(String jaxBVersion) {
    m_jaxBVersion = jaxBVersion;
    return this;
  }
}
