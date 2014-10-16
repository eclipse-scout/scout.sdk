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
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.TypeNewOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.TypeNewWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.TypeNewWizardPage.JavaSearchScopeFactory;

public class TypeNewWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;
  private String m_elementName;
  private String m_typeSuffix;
  private String m_recommendedPackageFragment;
  private boolean m_allowModifyPackage;
  private boolean m_allowModifySuperType;
  private boolean m_allowModifyInterfaceType;
  private JavaSearchScopeFactory m_superTypeSearchScopeFactory;
  private JavaSearchScopeFactory m_interfaceTypeSearchScopeFactory;
  private IType m_superType;
  private IType m_interfaceType;

  private TypeNewWizardPage m_page;

  private TypeNewOperation m_op;

  public TypeNewWizard(IScoutBundle bundle, String elementName) {
    m_bundle = bundle;
    m_elementName = elementName;
    setWindowTitle(Texts.get("CreateNewX", m_elementName));
  }

  @Override
  public void addPages() {
    m_page = new TypeNewWizardPage(m_bundle, m_elementName);
    m_page.setAllowModifyInterfaceType(isAllowModifyInterfaceType());
    m_page.setAllowModifySuperType(isAllowModifySuperType());
    m_page.setAllowModifyPackage(isAllowModifyPackage());
    m_page.setRecommendedPackageFragment(getRecommendedPackageFragment());
    m_page.setTypeSuffix(getTypeSuffix());
    if (getSuperTypeSearchScopeFactory() != null) {
      m_page.setSuperTypeSearchScopeFactory(getSuperTypeSearchScopeFactory());
    }
    else {
      m_page.setSuperTypeSearchScopeFactory(new JavaSearchScopeFactory());
    }
    if (getInterfaceTypeSearchScopeFactory() != null) {
      m_page.setInterfaceTypeSearchScopeFactory(getInterfaceTypeSearchScopeFactory());
    }
    else {
      m_page.setInterfaceTypeSearchScopeFactory(new JavaSearchScopeFactory());
    }
    if (getSuperType() != null) {
      m_page.setSuperType(getSuperType());
    }
    m_page.setInterfaceType(getInterfaceType());

    addPage(m_page);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_op = new TypeNewOperation();
    m_op.setBundle(m_bundle);
    m_op.setPackageName(m_page.getPackageName());
    m_op.setTypeName(m_page.getTypeName());
    m_op.setInterfaceType(m_page.getInterfaceType());
    m_op.setSuperType(m_page.getSuperType());
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    m_op.run(monitor, workingCopyManager);
    return true;
  }

  public String getTypeSuffix() {
    return m_typeSuffix;
  }

  public void setTypeSuffix(String typeSuffix) {
    m_typeSuffix = typeSuffix;
  }

  public String getRecommendedPackageFragment() {
    return m_recommendedPackageFragment;
  }

  public void setRecommendedPackageFragment(String recommendedPackageFragment) {
    m_recommendedPackageFragment = recommendedPackageFragment;
  }

  public boolean isAllowModifyPackage() {
    return m_allowModifyPackage;
  }

  public void setAllowModifyPackage(boolean allowModifyPackage) {
    m_allowModifyPackage = allowModifyPackage;
  }

  public boolean isAllowModifySuperType() {
    return m_allowModifySuperType;
  }

  public void setAllowModifySuperType(boolean allowModifySuperType) {
    m_allowModifySuperType = allowModifySuperType;
  }

  public boolean isAllowModifyInterfaceType() {
    return m_allowModifyInterfaceType;
  }

  public void setAllowModifyInterfaceType(boolean allowModifyInterfaceType) {
    m_allowModifyInterfaceType = allowModifyInterfaceType;
  }

  public JavaSearchScopeFactory getSuperTypeSearchScopeFactory() {
    return m_superTypeSearchScopeFactory;
  }

  public void setSuperTypeSearchScopeFactory(JavaSearchScopeFactory superTypeSearchScopeFactory) {
    m_superTypeSearchScopeFactory = superTypeSearchScopeFactory;
  }

  public JavaSearchScopeFactory getInterfaceTypeSearchScopeFactory() {
    return m_interfaceTypeSearchScopeFactory;
  }

  public void setInterfaceTypeSearchScopeFactory(JavaSearchScopeFactory interfaceTypeSearchScopeFactory) {
    m_interfaceTypeSearchScopeFactory = interfaceTypeSearchScopeFactory;
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
  }

  public IType getInterfaceType() {
    return m_interfaceType;
  }

  public void setInterfaceType(IType interfaceType) {
    m_interfaceType = interfaceType;
  }
}
