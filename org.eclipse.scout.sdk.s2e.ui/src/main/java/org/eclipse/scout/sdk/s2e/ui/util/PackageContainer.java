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
package org.eclipse.scout.sdk.s2e.ui.util;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * <h3>{@link PackageContainer}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
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
    final int prime = 31;
    int result = 1;
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
    PackageContainer other = (PackageContainer) obj;
    if (m_package == null) {
      if (other.m_package != null) {
        return false;
      }
    }
    else if (!m_package.equals(other.m_package)) {
      return false;
    }
    if (m_project == null) {
      if (other.m_project != null) {
        return false;
      }
    }
    else if (!m_project.equals(other.m_project)) {
      return false;
    }
    if (m_srcFolder == null) {
      if (other.m_srcFolder != null) {
        return false;
      }
    }
    else if (!m_srcFolder.equals(other.m_srcFolder)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
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