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
import java.util.regex.Pattern;

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
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.internal.BundleDependencies;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.util.PackageNewOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.DefaultTargetPackage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * Project specific scout cache
 */
public class ScoutBundle implements IScoutBundle {

  private final static Pattern REGEX_LEADING_DOTS = Pattern.compile("^\\.*");

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

  @Override
  public IScoutBundle[] getRequiredBundles(IScoutBundleFilter filter, boolean includeThis) {
    return m_workspaceGraph.getRequiredBundles(this, filter, includeThis);
  }

  @Override
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
    IScoutProject project = m_workspaceGraph.getScoutProject(this);
    INlsProject nlsProject = null;
    while (nlsProject == null && project != null) {
      nlsProject = project.findNlsProject();
      project = project.getParentProject();
    }
    return nlsProject;
  }

  @Override
  public IIconProvider findBestMatchIconProvider() {
    IIconProvider iconProvider = m_workspaceGraph.getScoutProject(this).getIconProvider();
    return iconProvider;
  }

  @Override
  public IProject getProject() {
    return m_project;
  }

  @Override
  public IJavaProject getJavaProject() {
    return JavaCore.create(getProject());
  }

  @Override
  public IScoutProject getScoutProject() {
    return m_workspaceGraph.getScoutProject(this);
  }

  @Override
  public String getBundleName() {
    return getProject().getName();
  }

  @Override
  public String getSimpleName() {
    String bundleName = getBundleName();
    bundleName = bundleName.replaceFirst(getScoutProject().getProjectName() + "\\.", "");
    return bundleName;
  }

  @Override
  public String getRootPackageName() {
    return getBundleName();
  }

  @Override
  public String getSourceFolderName() {
    return SdkProperties.DEFAULT_SOURCE_FOLDER_NAME;
  }

  @Override
  public IPackageFragment getRootPackage(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    return getPackageFragment(getRootPackageName(), monitor, workingCopyManager);
  }

  @Override
  public IPackageFragment getSpecificPackageFragment(String packageName, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    return getPackageFragment(packageName, monitor, workingCopyManager);
  }

  /**
   * @param qualifiedClassName
   *          . top workingCopyManager classes only.
   * @return the IType which corresponds to the qualified class name.
   */
  @Override
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

  @Override
  public String getDefaultPackage(String packageId) {
    String pck = DefaultTargetPackage.get(getScoutProject(), packageId);
    if (pck == null) {
      throw new IllegalArgumentException("invalid package id");
    }
    return getRootPackageName() + "." + pck;
  }

  @Override
  public String getPackageName(String appendix) {
    if (appendix == null) {
      return getRootPackageName();
    }

    appendix = REGEX_LEADING_DOTS.matcher(appendix).replaceAll("").trim();
    if (appendix.length() > 0) {
      appendix = "." + appendix;
    }
    return getRootPackageName() + appendix;
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

  @Override
  public IPackageFragment getPackageFragment(String packageName, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IPackageFragment pck = null;
    try {
      IPackageFragmentRoot root = getJavaProject().findPackageFragmentRoot(new Path("/" + getProject().getName() + "/" + getSourceFolderName()));
      pck = root.getPackageFragment(packageName);
      if ((pck == null || !pck.exists())) {
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

  @Override
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

  @Override
  public boolean contains(IJavaElement e) {
    if (e == null || !e.exists()) return false;
    if ((e instanceof IMember) && ((IMember) e).isBinary()) return false;
    return e.getJavaProject().getProject() == getJavaProject().getProject();
  }

  @Override
  public boolean isOnClasspath(IType type) {
    return TypeFilters.getTypesOnClasspath(getJavaProject()).accept(type);
  }

  @Override
  public boolean isOnClasspath(IScoutBundle bundle) {
    if (equals(bundle)) {
      return true;
    }
    return getJavaProject().isOnClasspath(bundle.getJavaProject());
  }

}
