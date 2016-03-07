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
package org.eclipse.scout.sdk.s2e.ui.fields.proposal.content;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link PackageContentProvider}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PackageContentProvider extends AbstractContentProviderAdapter {

  private IJavaProject m_javaProject;

  public PackageContentProvider(IJavaProject jp) {
    setJavaProject(jp);
  }

  @Override
  public String getText(Object element) {
    return ((IPackageFragment) element).getElementName();
  }

  @Override
  protected Collection<Object> loadProposals(IProgressMonitor monitor) {
    IJavaProject javaProject = getJavaProject();
    if (!S2eUtils.exists(javaProject)) {
      return Collections.emptyList();
    }

    Set<Object> ret = new TreeSet<>(new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        return getText(o1).compareTo(getText(o2));
      }
    });
    try {
      IPackageFragment[] packageFragments = javaProject.getPackageFragments();
      for (IPackageFragment pck : packageFragments) {
        if (monitor.isCanceled()) {
          return ret;
        }

        if (pck.getKind() == IPackageFragmentRoot.K_SOURCE) {
          String packageName = pck.getElementName();
          if (StringUtils.isNotBlank(packageName)) {
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
