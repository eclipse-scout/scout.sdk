/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.permission;

import java.util.function.BiConsumer;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link PermissionNewOperation}</h3>
 *
 * @since 7.0.0
 */
public class PermissionNewOperation implements BiConsumer<IEnvironment, IProgress> {

  // in
  private String m_permissionName;
  private IClasspathEntry m_sharedSourceFolder;
  private String m_package;
  private String m_superType;

  // out
  private IType m_createdPermission;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    progress.init(toString(), 1);
    setCreatedPermission(createPermission(env, progress.newChild(1)));
  }

  protected IType createPermission(IEnvironment env, IProgress progress) {
    Ensure.notBlank(getPermissionName(), "No permission name provided");
    Ensure.notBlank(getPackage(), "No package name provided");
    Ensure.notNull(getSharedSourceFolder(), "No source folder provided");
    Ensure.notNull(getSuperType(), "No supertype provided");

    PermissionGenerator<?> psb = new PermissionGenerator<>()
        .withElementName(getPermissionName())
        .withPackageName(getPackage())
        .withSuperClass(getSuperType());

    return env.writeCompilationUnit(psb, getSharedSourceFolder(), progress);
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

  public IClasspathEntry getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IClasspathEntry sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String package1) {
    m_package = package1;
  }

  public String getSuperType() {
    return m_superType;
  }

  public void setSuperType(String superType) {
    m_superType = superType;
  }

  @Override
  public String toString() {
    return "Create new Permission";
  }
}
