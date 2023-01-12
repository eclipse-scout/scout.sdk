/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal.content;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;

import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link PackageContentProvider}</h3>
 *
 * @since 5.2.0
 */
public class PackageContentProvider extends AbstractContentProviderAdapter {

  private IJavaProject m_javaProject;

  public PackageContentProvider(IJavaProject jp) {
    setJavaProject(jp);
  }

  @Override
  public String getText(Object element) {
    return ((IJavaElement) element).getElementName();
  }

  @Override
  protected Collection<Object> loadProposals(IProgressMonitor monitor) {
    var javaProject = getJavaProject();
    if (!JdtUtils.exists(javaProject)) {
      return emptyList();
    }

    Collection<Object> ret = new TreeSet<>(comparing(this::getText));
    try {
      var packageFragments = javaProject.getPackageFragments();
      for (var pck : packageFragments) {
        if (monitor.isCanceled()) {
          return ret;
        }

        if (pck.getKind() == IPackageFragmentRoot.K_SOURCE) {
          var packageName = pck.getElementName();
          if (Strings.hasText(packageName)) {
            ret.add(pck);
          }
        }
      }
    }
    catch (JavaModelException e1) {
      SdkLog.error("Error while calculating the package proposals for project {}", javaProject, e1);
    }
    return ret;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public void setJavaProject(IJavaProject javaProject) {
    if (Objects.equals(javaProject, getJavaProject())) {
      return;
    }

    m_javaProject = javaProject;
    clearCache();
  }
}
