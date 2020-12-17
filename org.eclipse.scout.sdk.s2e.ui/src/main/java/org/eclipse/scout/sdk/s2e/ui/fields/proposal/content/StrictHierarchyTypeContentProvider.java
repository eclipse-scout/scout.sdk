/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal.content;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link StrictHierarchyTypeContentProvider}</h3>
 *
 * @since 5.2.0
 */
public class StrictHierarchyTypeContentProvider extends AbstractContentProviderAdapter {

  private String m_baseClassFqn;
  private IJavaProject m_javaProject;
  private Predicate<IType> m_typeProposalFilter;

  public StrictHierarchyTypeContentProvider(IJavaProject javaProject, String baseClassFqn) {
    setJavaProject(javaProject);
    setBaseClassFqn(baseClassFqn);
  }

  @Override
  protected Collection<?> loadProposals(IProgressMonitor monitor) {
    var javaProject = getJavaProject();
    if (!JdtUtils.exists(javaProject)) {
      return emptyList();
    }

    try {
      return JdtUtils.findTypesInStrictHierarchy(javaProject, getBaseClassFqn(), monitor, getTypeProposalFilter());
    }
    catch (RuntimeException e) {
      SdkLog.error("Error loading super type proposals in project {} for base class {}", javaProject.getElementName(), getBaseClassFqn(), e);
      return emptyList();
    }
  }

  @Override
  public String getText(Object element) {
    return ((IJavaElement) element).getElementName();
  }

  @Override
  public String getTextSelected(Object element) {
    var t = (IType) element;
    var sb = new StringBuilder(t.getElementName());
    var elementName = t.getPackageFragment().getElementName();
    if (Strings.hasText(elementName)) {
      sb.append(" - ").append(elementName);
    }
    return sb.toString();
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

  public String getBaseClassFqn() {
    return m_baseClassFqn;
  }

  public void setBaseClassFqn(String baseClassFqn) {
    if (Objects.equals(baseClassFqn, getBaseClassFqn())) {
      return;
    }
    m_baseClassFqn = baseClassFqn;
    clearCache();
  }

  public Predicate<IType> getTypeProposalFilter() {
    return m_typeProposalFilter;
  }

  public void setTypeProposalFilter(Predicate<IType> typeProposalFilter) {
    if (Objects.equals(typeProposalFilter, getTypeProposalFilter())) {
      return;
    }
    m_typeProposalFilter = typeProposalFilter;
    clearCache();
  }
}
