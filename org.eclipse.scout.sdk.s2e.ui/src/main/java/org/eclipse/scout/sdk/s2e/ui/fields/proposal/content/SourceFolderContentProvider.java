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
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link SourceFolderContentProvider}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class SourceFolderContentProvider extends AbstractContentProviderAdapter {

  private final IFilter<IJavaElement> m_projectFilter;
  private final IFilter<IPackageFragmentRoot> m_sourceFolderFilter;

  public SourceFolderContentProvider(IFilter<IJavaElement> projectFilter) {
    m_projectFilter = projectFilter;
    m_sourceFolderFilter = new IFilter<IPackageFragmentRoot>() {
      @Override
      public boolean evaluate(IPackageFragmentRoot element) {
        String srcFolderName = element.getPath().removeFirstSegments(1).toString().toLowerCase();
        return ISdkProperties.GENERATED_SOURCE_FOLDER_NAME.equals(srcFolderName)
            || !srcFolderName.contains("generated");
      }
    };
  }

  @Override
  protected Collection<? extends Object> loadProposals(IProgressMonitor monitor) {
    try {
      IJavaProject[] javaProjects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
      List<IJavaProject> relevantProjects = new ArrayList<>();
      for (IJavaProject jp : javaProjects) {
        if (monitor.isCanceled()) {
          return Collections.emptyList();
        }

        if (m_projectFilter == null || m_projectFilter.evaluate(jp)) {
          relevantProjects.add(jp);
        }
      }
      if (monitor.isCanceled()) {
        return Collections.emptyList();
      }
      return S2eUtils.getSourceFolders(relevantProjects, m_sourceFolderFilter, monitor);
    }
    catch (JavaModelException | SdkException e) {
      SdkLog.error("Error while calculating possible source folders", e);
      return Collections.emptySet();
    }
  }

  @Override
  public String getText(Object element) {
    IPackageFragmentRoot root = (IPackageFragmentRoot) element;
    return root.getJavaProject().getElementName() + '/' + root.getPath().removeFirstSegments(1).toString();
  }
}
