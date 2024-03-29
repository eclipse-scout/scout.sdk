/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.util;

import java.util.Objects;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * <h3>{@link PackageContainer}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings({"NonFinalFieldReferencedInHashCode", "NonFinalFieldReferenceInEquals"})
public class PackageContainer {
  private IPackageFragmentRoot m_srcFolder;
  private IPackageFragment m_package;
  private IJavaProject m_project;

  public IPackageFragmentRoot getSrcFolder() {
    return m_srcFolder;
  }

  protected void setSrcFolder(IPackageFragmentRoot srcFolder) {
    m_srcFolder = srcFolder;
  }

  public IPackageFragment getPackage() {
    return m_package;
  }

  protected void setPackage(IPackageFragment package1) {
    m_package = package1;
  }

  public IJavaProject getProject() {
    return m_project;
  }

  protected void setProject(IJavaProject project) {
    m_project = project;
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = 1;
    result = prime * result + ((m_package == null) ? 0 : m_package.hashCode());
    result = prime * result + ((m_project == null) ? 0 : m_project.hashCode());
    result = prime * result + ((m_srcFolder == null) ? 0 : m_srcFolder.hashCode());
    return result;
  }

  @Override
  @SuppressWarnings("pmd:NPathComplexity")
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    var other = (PackageContainer) obj;
    return Objects.equals(m_package, other.m_package)
        && Objects.equals(m_project, other.m_project)
        && Objects.equals(m_srcFolder, other.m_srcFolder);
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append("PackageContainer [src_folder=").append(toStringElement(m_srcFolder))
        .append(", package=").append(toStringElement(m_package))
        .append(", project=").append(toStringElement(m_project)).append(']');
    return builder.toString();
  }

  private static String toStringElement(IJavaElement e) {
    if (e == null) {
      return "";
    }
    return e.getElementName();
  }
}
