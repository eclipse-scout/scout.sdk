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

import java.util.Locale;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.java.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutAbstractApi;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.builder.body.ScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link WebServiceClientGenerator}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceClientGenerator<TYPE extends WebServiceClientGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_portType;
  private String m_service;

  @Override
  protected void setup() {
    this
        .withSuperClassFrom(IScoutApi.class, this::buildSuperType)
        .withInterface(portType())
        .withType(createUrlPropertyType())
        .withMethod(createExecInstallHandlers())
        .withMethod(createGetConfiguredEndpointUrlProperty())
        .withAllMethodsImplemented(
            new SimpleWorkingCopyTransformerBuilder()
                .withMethodMapper(this::fillOverriddenMethods)
                .build());
  }

  protected String buildSuperType(IScoutAbstractApi api) {
    var superTypeFqnBuilder = new StringBuilder(api.AbstractWebServiceClient().fqn());
    superTypeFqnBuilder.append(JavaTypes.C_GENERIC_START).append(service()).append(", ").append(portType()).append(JavaTypes.C_GENERIC_END);
    return superTypeFqnBuilder.toString();
  }

  @SuppressWarnings("MethodMayBeStatic")
  protected IMethodGenerator<?, ?> fillOverriddenMethods(ITransformInput<IMethod, IMethodGenerator<?, ?>> input) {
    return input.requestDefaultWorkingCopy().withBody(b -> b.appendCallToSame(b.context().requireApi(IScoutApi.class).AbstractWebServiceClient().newInvocationContextMethodName() + "().getPort()"));
  }

  protected String getBaseName() {
    return Strings.removeSuffix(elementName().orElseThrow(() -> newFail("WebService client has no name.")), ISdkConstants.SUFFIX_WS_CLIENT);
  }

  public String urlPropertyName() {
    return "jaxws." + getBaseName().toLowerCase(Locale.US) + ".url";
  }

  protected String getPropertyClassName() {
    return getBaseName() + ISdkConstants.SUFFIX_WS_URL_PROPERTY;
  }

  protected ITypeGenerator<?> createUrlPropertyType() {
    return TypeGenerator.create()
        .asPublic()
        .asStatic()
        .withElementName(getPropertyClassName())
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractStringConfigProperty().fqn())
        .withMethod(MethodGenerator.create()
            .asPublic()
            .withReturnType(String.class.getName())
            .withElementNameFrom(IScoutApi.class, api -> api.IConfigProperty().getKeyMethodName())
            .withBody(b -> b.returnClause().stringLiteral(urlPropertyName()).semicolon())
            .withAnnotation(AnnotationGenerator.createOverride()))
        .withMethod(MethodGenerator.create()
            .asPublic()
            .withReturnType(String.class.getName())
            .withElementNameFrom(IScoutApi.class, api -> api.IConfigProperty().descriptionMethodName())
            .withBody(b -> b.returnClause().stringLiteral("").semicolon().appendTodo("documentation"))
            .withAnnotation(AnnotationGenerator.createOverride()));
  }

  protected IMethodGenerator<?, ?> createGetConfiguredEndpointUrlProperty() {
    return MethodOverrideGenerator.createOverride()
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractWebServiceClient().getConfiguredEndpointUrlPropertyMethodName())
        .withBody(b -> b.returnClassLiteral(getPropertyClassName()));
  }

  protected static IMethodGenerator<?, ?> createExecInstallHandlers() {
    return MethodOverrideGenerator.createOverride()
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractWebServiceClient().execInstallHandlersMethodName())
        .withBody(b -> ScoutMethodBodyBuilder.create(b)
            .appendParameterName(0).dot().append("add").parenthesisOpen().appendBeansGetFrom(IScoutApi.class, IScoutApi::LogHandler).parenthesisClose().semicolon().nl()
            .appendParameterName(0).dot().append("add").parenthesisOpen().appendBeansGetFrom(IScoutApi.class, IScoutApi::WsConsumerCorrelationIdHandler).parenthesisClose().semicolon());
  }

  public String portType() {
    return m_portType;
  }

  public TYPE withPortType(String portType) {
    m_portType = Ensure.notBlank(portType);
    return thisInstance();
  }

  public String service() {
    return m_service;
  }

  public TYPE withService(String service) {
    m_service = Ensure.notBlank(service);
    return thisInstance();
  }
}
