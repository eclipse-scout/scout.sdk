/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class WizardNewOperation implements IOperation {

  private final String m_typeName;
  private final String m_packageName;
  private final IJavaProject m_javaProject;

  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private boolean m_formatSource;

  // operation member
  private IType m_createdWizard;

  public WizardNewOperation(String typeName, String packageName, IJavaProject javaProject) {
    m_typeName = typeName;
    m_packageName = packageName;
    m_javaProject = javaProject;

  }

  @Override
  public String getOperationName() {
    return "Create wizard '" + getTypeName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getJavaProject() == null) {
      throw new IllegalArgumentException("client bundle can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {

    PrimaryTypeNewOperation newOp = new PrimaryTypeNewOperation(getTypeName(), getPackageName(), getJavaProject());
    newOp.setFlags(Flags.AccPublic);
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.setPackageExportPolicy(ExportPolicy.AddPackage);
    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(newOp.getElementName());
    constructorBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("super();"));
    newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
    // nls method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsTextMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TITLE);
      nlsTextMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsTextMethodBuilder), nlsTextMethodBuilder);
    }
    newOp.setFormatSource(isFormatSource());
    newOp.run(monitor, workingCopyManager);
    m_createdWizard = newOp.getCreatedType();
    workingCopyManager.register(m_createdWizard.getCompilationUnit(), monitor);

  }

  public IType getCreatedWizard() {
    return m_createdWizard;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public String getPackageName() {
    return m_packageName;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

}
