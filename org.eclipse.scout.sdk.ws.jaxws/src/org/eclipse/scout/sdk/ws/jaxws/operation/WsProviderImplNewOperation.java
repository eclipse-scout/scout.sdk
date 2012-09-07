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

import javax.jws.WebService;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodCreateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class WsProviderImplNewOperation implements IOperation {

  private IScoutBundle m_bundle;
  private String m_typeName;
  private String m_packageName;
  private IType m_portTypeInterfaceType;
  private boolean m_createScoutWebServiceAnnotation;
  private String m_sessionFactoryQName;
  private String m_authenticationHandlerQName;
  private String m_credentialValidationStrategyQName;

  private IType m_createdType;

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("bundle not set");
    }
    if (m_typeName == null) {
      throw new IllegalArgumentException("typeName not set");
    }
    if (m_packageName == null) {
      throw new IllegalArgumentException("packageName not set");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ScoutTypeNewOperation implNewTypeOp = new ScoutTypeNewOperation(m_typeName, m_packageName, m_bundle);
    if (TypeUtility.exists(m_portTypeInterfaceType)) {
      implNewTypeOp.addInterfaceSignature(Signature.createTypeSignature(m_portTypeInterfaceType.getFullyQualifiedName(), true));
    }
    else {
      JaxWsSdk.logError("Could not link webservice provider to port type as port type could not be found");
    }
    implNewTypeOp.run(monitor, workingCopyManager);
    m_createdType = implNewTypeOp.getCreatedType();
    workingCopyManager.register(m_createdType.getCompilationUnit(), monitor);
    if (TypeUtility.exists(m_portTypeInterfaceType)) {
      // override methods
      for (IMethod method : m_portTypeInterfaceType.getMethods()) {
        MethodCreateOperation op = new MethodCreateOperation(m_createdType, method.getElementName());
        op.setReturnTypeSignature(SignatureUtility.getReturnTypeSignatureResolved(method, m_portTypeInterfaceType));
        op.setMethodFlags(method.getFlags());
        op.setExceptionSignatures(method.getExceptionTypes());
        String[] paramNames = method.getParameterNames();
        op.setParameterNames(paramNames);
        op.setParameterSignatures(SignatureUtility.getMethodParameterSignatureResolved(method, m_portTypeInterfaceType));
        op.addAnnotation(new AnnotationCreateOperation(null, Signature.createTypeSignature(Override.class.getName(), true)));
        op.run(monitor, workingCopyManager);

        JavaElementFormatOperation wellFormOp = new JavaElementFormatOperation(op.getCreatedMethod(), true);
        wellFormOp.validate();
        wellFormOp.run(monitor, workingCopyManager);
      }
    }

    // create JAX-WS webservice annotation
    AnnotationUpdateOperation annotationOp = new AnnotationUpdateOperation();
    annotationOp.setDeclaringType(m_createdType);
    annotationOp.setAnnotationType(TypeUtility.getType(WebService.class.getName()));

    if (TypeUtility.exists(m_portTypeInterfaceType)) {
      annotationOp.addStringProperty("endpointInterface", m_portTypeInterfaceType.getFullyQualifiedName());
    }
    annotationOp.validate();
    annotationOp.run(monitor, workingCopyManager);

    // create ScoutWebService annotation
    if (m_createScoutWebServiceAnnotation) {
      annotationOp = new AnnotationUpdateOperation();
      annotationOp.setDeclaringType(m_createdType);
      annotationOp.setAnnotationType(TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService));

      String defaultSessionFactory = (String) TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getMethod(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY, new String[0]).getDefaultValue().getValue();
      String defaultAuthFactory = (String) TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getMethod(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER, new String[0]).getDefaultValue().getValue();
      String defaultCredentialFactory = (String) TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getMethod(JaxWsRuntimeClasses.PROP_SWS_CREDENTIAL_STRATEGY, new String[0]).getDefaultValue().getValue();

      if (m_sessionFactoryQName != null && !isSameType(m_sessionFactoryQName, defaultSessionFactory)) {
        IType type = createType(m_sessionFactoryQName, TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory), monitor, workingCopyManager);
        annotationOp.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY, type);
      }
      if (m_authenticationHandlerQName != null && !isSameType(m_authenticationHandlerQName, defaultAuthFactory)) {
        IType type = createType(m_authenticationHandlerQName, TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerProvider), monitor, workingCopyManager);
        annotationOp.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER, type);
      }
      if (m_credentialValidationStrategyQName != null && !isSameType(m_credentialValidationStrategyQName, defaultCredentialFactory)) {
        IType type = createType(m_credentialValidationStrategyQName, TypeUtility.getType(JaxWsRuntimeClasses.ICredentialValidationStrategy), monitor, workingCopyManager);
        annotationOp.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_CREDENTIAL_STRATEGY, type);
      }

      annotationOp.validate();
      annotationOp.run(monitor, workingCopyManager);
    }

    // format icu
    ICompilationUnit icu = m_createdType.getCompilationUnit();
    Document icuDoc = new Document(icu.getBuffer().getContents());

    SourceFormatOperation sourceFormatOp = new SourceFormatOperation(m_createdType.getJavaProject(), icuDoc, null);
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

      ScoutTypeNewOperation newTypeOp = new ScoutTypeNewOperation(typeName, packageName, m_bundle);
      newTypeOp.addInterfaceSignature(Signature.createTypeSignature(interfaceType.getFullyQualifiedName(), true));
      newTypeOp.run(monitor, workingCopyManager);
      type = newTypeOp.getCreatedType();
      workingCopyManager.register(type.getCompilationUnit(), monitor);
    }
    return type;
  }

  @Override
  public String getOperationName() {
    return WsProviderImplNewOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public String getPackageName() {
    return m_packageName;
  }

  public void setPackageName(String packageName) {
    m_packageName = packageName;
  }

  public boolean isCreateScoutWebserviceAnnotation() {
    return m_createScoutWebServiceAnnotation;
  }

  public void setCreateScoutWebServiceAnnotation(boolean createScoutWebServiceAnnotation) {
    m_createScoutWebServiceAnnotation = createScoutWebServiceAnnotation;
  }

  public String getSessionFactoryQName() {
    return m_sessionFactoryQName;
  }

  public void setSessionFactoryQName(String sessionFactoryQName) {
    m_sessionFactoryQName = sessionFactoryQName;
  }

  public String getAuthenticationHandlerQName() {
    return m_authenticationHandlerQName;
  }

  public void setAuthenticationHandlerQName(String authenticationHandlerQName) {
    m_authenticationHandlerQName = authenticationHandlerQName;
  }

  public String getCredentialValidationStrategyQName() {
    return m_credentialValidationStrategyQName;
  }

  public void setCredentialValidationStrategyQName(String credentialValidationStrategyQName) {
    m_credentialValidationStrategyQName = credentialValidationStrategyQName;
  }

  public boolean isCreateScoutWebServiceAnnotation() {
    return m_createScoutWebServiceAnnotation;
  }

  public void setCreatedType(IType createdType) {
    m_createdType = createdType;
  }

  public IType getCreatedType() {
    return m_createdType;
  }

  public IType getPortTypeInterfaceType() {
    return m_portTypeInterfaceType;
  }

  public void setPortTypeInterfaceType(IType portTypeInterfaceType) {
    m_portTypeInterfaceType = portTypeInterfaceType;
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
