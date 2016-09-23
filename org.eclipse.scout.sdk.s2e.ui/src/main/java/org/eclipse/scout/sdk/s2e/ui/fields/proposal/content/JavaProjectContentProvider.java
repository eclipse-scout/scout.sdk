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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link JavaProjectContentProvider}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class JavaProjectContentProvider extends AbstractContentProviderAdapter {

  private Predicate<IJavaProject> m_filter;

  @Override
  public String getText(Object element) {
    return ((IJavaProject) element).getElementName();
  }

  @Override
  protected Collection<? extends Object> loadProposals(IProgressMonitor monitor) {
    try {
      IJavaProject[] allJavaProjectsInWorkspace = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
      Collection<IJavaProject> allMavenJavaProjects = new ArrayList<>(allJavaProjectsInWorkspace.length);

      for (IJavaProject candidate : allJavaProjectsInWorkspace) {
        if (monitor.isCanceled()) {
          return Collections.emptyList();
        }
        if (accepts(candidate)) {
          allMavenJavaProjects.add(candidate);
        }
      }
      return allMavenJavaProjects;
    }
    catch (CoreException e) {
      SdkLog.error("Unable to get all java projects in the current workspace.", e);
    }
    return Collections.emptyList();
  }

  protected boolean accepts(IJavaProject jp) throws CoreException {
    if (!S2eUtils.exists(jp)) {
      return false;
    }
    IProject project = jp.getProject();
    if (!project.isAccessible()) {
      return false;
    }
    if (!project.hasNature(org.eclipse.m2e.core.internal.IMavenConstants.NATURE_ID)) {
      return false;
    }
    IFile pom = project.getFile(IMavenConstants.POM);
    if (pom == null || !pom.isAccessible()) {
      return false;
    }

    Predicate<IJavaProject> filter = getFilter();
    return filter == null || filter.test(jp);
  }

  public Predicate<IJavaProject> getFilter() {
    return m_filter;
  }

  public void setFilter(Predicate<IJavaProject> filter) {
    m_filter = filter;
  }
}
