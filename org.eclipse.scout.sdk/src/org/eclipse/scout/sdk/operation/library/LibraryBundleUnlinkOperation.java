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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.ScoutResourceFilters;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link LibraryBundleUnlinkOperation}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 13.03.2012
 */
public class LibraryBundleUnlinkOperation implements IOperation {

  private final Collection<IPluginModelBase> m_libraries;
  private final IScoutBundle m_bundle;

  public LibraryBundleUnlinkOperation(IScoutBundle bundle, Collection<IPluginModelBase> libraries) {
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
    if (getBundle() == null) {
      throw new IllegalArgumentException("The bundle to mremove the library from can not be null");
    }
    if (getLibraries() == null || getLibraries().isEmpty()) {
      throw new IllegalArgumentException("Libraries to add can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    Set<IFile> productsToCheck = new HashSet<IFile>();
    Set<IScoutBundle> checkedScoutProjects = new HashSet<IScoutBundle>();
    List<IPluginModelBase> allUnlinkedLibraries = new LinkedList<IPluginModelBase>();
    IScoutBundle bundle = getBundle();
    PluginModelHelper helper = new PluginModelHelper(bundle.getProject());
    for (IPluginModelBase model : getLibraries()) {
      helper.Manifest.removeDependency(model.getPluginBase().getId());
      allUnlinkedLibraries.add(model);
    }
    helper.save();
    // grab product files
    if (!checkedScoutProjects.contains(bundle)) {
      for (IResource productFile : ResourceUtility.getAllResources(ScoutResourceFilters.getProductFileFilter(bundle))) {
        productsToCheck.add((IFile) productFile);
      }
    }

    for (IFile productFile : productsToCheck) {
      try {
        ProductFileModelHelper h = new ProductFileModelHelper(productFile);
        for (IPluginModelBase lib : allUnlinkedLibraries) {
          if (h.ProductFile.existsDependency(lib.getBundleDescription().getSymbolicName())) {
            removeLibraryFromProduct(h, lib);
          }
        }
        h.save();
      }
      catch (CoreException e) {
        ScoutSdk.logError("error during checking product files.", e);
      }
    }
  }

  /**
   * removes the library from the product file if the library bundle is not used by any other bundle of the product.
   * 
   * @param prodcutModelHelper
   * @param libraryBundle
   * @throws CoreException
   */
  private void removeLibraryFromProduct(ProductFileModelHelper prodcutModelHelper, IPluginModelBase libraryBundle) throws CoreException {
    for (BundleDescription libDependentBundle : libraryBundle.getBundleDescription().getDependents()) {
      if (prodcutModelHelper.ProductFile.existsDependency(libDependentBundle.getSymbolicName())) {
        return;
      }
    }
    prodcutModelHelper.ProductFile.removeDependency(libraryBundle.getBundleDescription().getSymbolicName());
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public Collection<IPluginModelBase> getLibraries() {
    return m_libraries;
  }

}
