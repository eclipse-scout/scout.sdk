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

import static java.util.Collections.reverse;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link EmptyWsdlGenerator}</h3>
 *
 * @since 7.0.0
 */
public class EmptyWsdlGenerator implements ISourceGenerator<ISourceBuilder<?>> {

  private String m_name;
  private String m_packageName;

  @Override
  @SuppressWarnings("HttpUrlsUsage")
  public void generate(ISourceBuilder<?> builder) {
    var name = name().orElseThrow();
    var nameSpace = packageToNamespace(name, packageName().orElseThrow());

    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").nl();
    builder.append("<wsdl:definitions name=\"").append(name).append('"').nl();
    builder.append("  xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"").nl();
    builder.append("  xmlns:tns=\"http://").append(nameSpace).append('/').append(name).append("/\"").nl();
    builder.append("  xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"").nl();
    builder.append("  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"").nl();
    builder.append("  targetNamespace=\"http://").append(nameSpace).append('/').append(name).append("/\">").nl();
    builder.append("  <wsdl:types>").nl();
    builder.append("    <xsd:schema targetNamespace=\"http://").append(nameSpace).append('/').append(name).append("/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">").nl();
    builder.append("      <xsd:element name=\"InElement\" type=\"xsd:string\"/>").nl();
    builder.append("      <xsd:element name=\"OutElement\" type=\"xsd:string\"/>").nl();
    builder.append("    </xsd:schema>").nl();
    builder.append("  </wsdl:types>").nl();
    builder.append("  <wsdl:message name=\"NewOperationRequest\">").nl();
    builder.append("    <wsdl:part element=\"tns:InElement\" name=\"in\"/>").nl();
    builder.append("  </wsdl:message>").nl();
    builder.append("  <wsdl:message name=\"NewOperationResponse\">").nl();
    builder.append("    <wsdl:part element=\"tns:OutElement\" name=\"ret\"/>").nl();
    builder.append("  </wsdl:message>").nl();
    builder.append("  <wsdl:portType name=\"").append(name).append("PortType\">").nl();
    builder.append("    <wsdl:operation name=\"NewOperation\">").nl();
    builder.append("      <wsdl:input message=\"tns:NewOperationRequest\"/>").nl();
    builder.append("      <wsdl:output message=\"tns:NewOperationResponse\"/>").nl();
    builder.append("    </wsdl:operation>").nl();
    builder.append("  </wsdl:portType>").nl();
    builder.append("  <wsdl:binding name=\"").append(name).append("PortSoapBinding\" type=\"tns:").append(name).append("PortType\">").nl();
    builder.append("    <soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>").nl();
    builder.append("    <wsdl:operation name=\"NewOperation\">").nl();
    builder.append("      <soap:operation soapAction=\"http://").append(nameSpace).append('/').append(name).append("/NewOperation\"/>").nl();
    builder.append("      <wsdl:input>").nl();
    builder.append("        <soap:body use=\"literal\"/>").nl();
    builder.append("      </wsdl:input>").nl();
    builder.append("      <wsdl:output>").nl();
    builder.append("        <soap:body use=\"literal\"/>").nl();
    builder.append("      </wsdl:output>").nl();
    builder.append("    </wsdl:operation>").nl();
    builder.append("  </wsdl:binding>").nl();
    builder.append("  <wsdl:service name=\"").append(name).append("WebService\">").nl();
    builder.append("    <wsdl:port binding=\"tns:").append(name).append("PortSoapBinding\" name=\"").append(name).append("Port\">").nl();
    builder.append("      <soap:address location=\"http://").append(nameSpace).append("/\"/>").nl();
    builder.append("    </wsdl:port>").nl();
    builder.append("  </wsdl:service>").nl();
    builder.append("</wsdl:definitions>").nl();
  }

  protected static String packageToNamespace(String name, CharSequence packageName) {
    var parts = ISdkConstants.REGEX_DOT.split(packageName);
    if (parts.length > 0 && name.equalsIgnoreCase(parts[parts.length - 1])) {
      parts = Arrays.copyOf(parts, parts.length - 1);
    }
    var partsInXmlOrder = Arrays.asList(parts);
    reverse(partsInXmlOrder);
    return String.join(".", partsInXmlOrder);
  }

  public Optional<String> name() {
    return Strings.notBlank(m_name);
  }

  /**
   * @param name
   *          The name of the wsdl
   * @return this
   */
  public EmptyWsdlGenerator withName(String name) {
    m_name = name;
    return this;
  }

  public Optional<String> packageName() {
    return Strings.notBlank(m_packageName);
  }

  /**
   * @param packageName
   *          The package name of the wsdl (reverse of the namespace).
   * @return this
   */
  public EmptyWsdlGenerator withPackage(String packageName) {
    m_packageName = packageName;
    return this;
  }
}
