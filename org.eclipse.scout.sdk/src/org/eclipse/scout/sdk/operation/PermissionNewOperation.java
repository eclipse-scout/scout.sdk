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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.operation.field.FieldCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodCreateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>PermissionNewOperation</h3> ...
 */
public class PermissionNewOperation implements IOperation {

  private String m_typeName;
  private String m_superTypeSignature;
  private IScoutBundle m_sharedBundle;

  private IType m_createdType;
  private boolean m_formatSource;

  public PermissionNewOperation() {
    this(false);
  }

  public PermissionNewOperation(boolean formatSource) {
    m_formatSource = formatSource;
    m_superTypeSignature = Signature.createTypeSignature(RuntimeClasses.BasicPermission, true);
  }

  public String getOperationName() {
    return "New Permission...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getSharedBundle() == null) {
      throw new IllegalArgumentException("shared bundle can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name can not be null or empty.");
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getSharedBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SECURITY), getSharedBundle());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.run(monitor, workingCopyManager);
    m_createdType = newOp.getCreatedType();
    workingCopyManager.register(m_createdType.getCompilationUnit(), monitor);

    FieldCreateOperation serialVersionUIDOp = new FieldCreateOperation(getCreatedPermission(), "serialVersionUID", false);
    serialVersionUIDOp.setSignature(Signature.SIG_LONG);
    serialVersionUIDOp.setSimpleInitValue("0L");
    serialVersionUIDOp.setFlags(Flags.AccPrivate | Flags.AccFinal | Flags.AccStatic);
    serialVersionUIDOp.validate();
    serialVersionUIDOp.run(monitor, workingCopyManager);

    // constructor
    MethodCreateOperation constructorOp = new MethodCreateOperation(getCreatedPermission(), getTypeName(), "super(\"" + getTypeName().replaceAll(ScoutIdeProperties.SUFFIX_PERMISSION + "$", "") + "\");");
    constructorOp.setMethodFlags(Flags.AccPublic);
    constructorOp.validate();
    constructorOp.run(monitor, workingCopyManager);

    if (m_formatSource) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedPermission(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY, new IPackageFragment[]{m_createdType.getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);

  }

  public IType getCreatedPermission() {
    return m_createdType;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setSharedBundle(IScoutBundle sharedBundle) {
    m_sharedBundle = sharedBundle;
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
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
}
