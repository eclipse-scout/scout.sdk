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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.ExpressionSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.AbstractEntitySourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link EntryPointDefinitionSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class EntryPointDefinitionSourceBuilder extends AbstractEntitySourceBuilder {

  private String m_portTypeFqn;
  private String m_serviceName;
  private String m_portName;
  private String m_entryPointPackage;
  private String m_entryPointName;

  public EntryPointDefinitionSourceBuilder(String entityName, String packageName, IJavaEnvironment env) {
    super(entityName, packageName, env);
  }

  @Override
  public void setup() {
    setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));
    TypeSourceBuilder typeBuilder = new TypeSourceBuilder(getEntityName());
    typeBuilder.setFlags(Flags.AccPublic | Flags.AccInterface);
    addType(typeBuilder);

    AnnotationSourceBuilder webServiceEntryPointBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.WebServiceEntryPoint);
    webServiceEntryPointBuilder.putElement(JaxWsUtils.ENTRY_POINT_DEFINITION_ENDPOINTINTERFACE_ATTRIBUTE, new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(validator.useName(getPortTypeFqn())).append(SuffixConstants.SUFFIX_STRING_class);
      }
    });
    webServiceEntryPointBuilder.putElement(JaxWsUtils.ENTRY_POINT_DEFINITION_PACKAGE_ATTRIBUTE, CoreUtils.toStringLiteral(getEntryPointPackage()));
    webServiceEntryPointBuilder.putElement(JaxWsUtils.ENTRY_POINT_DEFINITION_NAME_ATTRIBUTE, CoreUtils.toStringLiteral(getEntryPointName()));
    webServiceEntryPointBuilder.putElement("serviceName", CoreUtils.toStringLiteral(getServiceName()));
    webServiceEntryPointBuilder.putElement("portName", CoreUtils.toStringLiteral(getPortName()));
    Collection<ISourceBuilder> handlers = new ArrayList<>(2);
    handlers.add(createHandlerBuilder(IScoutRuntimeTypes.WsProviderCorrelationIdHandler));
    handlers.add(createHandlerBuilder(IScoutRuntimeTypes.LogHandler));
    webServiceEntryPointBuilder.putElement(JaxWsUtils.ENTRY_POINT_DEFINITION_HANDLER_CHAIN_ATTRIBUTE, ExpressionSourceBuilderFactory.createArray(handlers, true));
    webServiceEntryPointBuilder.putElement(JaxWsUtils.ENTRY_POINT_DEFINITION_AUTH_ATTRIBUTE, createAuthenticationBuilder());
    typeBuilder.addAnnotation(webServiceEntryPointBuilder);
  }

  protected IAnnotationSourceBuilder createAuthenticationBuilder() {
    IAnnotationSourceBuilder authentication = new AnnotationSourceBuilder(IScoutRuntimeTypes.Authentication);
    authentication.putElement("method", createClazzBuilder(IScoutRuntimeTypes.BasicAuthenticationMethod));
    authentication.putElement("verifier", createClazzBuilder(IScoutRuntimeTypes.ConfigFileCredentialVerifier));
    return authentication;
  }

  protected IAnnotationSourceBuilder createClazzBuilder(final String clazzFqn) {
    IAnnotationSourceBuilder clazz = new AnnotationSourceBuilder(IScoutRuntimeTypes.Clazz);
    clazz.putElement("value", new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(validator.useName(clazzFqn)).append(SuffixConstants.SUFFIX_STRING_class);
      }
    });
    return clazz;
  }

  protected IAnnotationSourceBuilder createHandlerBuilder(String handlerFqn) {
    IAnnotationSourceBuilder handlerBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.Handler);
    handlerBuilder.putElement("value", createClazzBuilder(handlerFqn));
    return handlerBuilder;
  }

  public String getPortTypeFqn() {
    return m_portTypeFqn;
  }

  public void setPortTypeFqn(String portTypeFqn) {
    m_portTypeFqn = portTypeFqn;
  }

  public String getEntryPointPackage() {
    return m_entryPointPackage;
  }

  public void setEntryPointPackage(String entryPointPackage) {
    m_entryPointPackage = entryPointPackage;
  }

  public String getServiceName() {
    return m_serviceName;
  }

  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
  }

  public String getPortName() {
    return m_portName;
  }

  public void setPortName(String portName) {
    m_portName = portName;
  }

  public String getEntryPointName() {
    return m_entryPointName;
  }

  public void setEntryPointName(String entryPointName) {
    m_entryPointName = entryPointName;
  }
}
