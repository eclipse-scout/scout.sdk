/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.library;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.ScoutResourceFilters;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link LibraryBundlesAddOperation}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 13.03.2012
 */
public class LibraryBundlesAddOperation implements IOperation {

  private final Set<IPluginModelBase> m_libraries;
  private final IScoutBundle m_bundle;

  public LibraryBundlesAddOperation(IScoutBundle bundle, Set<IPluginModelBase> libraries) {
    m_bundle = bundle;
    m_libraries = libraries;

  }

  @Override
  public String getOperationName() {
    StringBuilder nameBuilder = new StringBuilder();
    Iterator<IPluginModelBase> it = getLibraries().iterator();
    if (it.hasNext()) {
      nameBuilder.append(it.next().getBundleDescription().getName());
    }
    while (it.hasNext()) {
      nameBuilder.append(", ").append(it.next().getBundleDescription().getName());
    }
    return Texts.get("AddLibraryBundlesOperationName", nameBuilder.toString());
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getLibraries() == null || getLibraries().isEmpty()) {
      throw new IllegalArgumentException("Libraries to add can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // add to manifest
    PluginModelHelper ownerHelper = new PluginModelHelper(getBundle().getProject());
    Set<IPluginModelBase> libraries = getLibraries();
    for (IPluginModelBase lib : libraries) {
      ownerHelper.Manifest.addDependency(lib.getBundleDescription().getName());
    }
    ownerHelper.save();
    // add the dependencies to the product files
    // find all product files in the current scout project.
    for (IResource productFile : ResourceUtility.getAllResources(ScoutResourceFilters.getProductFiles(getBundle()))) {
      ProductFileModelHelper h = new ProductFileModelHelper((IFile) productFile);
      // add library bundle if there is already the library owner bundle in it.
      if (h.ProductFile.existsDependency(getBundle().getSymbolicName())) {
        for (IPluginModelBase lib : libraries) {
          h.ProductFile.addDependency(lib.getBundleDescription().getName());
        }
        h.save();
      }
    }
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public Set<IPluginModelBase> getLibraries() {
    return m_libraries;
  }

}
