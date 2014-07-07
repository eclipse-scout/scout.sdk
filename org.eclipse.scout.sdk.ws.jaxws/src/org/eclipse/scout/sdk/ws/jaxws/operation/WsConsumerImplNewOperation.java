/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.operation;

import javax.xml.ws.Service;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class WsConsumerImplNewOperation extends ServiceNewOperation {
  /**
   * @param serviceInterfaceName
   * @param serviceName
   */
  public WsConsumerImplNewOperation(String serviceInterfaceName, String serviceName) {
    super(serviceInterfaceName, serviceName);
    setFormatSource(true);
  }

  // used for generic super type
  private IType m_jaxWsServiceType;
  // used for generic super type
  private IType m_jaxWsPortType;

  private boolean m_createScoutWebServiceAnnotation;
  private String m_authenticationHandlerQName;

  @Override
  public void validate() {
  }

  @Override
  public void run(final IProgressMonitor monitor, final IWorkingCopyManager workingCopyManager) throws CoreException {
    // assemble supertype signature
    IType jaxWsPortType = null;
    if (TypeUtility.exists(getJaxWsPortType())) {
      jaxWsPortType = getJaxWsPortType();
    }
    else {
      jaxWsPortType = TypeUtility.getType(Object.class.getName());
      JaxWsSdk.logError("Could not link webservice consumer to port type as port type could not be found");
    }
    IType jaxWsServiceType = null;
    if (TypeUtility.exists(getJaxWsServiceType())) {
      jaxWsServiceType = getJaxWsServiceType();
    }
    else {
      jaxWsServiceType = TypeUtility.getType(Service.class.getName());
      JaxWsSdk.logError("Could not link webservice consumer to service as service could not be found");
    }

    String superTypeSignature = SignatureCache.createTypeSignature(JaxWsRuntimeClasses.AbstractWebServiceClient + "<" + jaxWsServiceType.getFullyQualifiedName() + ", " + jaxWsPortType.getFullyQualifiedName() + ">");
    setImplementationSuperTypeSignature(superTypeSignature);

    if (m_createScoutWebServiceAnnotation) {
      final String defaultAuthFactory = (String) TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebServiceClient).getMethod(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER, new String[0]).getDefaultValue().getValue();
      AnnotationSourceBuilder scoutWebServiceClientAnnot = new AnnotationSourceBuilder(SignatureCache.createTypeSignature(JaxWsRuntimeClasses.ScoutWebServiceClient)) {
        @Override
        public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          if (m_authenticationHandlerQName != null && !isSameType(m_authenticationHandlerQName, defaultAuthFactory)) {
            IType type = createType(m_authenticationHandlerQName, TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerConsumer), monitor, workingCopyManager);
            addParameter(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER + "=" + validator.getTypeName(SignatureCache.createTypeSignature(type.getFullyQualifiedName())) + ".class");
          }
          super.createSource(source, lineDelimiter, ownerProject, validator);
        }
      };
      getImplementationSourceBuilder().addAnnotationSourceBuilder(scoutWebServiceClientAnnot);
    }

    super.run(monitor, workingCopyManager);
  }

  private IType createType(String qualifiedTypeName, IType interfaceType, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IType type;
    if (TypeUtility.existsType(qualifiedTypeName)) {
      type = TypeUtility.getType(qualifiedTypeName);
    }
    else {
      String typeName = Signature.getSimpleName(qualifiedTypeName);
      String packageName = Signature.getQualifier(qualifiedTypeName);

      PrimaryTypeNewOperation newTypeOp = new PrimaryTypeNewOperation(typeName, packageName, getImplementationProject());
      newTypeOp.setIcuCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
      newTypeOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
      newTypeOp.setFlags(Flags.AccPublic);
      newTypeOp.addInterfaceSignature(SignatureCache.createTypeSignature(interfaceType.getFullyQualifiedName()));
      newTypeOp.run(monitor, workingCopyManager);
      type = newTypeOp.getCreatedType();
      workingCopyManager.register(type.getCompilationUnit(), monitor);
    }
    return type;
  }

  @Override
  public String getOperationName() {
    return WsConsumerImplNewOperation.class.getName();
  }

  public boolean isCreateScoutWebserviceAnnotation() {
    return m_createScoutWebServiceAnnotation;
  }

  public void setCreateScoutWebServiceAnnotation(boolean createScoutWebServiceAnnotation) {
    m_createScoutWebServiceAnnotation = createScoutWebServiceAnnotation;
  }

  public String getAuthenticationHandlerQName() {
    return m_authenticationHandlerQName;
  }

  public void setAuthenticationHandlerQName(String authenticationHandlerQName) {
    m_authenticationHandlerQName = authenticationHandlerQName;
  }

  public boolean isCreateScoutWebServiceAnnotation() {
    return m_createScoutWebServiceAnnotation;
  }

  public IType getJaxWsServiceType() {
    return m_jaxWsServiceType;
  }

  public void setJaxWsServiceType(IType jaxWsServiceType) {
    m_jaxWsServiceType = jaxWsServiceType;
  }

  public IType getJaxWsPortType() {
    return m_jaxWsPortType;
  }

  public void setJaxWsPortType(IType jaxWsPortType) {
    m_jaxWsPortType = jaxWsPortType;
  }

  private boolean isSameType(String fullyQualifiedName1, String fullyQualifiedName2) {
    if (fullyQualifiedName1 != null) {
      fullyQualifiedName1 = fullyQualifiedName1.replaceAll("\\$", "."); // because of inner classes
    }
    if (fullyQualifiedName2 != null) {
      fullyQualifiedName2 = fullyQualifiedName2.replaceAll("\\$", "."); // because of inner classes
    }

    return CompareUtility.equals(fullyQualifiedName1, fullyQualifiedName2);
  }
}
