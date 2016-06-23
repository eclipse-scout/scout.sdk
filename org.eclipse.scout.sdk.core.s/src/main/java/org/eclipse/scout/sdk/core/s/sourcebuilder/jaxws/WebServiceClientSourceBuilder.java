/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.jaxws;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.AbstractEntitySourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.IMethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link WebServiceClientSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceClientSourceBuilder extends AbstractEntitySourceBuilder {

  private String m_portTypeSignature;
  private String m_serviceSignature;

  public WebServiceClientSourceBuilder(String elementName, String packageName, IJavaEnvironment env) {
    super(elementName, packageName, env);
  }

  @Override
  public void setup() {
    setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

    TypeSourceBuilder clientBuilder = new TypeSourceBuilder(getEntityName());
    clientBuilder.setFlags(Flags.AccPublic);
    StringBuilder superTypeFqnBuilder = new StringBuilder(IScoutRuntimeTypes.AbstractWebServiceClient);
    superTypeFqnBuilder.append(ISignatureConstants.C_GENERIC_START).append(SignatureUtils.toFullyQualifiedName(getServiceSignature()))
        .append(", ").append(SignatureUtils.toFullyQualifiedName(getPortTypeSignature())).append(ISignatureConstants.C_GENERIC_END);
    clientBuilder.setSuperTypeSignature(Signature.createTypeSignature(superTypeFqnBuilder.toString()));
    clientBuilder.addInterfaceSignature(getPortTypeSignature());
    addType(clientBuilder);

    // URL config property
    ITypeSourceBuilder urlPropertyType = createUrlPropertyType();
    clientBuilder.addType(urlPropertyType);

    // execInstallHandlers
    clientBuilder.addMethod(createExecInstallHandlers(clientBuilder));

    // getConfiguredEndpointUrlProperty
    clientBuilder.addMethod(createGetConfiguredEndpointUrlProperty(clientBuilder, urlPropertyType));

    // web service methods
    if (getJavaEnvironment().findType(SignatureUtils.toFullyQualifiedName(getPortTypeSignature())) == null || getJavaEnvironment().findType(SignatureUtils.toFullyQualifiedName(getServiceSignature())) == null) {
      // stub generation failed and no port type and service type are available. we cannot implement the required methods as we don't know them -> skip
      SdkLog.warning("Web Service client could not be filled with all methods because the port type or web service stub could not be found.");
      return;
    }

    addUnimplementedMethods(clientBuilder);
  }

  protected void addUnimplementedMethods(ITypeSourceBuilder builder) {
    List<IMethodSourceBuilder> unimplementedMethods = MethodSourceBuilderFactory.createUnimplementedMethods(builder, getPackageName(), getJavaEnvironment());
    for (IMethodSourceBuilder unimplemented : unimplementedMethods) {
      StringBuilder bodyBuilder = new StringBuilder();
      if (!ISignatureConstants.SIG_VOID.equals(unimplemented.getReturnTypeSignature())) {
        bodyBuilder.append("return ");
      }
      bodyBuilder.append("newInvocationContext().getPort().").append(unimplemented.getElementName()).append('(');
      Iterator<IMethodParameterSourceBuilder> parameters = unimplemented.getParameters().iterator();
      if (parameters.hasNext()) {
        IMethodParameterSourceBuilder parameterSourceBuilder = parameters.next();
        bodyBuilder.append(parameterSourceBuilder.getElementName());
        while (parameters.hasNext()) {
          parameterSourceBuilder = parameters.next();
          bodyBuilder.append(", ");
          bodyBuilder.append(parameterSourceBuilder.getElementName());
        }
      }
      bodyBuilder.append(");");
      unimplemented.setBody(new RawSourceBuilder(bodyBuilder.toString()));
      builder.addMethod(unimplemented);
    }
  }

  protected String getBaseName() {
    String name = getEntityName();
    if (name.endsWith(ISdkProperties.SUFFIX_WS_CLIENT)) {
      name = name.substring(0, name.length() - ISdkProperties.SUFFIX_WS_CLIENT.length());
    }
    return name;
  }

  public String getUrlPropertyName() {
    return "jaxws." + getBaseName().toLowerCase() + ".url";
  }

  protected ITypeSourceBuilder createUrlPropertyType() {

    ITypeSourceBuilder urlProperty = new TypeSourceBuilder(getBaseName() + ISdkProperties.SUFFIX_WS_URL_PROPERTY);
    urlProperty.setFlags(Flags.AccPublic | Flags.AccStatic);
    urlProperty.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractStringConfigProperty));

    IMethodSourceBuilder getKey = MethodSourceBuilderFactory.createOverride(urlProperty, getPackageName(), getJavaEnvironment(), "getKey");
    getKey.setBody(new RawSourceBuilder("return " + CoreUtils.toStringLiteral(getUrlPropertyName()) + ';'));
    urlProperty.addMethod(getKey);

    return urlProperty;
  }

  protected IMethodSourceBuilder createGetConfiguredEndpointUrlProperty(ITypeSourceBuilder wsClientType, ITypeSourceBuilder propertyType) {
    IMethodSourceBuilder getConfiguredEndpointUrlProperty = MethodSourceBuilderFactory.createOverride(wsClientType, getPackageName(), getJavaEnvironment(), "getConfiguredEndpointUrlProperty");
    getConfiguredEndpointUrlProperty.setBody(MethodBodySourceBuilderFactory.createReturnClassReference(Signature.createTypeSignature(propertyType.getFullyQualifiedName())));
    getConfiguredEndpointUrlProperty.removeAnnotation(IScoutRuntimeTypes.ConfigProperty);
    return getConfiguredEndpointUrlProperty;
  }

  protected IMethodSourceBuilder createExecInstallHandlers(ITypeSourceBuilder wsClientType) {
    final IMethodSourceBuilder execInstallHandlers = MethodSourceBuilderFactory.createOverride(wsClientType, getPackageName(), getJavaEnvironment(), "execInstallHandlers");
    final String chainVarName = execInstallHandlers.getParameters().get(0).getElementName();
    execInstallHandlers.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(chainVarName).append(".add(").append(validator.useName(IScoutRuntimeTypes.BEANS)).append(".get(")
            .append(validator.useName(IScoutRuntimeTypes.LogHandler)).append(SuffixConstants.SUFFIX_STRING_class).append("));").append(lineDelimiter);
        source.append(chainVarName).append(".add(").append(validator.useName(IScoutRuntimeTypes.BEANS)).append(".get(")
            .append(validator.useName(IScoutRuntimeTypes.WsConsumerCorrelationIdHandler)).append(SuffixConstants.SUFFIX_STRING_class).append("));");
      }
    });
    execInstallHandlers.removeAnnotation(IScoutRuntimeTypes.ConfigOperation);
    return execInstallHandlers;
  }

  public String getPortTypeSignature() {
    return m_portTypeSignature;
  }

  public void setPortTypeSignature(String portTypeSignature) {
    m_portTypeSignature = portTypeSignature;
  }

  public String getServiceSignature() {
    return m_serviceSignature;
  }

  public void setServiceSignature(String serviceSignature) {
    m_serviceSignature = serviceSignature;
  }
}
