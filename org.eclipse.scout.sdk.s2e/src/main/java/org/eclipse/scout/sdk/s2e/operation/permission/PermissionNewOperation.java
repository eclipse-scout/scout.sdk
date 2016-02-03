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
package org.eclipse.scout.sdk.s2e.operation.permission;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.s.sourcebuilder.permission.PermissionSourceBuilder;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.jsoup.helper.Validate;

/**
 * <h3>{@link PermissionNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PermissionNewOperation implements IOperation {

  // in
  private String m_permissionName;
  private IPackageFragmentRoot m_sharedSourceFolder;
  private String m_package;
  private IType m_superType;

  // out
  private IType m_createdPermission;

  @Override
  public String getOperationName() {
    return "Create Permission '" + getPermissionName() + "'.";
  }

  @Override
  public void validate() {
    Validate.isTrue(StringUtils.isNotBlank(getPermissionName()), "No permission name provided");
    Validate.isTrue(S2eUtils.exists(getSharedSourceFolder()), "No source folder provided");
    Validate.isTrue(StringUtils.isNotBlank(getPackage()), "No package name provided");
    Validate.isTrue(S2eUtils.exists(getSuperType()), "No supertype provided");
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 1);

    setCreatedPermission(createPermission(progress.newChild(1), workingCopyManager));
  }

  protected IType createPermission(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    PermissionSourceBuilder psb = new PermissionSourceBuilder(getPermissionName(), getPackage());
    psb.setup();
    psb.getMainType().setSuperTypeSignature(Signature.createTypeSignature(getSuperType().getFullyQualifiedName()));
    return S2eUtils.writeType(getSharedSourceFolder(), psb, monitor, workingCopyManager);
  }

  public IType getCreatedPermission() {
    return m_createdPermission;
  }

  protected void setCreatedPermission(IType createdPermission) {
    m_createdPermission = createdPermission;
  }

  public String getPermissionName() {
    return m_permissionName;
  }

  public void setPermissionName(String permissionName) {
    m_permissionName = permissionName;
  }

  public IPackageFragmentRoot getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IPackageFragmentRoot sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String package1) {
    m_package = package1;
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
  }

}
