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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class TypeNewOperation implements IOperation {

  private IScoutBundle m_bundle;
  private String m_typeName;
  private String m_packageName;
  private IType m_superType;
  private IType m_interfaceType;
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
    ScoutTypeNewOperation opType = new ScoutTypeNewOperation(m_typeName, m_packageName, m_bundle);

    if (m_superType != null) {
      opType.setSuperTypeSignature(Signature.createTypeSignature(m_superType.getFullyQualifiedName(), true));
    }
    if (m_interfaceType != null) {
      opType.addInterfaceSignature(Signature.createTypeSignature(m_interfaceType.getFullyQualifiedName(), true));
    }
    opType.run(monitor, workingCopyManager);
    m_createdType = opType.getCreatedType();
    workingCopyManager.register(m_createdType.getCompilationUnit(), monitor);

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

  @Override
  public String getOperationName() {
    return TypeNewOperation.class.getName();
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

  public void setSuperType(IType superType) {
    m_superType = superType;
  }

  public void setInterfaceType(IType interfaceType) {
    m_interfaceType = interfaceType;
  }
}
