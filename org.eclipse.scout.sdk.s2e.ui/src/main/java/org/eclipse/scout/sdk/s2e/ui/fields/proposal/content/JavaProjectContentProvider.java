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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link JavaProjectContentProvider}</h3>
 *
 * @since 5.2.0
 */
public class JavaProjectContentProvider extends AbstractContentProviderAdapter {

  private Predicate<IJavaProject> m_filter;

  @Override
  public String getText(Object element) {
    return ((IJavaElement) element).getElementName();
  }

  @Override
  protected Collection<?> loadProposals(IProgressMonitor monitor) {
    try {
      var allJavaProjectsInWorkspace = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
      Collection<IJavaProject> allMavenJavaProjects = new ArrayList<>(allJavaProjectsInWorkspace.length);

      for (var candidate : allJavaProjectsInWorkspace) {
        if (monitor.isCanceled()) {
          return emptyList();
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
    return emptyList();
  }

  protected boolean accepts(IJavaProject jp) throws CoreException {
    if (!JdtUtils.exists(jp)) {
      return false;
    }
    var project = jp.getProject();
    if (!project.isAccessible()) {
      return false;
    }
    if (!project.hasNature(org.eclipse.m2e.core.internal.IMavenConstants.NATURE_ID)) {
      return false;
    }
    var pom = project.getFile(IMavenConstants.POM);
    if (pom == null || !pom.isAccessible()) {
      return false;
    }

    var filter = getFilter();
    return filter == null || filter.test(jp);
  }

  public Predicate<IJavaProject> getFilter() {
    return m_filter;
  }

  public void setFilter(Predicate<IJavaProject> filter) {
    m_filter = filter;
  }
}
