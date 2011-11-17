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

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class HandlerNewOperation implements IOperation {

  private IScoutBundle m_bundle;
  private String m_typeName;
  private String m_packageName;
  private boolean m_transactional;
  private IType m_sessionFactoryType;
  private IType m_superType;
  private IType m_createdType;

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("bundle not set");
    }
    if (m_superType == null) {
      throw new IllegalArgumentException("superType not set");
    }
    if (m_typeName == null) {
      throw new IllegalArgumentException("typeName not set");
    }
    if (m_packageName == null) {
      throw new IllegalArgumentException("packageName not set");
    }
    if (m_transactional && m_sessionFactoryType == null) {
      throw new IllegalArgumentException("session factory must be set");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ScoutTypeNewOperation opType = new ScoutTypeNewOperation(m_typeName, m_packageName, m_bundle);

    String superTypeSignature;
    if (m_superType.getFullyQualifiedName().equals(LogicalHandler.class.getName())) {
      superTypeSignature = Signature.createTypeSignature(LogicalHandler.class.getName() + "<" + LogicalMessageContext.class.getName() + ">", false);
    }
    else if (m_superType.getFullyQualifiedName().equals(SOAPHandler.class.getName())) {
      superTypeSignature = Signature.createTypeSignature(SOAPHandler.class.getName() + "<" + SOAPMessageContext.class.getName() + ">", false);
    }
    else {
      superTypeSignature = Signature.createTypeSignature(m_superType.getFullyQualifiedName(), true);
    }
    if (m_superType.isInterface()) {
      opType.addInterfaceSignature(superTypeSignature);
    }
    else {
      opType.setSuperTypeSignature(superTypeSignature);
    }
    opType.run(monitor, workingCopyManager);
    m_createdType = opType.getCreatedType();
    workingCopyManager.register(m_createdType.getCompilationUnit(), monitor);

    if (m_transactional) {
      AnnotationUpdateOperation opAnnotation = new AnnotationUpdateOperation();
      opAnnotation.setDeclaringType(m_createdType);
      opAnnotation.setAnnotationType(JaxWsRuntimeClasses.ScoutTransaction);

      String defaultSessionFactory = (String) JaxWsRuntimeClasses.ScoutWebService.getMethod(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY, new String[0]).getDefaultValue().getValue();

      if (!isSameType(m_sessionFactoryType.getFullyQualifiedName(), defaultSessionFactory)) {
        opAnnotation.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY, m_sessionFactoryType);
      }

      opAnnotation.validate();
      opAnnotation.run(monitor, workingCopyManager);
    }

    JaxWsSdkUtility.overrideUnimplementedMethodsAsync(m_createdType);

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
    return HandlerNewOperation.class.getName();
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

  public IType getCreatedType() {
    return m_createdType;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public void setPackageName(String packageName) {
    m_packageName = packageName;
  }

  public void setTransactional(boolean transactional) {
    m_transactional = transactional;
  }

  public void setSessionFactoryType(IType sessionFactoryType) {
    m_sessionFactoryType = sessionFactoryType;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
  }
}
