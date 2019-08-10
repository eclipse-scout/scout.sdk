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

import static org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator.createAuthentication;
import static org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator.createHandler;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link EntryPointDefinitionGenerator}</h3>
 *
 * @since 5.2.0
 */
public class EntryPointDefinitionGenerator<TYPE extends EntryPointDefinitionGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_portTypeFqn;
  private String m_serviceName;
  private String m_portName;
  private String m_entryPointPackage;
  private String m_entryPointName;

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    mainType
        .asInterface()
        .withAnnotation(AnnotationGenerator.create()
            .withElementName(IScoutRuntimeTypes.WebServiceEntryPoint)
            .withElement(JaxWsUtils.ENTRY_POINT_DEFINITION_ENDPOINTINTERFACE_ATTRIBUTE, b -> b.classLiteral(portTypeFqn()
                .orElseThrow(() -> newFail("Fully qualified name of the PortType must be specified."))))
            .withElement(JaxWsUtils.ENTRY_POINT_DEFINITION_PACKAGE_ATTRIBUTE, b -> b.stringLiteral(entryPointPackage()
                .orElseThrow(() -> newFail("Entry point package must be specified."))))
            .withElement(JaxWsUtils.ENTRY_POINT_DEFINITION_NAME_ATTRIBUTE, b -> b.stringLiteral(entryPointName()
                .orElseThrow(() -> newFail("Entry point name must be specified"))))
            .withElement(JaxWsUtils.ENTRY_POINT_DEFINITION_SERVICE_NAME_ATTRIBUTE, b -> b.stringLiteral(serviceName()
                .orElseThrow(() -> newFail("Service name must be specified."))))
            .withElement(JaxWsUtils.ENTRY_POINT_DEFINITION_PORT_NAME_ATTRIBUTE, b -> b.stringLiteral(portName()
                .orElseThrow(() -> newFail("Port name must be specified."))))
            .withElement(JaxWsUtils.ENTRY_POINT_DEFINITION_HANDLER_CHAIN_ATTRIBUTE, b -> b.array(
                Stream.of(
                    createHandler(IScoutRuntimeTypes.WsProviderCorrelationIdHandler),
                    createHandler(IScoutRuntimeTypes.LogHandler)),
                true))
            .withElement(JaxWsUtils.ENTRY_POINT_DEFINITION_AUTH_ATTRIBUTE, b -> createAuthentication().generate(b)));
  }

  public Optional<String> portTypeFqn() {
    return Strings.notBlank(m_portTypeFqn);
  }

  public TYPE withPortTypeFqn(String portTypeFqn) {
    m_portTypeFqn = portTypeFqn;
    return currentInstance();
  }

  public Optional<String> entryPointPackage() {
    return Strings.notBlank(m_entryPointPackage);
  }

  public TYPE withEntryPointPackage(String entryPointPackage) {
    m_entryPointPackage = entryPointPackage;
    return currentInstance();
  }

  public Optional<String> serviceName() {
    return Strings.notBlank(m_serviceName);
  }

  public TYPE withServiceName(String serviceName) {
    m_serviceName = serviceName;
    return currentInstance();
  }

  public Optional<String> portName() {
    return Strings.notBlank(m_portName);
  }

  public TYPE withPortName(String portName) {
    m_portName = portName;
    return currentInstance();
  }

  public Optional<String> entryPointName() {
    return Strings.notBlank(m_entryPointName);
  }

  public TYPE withEntryPointName(String entryPointName) {
    m_entryPointName = entryPointName;
    return currentInstance();
  }
}
