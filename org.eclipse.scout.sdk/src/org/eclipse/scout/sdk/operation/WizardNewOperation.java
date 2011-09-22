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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class WizardNewOperation implements IOperation {

  private IScoutBundle m_clientBundle;
  private String m_typeName;
  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private boolean m_formatSource;

  // operation member
  private IType m_createdWizard;

  @Override
  public String getOperationName() {
    return "Create wizard '" + getTypeName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getClientBundle() == null) {
      throw new IllegalArgumentException("client bundle can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getClientBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_WIZARDS), getClientBundle());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.run(monitor, workingCopyManager);
    m_createdWizard = newOp.getCreatedType();
    workingCopyManager.register(m_createdWizard.getCompilationUnit(), monitor);
    // create constructor
    ConstructorCreateOperation constructorOp = new ConstructorCreateOperation(getCreatedWizard(), false);
    constructorOp.setSimpleBody("super();");
    constructorOp.setMethodFlags(Flags.AccPublic);
    constructorOp.validate();
    constructorOp.run(monitor, workingCopyManager);
    // nls text
    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(getCreatedWizard(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TITLE, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }

    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{getCreatedWizard().getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);

    if (m_formatSource) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedWizard(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedWizard() {
    return m_createdWizard;
  }

  public void setClientBundle(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
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
