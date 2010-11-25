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
package org.eclipse.scout.sdk.workspace;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.internal.workspace.IScoutBundleConstantes;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public interface IScoutBundle extends IScoutContainer, IScoutBundleConstantes {

  public static final int DEPTH_ONE = 1;
  public static final int DEPTH_INFINITE = 2;

  IProject getProject();

  IJavaProject getJavaProject();

  IScoutProject getScoutProject();

  /**
   * spiders all known shared bundles and returns the closest found nls project.
   * 
   * @return best fit nls project or null if no nls project can be found
   */
  INlsProject findBestMatchNlsProject();

  /**
   * spiders all known shared bundles and returns the closest found icon provider.
   * 
   * @return best fit icon provider or null if no icon provider can be found
   */
  IIconProvider findBestMatchIconProvider();

  boolean isOnClasspath(IType type);

  boolean isOnClasspath(IScoutBundle bundle);

  /**
   * returns the project expected to be also the bundle name and the root package folder
   * 
   * @return
   */
  String getBundleName();

  /**
   * @return the name without the group prefix
   */
  String getSimpleName();

  IType findType(String qualifiedClassName);

  IPackageFragment getSpecificPackageFragment(String packageName, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager);

  String getSourceFolderName();

  String getRootPackageName();

  IPackageFragment getRootPackage(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager);

  IPackageFragment getPackageFragment(String packageName, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager);

  /**
   * @param extension
   * @return
   */
  String getPackageName(String extension);

  /**
   * find the package corresponding to the given name. Will not create if does not exist.
   * 
   * @param packageName
   * @return the found packageFragment or null if not found
   */
  IPackageFragment getPackageFragment(String packageName);

  /**
   * @param packageName
   *          a dot split package name
   * @param depth
   *          {@link IScoutBundle#DEPTH_ONE} for only direct subpackages, {@link IScoutBundle#DEPTH_INFINITE} for all
   *          subpackages.
   * @return
   */
  IPackageFragment[] getSubpackages(String packageName, int depth);

  /**
   * To find all dependent scout bundles. A dependent bundle has a dependency entry in in its Manifest.MF.
   * 
   * @return all scout bundles direct dependent on this bundle
   */
  IScoutBundle[] getDirectDependents();

  /**
   * To find all dependent scout bundles matching the filter. A dependent bundle has a dependency entry in in its
   * Manifest.MF.
   * 
   * @param filter
   *          the filter will be applied to all dependent bundles.
   * @return all scout bundles direct dependent on this bundle
   */
  IScoutBundle[] getDirectDependents(IScoutBundleFilter filter);

  /**
   * To find all scout bundles direct required of this bundle. A required bundle is registered as required bundle in the
   * Manifest.MF.
   * 
   * @return all direct required scout bundles.
   */
  IScoutBundle[] getDirectRequiredBundles();

  /**
   * To find all scout bundles direct required of this bundle. A required bundle is registered as required bundle in the
   * Manifest.MF.
   * 
   * @param filter
   *          the filter will be applied to all dependent bundles.
   * @return all direct required scout bundles.
   */
  IScoutBundle[] getDirectRequiredBundles(IScoutBundleFilter filter);

  IScoutBundle[] getDependentBundles(IScoutBundleFilter filter, boolean includeThis);

  IScoutBundle[] getRequiredBundles(IScoutBundleFilter filter, boolean includeThis);

  IScoutBundle findBestMatchShared();

}
