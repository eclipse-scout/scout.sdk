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

import static java.util.Collections.emptyList;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils.JaxWsBindingMapping;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JaxwsBindingGenerator}</h3>
 *
 * @since 7.0.0
 */
public class JaxwsBindingGenerator implements ISourceGenerator<ISourceBuilder<?>> {

  private String m_jaxBNamespace;
  private String m_jaxWsNamespace;
  private String m_wsPackage;
  private URI m_wsdlLocation;
  private Iterable<JaxWsBindingMapping> m_names = emptyList();

  @Override
  public void generate(ISourceBuilder<?> builder) {
    var jaxBNamespace = jaxBNamespace().orElseThrow();
    var jaxWsNamespace = jaxWsNamespace().orElseThrow();
    var wsdlLocation = wsdlLocation().orElseThrow();
    var wsPackage = wsPackage().orElseThrow();

    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").nl();
    builder.append("<!-- binding to customize webservice artifacts (jaxws-namespace: ").append(jaxWsNamespace).append(" -->").nl();
    builder.append("<jaxws:bindings wsdlLocation=\"").append(wsdlLocation.toString()).append('"').nl();
    builder.append("  xmlns:jaxws=\"").append(jaxWsNamespace).append('"').nl();
    builder.append("  xmlns:jaxb=\"").append(jaxBNamespace).append('"').nl();
    builder.append("  xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"").nl();
    builder.append("  xmlns:xjc=\"http://java.sun.com/xml/ns/jaxb/xjc\"").nl();
    builder.append("  xmlns:javaee=\"http://java.sun.com/xml/ns/javaee\"").nl();
    builder.append("  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">").nl();
    builder.append("  <jaxws:bindings node=\"wsdl:definitions\">").nl();
    builder.append("    <jaxws:package name=\"").append(wsPackage).append("\"/>").nl();
    builder.append("  </jaxws:bindings>").nl();
    for (var mapping : names()) {
      String nodeAttr;
      if (mapping.isPortType()) {
        nodeAttr = JaxWsUtils.getPortTypeXPath(mapping.getWsdlName());
      }
      else {
        nodeAttr = JaxWsUtils.getWebServiceXPath(mapping.getWsdlName());
      }
      builder.append("  <jaxws:bindings node=\"").append(nodeAttr).append("\">").nl();
      builder.append("    <jaxws:class name=\"").append(mapping.getClassName()).append("\" />").nl();
      builder.append("  </jaxws:bindings>").nl();
    }
    builder.append("</jaxws:bindings>").nl();
  }

  public Optional<String> jaxBNamespace() {
    return Strings.notBlank(m_jaxBNamespace);
  }

  public JaxwsBindingGenerator withJaxBNamespace(String jaxBNamespace) {
    m_jaxBNamespace = jaxBNamespace;
    return this;
  }

  public Optional<String> jaxWsNamespace() {
    return Strings.notBlank(m_jaxWsNamespace);
  }

  public JaxwsBindingGenerator withJaxWsNamespace(String jaxWsNamespace) {
    m_jaxWsNamespace = jaxWsNamespace;
    return this;
  }

  public Optional<String> wsPackage() {
    return Strings.notBlank(m_wsPackage);
  }

  public JaxwsBindingGenerator withWsPackage(String wsPackage) {
    m_wsPackage = wsPackage;
    return this;
  }

  public Optional<URI> wsdlLocation() {
    return Optional.ofNullable(m_wsdlLocation);
  }

  public JaxwsBindingGenerator withWsdlLocation(URI wsdlLocation) {
    m_wsdlLocation = wsdlLocation;
    return this;
  }

  public Iterable<JaxWsBindingMapping> names() {
    return m_names;
  }

  public JaxwsBindingGenerator withNames(Iterable<JaxWsBindingMapping> names) {
    m_names = Objects.requireNonNullElse(names, emptyList());
    return this;
  }
}
