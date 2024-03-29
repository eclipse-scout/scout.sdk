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
import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Predicate;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link SourceFolderContentProvider}</h3>
 *
 * @since 5.2.0
 */
public class SourceFolderContentProvider extends AbstractContentProviderAdapter {

  private final Predicate<IJavaElement> m_projectFilter;

  public SourceFolderContentProvider(Predicate<IJavaElement> projectFilter) {
    m_projectFilter = projectFilter;
  }

  @Override
  protected Collection<?> loadProposals(IProgressMonitor monitor) {
    try {
      var javaProjects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
      Collection<IPackageFragmentRoot> result = new ArrayList<>();
      for (var jp : javaProjects) {
        if (monitor.isCanceled()) {
          return emptyList();
        }

        if (m_projectFilter == null || m_projectFilter.test(jp)) {
          S2eUtils.sourceFoldersOrdered(jp)
              .filter(root -> {
                // filter generated source folders (except the form-data-generated source folder)
                var srcFolderName = root.getPath().removeFirstSegments(1).toString().toLowerCase(Locale.US);
                return IScoutSourceFolders.GENERATED_SOURCE_FOLDER.equals(srcFolderName)
                    || !srcFolderName.contains("generated");
              })
              .forEach(result::add);
        }
      }
      return result;
    }
    catch (JavaModelException | SdkException e) {
      SdkLog.error("Error while calculating possible source folders", e);
      return emptySet();
    }
  }

  @Override
  public String getText(Object element) {
    var root = (IJavaElement) element;
    return root.getJavaProject().getElementName() + '/' + root.getPath().removeFirstSegments(1);
  }
}
