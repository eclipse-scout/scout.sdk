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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class WsConsumerImplNewOperation extends ServiceNewOperation {
  /**
   * @param serviceInterfaceName
   * @param serviceName
   */
  public WsConsumerImplNewOperation(String serviceInterfaceName, String serviceName) {
    super(serviceInterfaceName, serviceName);
  }

  // used for generic super type
  private IType m_jaxWsServiceType;
  // used for generic super type
  private IType m_jaxWsPortType;

  private boolean m_createScoutWebServiceAnnotation;
  private String m_authenticationHandlerQName;

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
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

    String superTypeSignature = "<";
    superTypeSignature += Signature.toString(SignatureCache.createTypeSignature(jaxWsServiceType.getFullyQualifiedName()));
    superTypeSignature += ", ";
    superTypeSignature += Signature.toString(SignatureCache.createTypeSignature(jaxWsPortType.getFullyQualifiedName()));
    superTypeSignature += ">";
    superTypeSignature = SignatureCache.createTypeSignature(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient).getFullyQualifiedName() + superTypeSignature);
    setImplementationSuperTypeSignature(superTypeSignature);
    super.run(monitor, workingCopyManager);

    IType createdType = getCreatedServiceImplementation();

    // create import directives for generic types
    JaxWsSdkUtility.createImportDirective(createdType, jaxWsPortType);
    JaxWsSdkUtility.createImportDirective(createdType, jaxWsServiceType);

    // create ScoutWebService annotation
    if (m_createScoutWebServiceAnnotation) {
      AnnotationUpdateOperation annotationOp = new AnnotationUpdateOperation();
      annotationOp.setDeclaringType(createdType);
      annotationOp.setAnnotationType(TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebServiceClient));

      String defaultAuthFactory = (String) TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebServiceClient).getMethod(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER, new String[0]).getDefaultValue().getValue();
      // only add annotation property if different to default

      if (m_authenticationHandlerQName != null && !isSameType(m_authenticationHandlerQName, defaultAuthFactory)) {
        IType type = createType(m_authenticationHandlerQName, TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerConsumer), monitor, workingCopyManager);
        annotationOp.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER, type);
      }
      annotationOp.validate();
      annotationOp.run(monitor, workingCopyManager);
    }

    // format icu
    ICompilationUnit icu = createdType.getCompilationUnit();
    Document icuDoc = new Document(icu.getBuffer().getContents());

    SourceFormatOperation sourceFormatOp = new SourceFormatOperation(createdType.getJavaProject(), icuDoc, null);
    sourceFormatOp.run(monitor, workingCopyManager);

    // write document back
    icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(icuDoc.get(), icuDoc));

    // reconcilation
    workingCopyManager.reconcile(icu, monitor);
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
