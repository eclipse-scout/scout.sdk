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
package org.eclipse.scout.sdk.internal.workspace;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.internal.BundleDependencies;
import org.eclipse.scout.sdk.operation.util.PackageNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;

/**
 * Project specific scout cache
 */
public class ScoutBundle implements IScoutBundle {

  private final IProject m_project;
  private final ScoutWorkspace m_workspaceGraph;
  private BundleDependencies m_dependencies;

  public ScoutBundle(IProject project, ScoutWorkspace scoutWorkspace) {
    m_project = project;
    m_workspaceGraph = scoutWorkspace;
    IPluginModelBase pluginModel = PluginRegistry.findModel(project);
    m_dependencies = new BundleDependencies(pluginModel.getBundleDescription());
  }

  boolean hasDependencyChanges() {
    IPluginModelBase model = PluginRegistry.findModel(getProject().getName());
    BundleDependencies newDependencies = new BundleDependencies(model.getBundleDescription());
    if (!newDependencies.equals(m_dependencies)) {
      m_dependencies = newDependencies;
      return true;
    }
    return false;
  }

  @Override
  public int getType() {
    return m_workspaceGraph.getBundleType(this);
  }

  @Override
  public IScoutBundle[] getDirectDependents() {
    return getDirectDependents(ScoutBundleFilters.getAcceptAllFilter());
  }

  @Override
  public IScoutBundle[] getDirectDependents(IScoutBundleFilter filter) {
    return m_workspaceGraph.getDirectDependents(this, filter);
  }

  @Override
  public IScoutBundle[] getDependentBundles(IScoutBundleFilter filter, boolean includeThis) {
    return m_workspaceGraph.getDependentBundles(this, filter, includeThis);
  }

  @Override
  public IScoutBundle[] getDirectRequiredBundles() {
    return getDirectRequiredBundles(ScoutBundleFilters.getAcceptAllFilter());
  }

  @Override
  public IScoutBundle[] getDirectRequiredBundles(IScoutBundleFilter filter) {
    return m_workspaceGraph.getDirectRequiredBundles(this, filter);
  }

  public IScoutBundle[] getRequiredBundles(IScoutBundleFilter filter, boolean includeThis) {
    return m_workspaceGraph.getRequiredBundles(this, filter, includeThis);
  }

  public IScoutBundle findBestMatchShared() {
    IScoutBundle result = null;
    IScoutBundle[] knownShareds = getRequiredBundles(ScoutBundleFilters.getSharedFilter(), true);
    if (knownShareds.length > 0) {
      result = knownShareds[0];
    }
    return result;
  }

  @Override
  public INlsProject findBestMatchNlsProject() {
    INlsProject nlsProject = m_workspaceGraph.getScoutProject(this).getNlsProject();
    return nlsProject;
  }

  @Override
  public IIconProvider findBestMatchIconProvider() {
    IIconProvider iconProvider = m_workspaceGraph.getScoutProject(this).getIconProvider();
    return iconProvider;
  }

  public IProject getProject() {
    return m_project;
  }

  public IJavaProject getJavaProject() {
    return JavaCore.create(getProject());
  }

  public IScoutProject getScoutProject() {
    return m_workspaceGraph.getScoutProject(this);
  }

  public String getBundleName() {
    return getProject().getName();
  }

  @Override
  public String getSimpleName() {
    String bundleName = getBundleName();
    bundleName = bundleName.replaceFirst(getScoutProject().getProjectName() + "\\.", "");
    return bundleName;
  }

  public String getRootPackageName() {
    return getBundleName();
  }

  public String getSourceFolderName() {
    return ScoutIdeProperties.DEFAULT_SOURCE_FOLDER_NAME;
  }

  public IPackageFragment getRootPackage(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    return getPackageFragment(getRootPackageName(), monitor, workingCopyManager);
  }

  public IPackageFragment getSpecificPackageFragment(String packageName, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    return getPackageFragment(packageName, monitor, workingCopyManager);
  }

  /**
   * @param qualifiedClassName
   *          . top workingCopyManager classes only.
   * @return the IType which corresponds to the qualified class name.
   */
  public IType findType(String qualifiedClassName) {
    try {
      return getJavaProject().findType(qualifiedClassName);
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning(e);
    }
    return null;
  }

  @Override
  public IJavaSearchScope getSearchScope() {
    return SearchEngine.createJavaSearchScope(new IJavaElement[]{getJavaProject()});
  }

  public String getPackageName(String extension) {
    return getRootPackageName() + extension;
  }

  @Override
  public IPackageFragment getPackageFragment(String packageName) {
    IPackageFragment pck = null;
    try {
      IPackageFragmentRoot root = getJavaProject().findPackageFragmentRoot(new Path("/" + getProject().getName() + "/" + getSourceFolderName()));
      pck = root.getPackageFragment(packageName);
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not get package '" + packageName + "'.", e);
    }
    return pck;
  }

  public IPackageFragment getPackageFragment(String packageName, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    IPackageFragment pck = null;
    try {
      IPackageFragmentRoot root = getJavaProject().findPackageFragmentRoot(new Path("/" + getProject().getName() + "/" + getSourceFolderName()));
      pck = root.getPackageFragment(packageName);
      if ((pck == null || !pck.exists()) && workingCopyManager != null) {
        PackageNewOperation proc = new PackageNewOperation(getJavaProject(), getSourceFolderName(), packageName);
        proc.run(monitor, workingCopyManager);
        pck = proc.getCreatedPackageFragment();
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("create package " + packageName, e);
    }
    return pck;
  }

  public IPackageFragment[] getSubpackages(String packageName, int depth) {
    // Leniency
    packageName = packageName.replace("/", ".");
    packageName = packageName.replaceAll("\\.*$", "");

    ArrayList<IPackageFragment> packages = new ArrayList<IPackageFragment>();
    try {
      String regex = null;
      if (depth == DEPTH_ONE) {
        regex = "^" + packageName + "\\.[^.]*$";
      }
      else {
        regex = "^" + packageName + "\\..*$";
      }
      IPackageFragmentRoot root = getJavaProject().findPackageFragmentRoot(new Path("/" + getProject().getName() + "/" + getSourceFolderName()));
      for (IJavaElement e : root.getChildren()) {
        if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT && e.getElementName().matches(regex)) {
          packages.add((IPackageFragment) e);
        }
      }
    }
    catch (CoreException e) {
      ScoutSdk.logWarning(e);
    }
    return packages.toArray(new IPackageFragment[packages.size()]);
  }

  public boolean contains(IJavaElement e) {
    if (e == null || !e.exists()) return false;
    if ((e instanceof IMember) && ((IMember) e).isBinary()) return false;
    return e.getJavaProject().getProject() == getJavaProject().getProject();
  }

  public boolean isOnClasspath(IType type) {
    return TypeFilters.getTypesOnClasspath(getJavaProject()).accept(type);
  }

  public boolean isOnClasspath(IScoutBundle bundle) {
    if (equals(bundle)) {
      return true;
    }
    return getJavaProject().isOnClasspath(bundle.getJavaProject());
  }

}
