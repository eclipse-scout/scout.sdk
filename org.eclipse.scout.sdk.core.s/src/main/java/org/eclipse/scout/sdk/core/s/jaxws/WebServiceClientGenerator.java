/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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

import java.util.Locale;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutAbstractApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.builder.java.body.ScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link WebServiceClientGenerator}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceClientGenerator<TYPE extends WebServiceClientGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_portType;
  private String m_service;

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    mainType.withSuperClassFrom(IScoutApi.class, this::buildSuperType)
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
  protected IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> fillOverriddenMethods(ITransformInput<IMethod, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> input) {
    return input.requestDefaultWorkingCopy().withBody(b -> b.appendCallToSame(b.context().requireApi(IScoutApi.class).AbstractWebServiceClient().newInvocationContextMethodName() + "().getPort()"));
  }

  protected String getBaseName() {
    var name = elementName().orElseThrow(() -> newFail("WebService client has no name."));
    if (name.endsWith(ISdkConstants.SUFFIX_WS_CLIENT)) {
      name = name.substring(0, name.length() - ISdkConstants.SUFFIX_WS_CLIENT.length());
    }
    return name;
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

  protected IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createGetConfiguredEndpointUrlProperty() {
    return MethodOverrideGenerator.createOverride()
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractWebServiceClient().getConfiguredEndpointUrlPropertyMethodName())
        .withBody(b -> b.returnClassLiteral(getPropertyClassName()));
  }

  protected static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createExecInstallHandlers() {
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
