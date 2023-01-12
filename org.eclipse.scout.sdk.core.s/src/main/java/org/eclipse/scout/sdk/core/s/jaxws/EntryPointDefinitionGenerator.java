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

import static org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator.createAuthentication;
import static org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator.createHandler;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutVariousApi;
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
  protected void setup() {
    this
        .asInterface()
        .withAnnotation(AnnotationGenerator.create()
            .withAnnotationNameFrom(IScoutApi.class, IScoutApi::WebServiceEntryPoint)
            .withElementFrom(IScoutApi.class, api -> api.WebServiceEntryPoint().endpointInterfaceElementName(), b -> b.classLiteral(portTypeFqn()
                .orElseThrow(() -> newFail("Fully qualified name of the PortType must be specified."))))
            .withElementFrom(IScoutApi.class, api -> api.WebServiceEntryPoint().entryPointPackageElementName(), b -> b.stringLiteral(entryPointPackage()
                .orElseThrow(() -> newFail("Entry point package must be specified."))))
            .withElementFrom(IScoutApi.class, api -> api.WebServiceEntryPoint().entryPointNameElementName(), b -> b.stringLiteral(entryPointName()
                .orElseThrow(() -> newFail("Entry point name must be specified"))))
            .withElementFrom(IScoutApi.class, api -> api.WebServiceEntryPoint().serviceNameElementName(), b -> b.stringLiteral(serviceName()
                .orElseThrow(() -> newFail("Service name must be specified."))))
            .withElementFrom(IScoutApi.class, api -> api.WebServiceEntryPoint().portNameElementName(), b -> b.stringLiteral(portName()
                .orElseThrow(() -> newFail("Port name must be specified."))))
            .withElementFrom(IScoutApi.class, api -> api.WebServiceEntryPoint().handlerChainElementName(), b -> b.array(
                Stream.of(
                    createHandler(IScoutApi.class, IScoutVariousApi::WsProviderCorrelationIdHandler),
                    createHandler(IScoutApi.class, IScoutVariousApi::LogHandler)),
                true))
            .withElementFrom(IScoutApi.class, api -> api.WebServiceEntryPoint().authenticationElementName(), b -> createAuthentication().generate(b)));
  }

  public Optional<String> portTypeFqn() {
    return Strings.notBlank(m_portTypeFqn);
  }

  public TYPE withPortTypeFqn(String portTypeFqn) {
    m_portTypeFqn = portTypeFqn;
    return thisInstance();
  }

  public Optional<String> entryPointPackage() {
    return Strings.notBlank(m_entryPointPackage);
  }

  public TYPE withEntryPointPackage(String entryPointPackage) {
    m_entryPointPackage = entryPointPackage;
    return thisInstance();
  }

  public Optional<String> serviceName() {
    return Strings.notBlank(m_serviceName);
  }

  public TYPE withServiceName(String serviceName) {
    m_serviceName = serviceName;
    return thisInstance();
  }

  public Optional<String> portName() {
    return Strings.notBlank(m_portName);
  }

  public TYPE withPortName(String portName) {
    m_portName = portName;
    return thisInstance();
  }

  public Optional<String> entryPointName() {
    return Strings.notBlank(m_entryPointName);
  }

  public TYPE withEntryPointName(String entryPointName) {
    m_entryPointName = entryPointName;
    return thisInstance();
  }
}
