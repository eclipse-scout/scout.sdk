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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

import com.google.common.base.Objects;

/**
 * <h3>{@link StrictHierarchyTypeContentProvider}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class StrictHierarchyTypeContentProvider extends AbstractContentProviderAdapter {

  private String m_baseClassFqn;
  private IJavaProject m_javaProject;
  private IFilter<IType> m_typeProposalFilter;

  public StrictHierarchyTypeContentProvider(IJavaProject javaProject, String baseClassFqn) {
    setJavaProject(javaProject);
    setBaseClassFqn(baseClassFqn);
  }

  @Override
  protected Collection<? extends Object> loadProposals(IProgressMonitor monitor) {
    IJavaProject javaProject = getJavaProject();
    if (!S2eUtils.exists(javaProject)) {
      return Collections.emptyList();
    }

    try {
      return S2eUtils.findClassesInStrictHierarchy(javaProject, getBaseClassFqn(), monitor, getTypeProposalFilter());
    }
    catch (CoreException e) {
      SdkLog.error("Error loading super type proposals in project {} for base class {}", javaProject.getElementName(), getBaseClassFqn(), e);
      return Collections.emptyList();
    }
  }

  @Override
  public String getText(Object element) {
    return ((IType) element).getElementName();
  }

  @Override
  public String getTextSelected(Object element) {
    IType t = (IType) element;
    StringBuilder sb = new StringBuilder(t.getElementName());
    String elementName = t.getPackageFragment().getElementName();
    if (StringUtils.isNotBlank(elementName)) {
      sb.append(" - ").append(elementName);
    }
    return sb.toString();
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public void setJavaProject(IJavaProject javaProject) {
    if (Objects.equal(javaProject, getJavaProject())) {
      return;
    }

    m_javaProject = javaProject;
    clearCache();
  }

  public String getBaseClassFqn() {
    return m_baseClassFqn;
  }

  public void setBaseClassFqn(String baseClassFqn) {
    if (Objects.equal(baseClassFqn, getBaseClassFqn())) {
      return;
    }
    m_baseClassFqn = baseClassFqn;
    clearCache();
  }

  public IFilter<IType> getTypeProposalFilter() {
    return m_typeProposalFilter;
  }

  public void setTypeProposalFilter(IFilter<IType> typeProposalFilter) {
    if (Objects.equal(typeProposalFilter, getTypeProposalFilter())) {
      return;
    }
    m_typeProposalFilter = typeProposalFilter;
    clearCache();
  }
}
