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
package org.eclipse.scout.sdk;

import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.util.ScoutSdkUtilCore;
import org.eclipse.scout.sdk.util.typecache.IHierarchyCache;
import org.eclipse.scout.sdk.util.typecache.IJavaResourceChangedEmitter;
import org.eclipse.scout.sdk.util.typecache.ITypeCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutWorkspace;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateManager;

/**
 * <h3>{@link ScoutSdkCore}</h3>The plug-in runtime class for the Scout SDK containing the core (UI-free) support for
 * Scout projects.
 *
 * @author Matthias Villiger
 * @since 3.9.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ScoutSdkCore {

  private ScoutSdkCore() {
  }

  /**
   * Access to the scout workspace root.
   *
   * @return The scout workspace
   * @see IScoutWorkspace
   */
  public static IScoutWorkspace getScoutWorkspace() {
    return ScoutWorkspace.getInstance();
  }

  /**
   * @see ScoutSdkUtilCore#createWorkingCopyManger()
   */
  public static IWorkingCopyManager createWorkingCopyManger() {
    return ScoutSdkUtilCore.createWorkingCopyManger();
  }

  /**
   * @see ScoutSdkUtilCore#getTypeCache()
   */
  public static ITypeCache getTypeCache() {
    return ScoutSdkUtilCore.getTypeCache();
  }

  /**
   * @see ScoutSdkUtilCore#getHierarchyCache()
   */
  public static IHierarchyCache getHierarchyCache() {
    return ScoutSdkUtilCore.getHierarchyCache();
  }

  /**
   * @see ScoutSdkUtilCore#getJavaResourceChangedEmitter()
   */
  public static IJavaResourceChangedEmitter getJavaResourceChangedEmitter() {
    return ScoutSdkUtilCore.getJavaResourceChangedEmitter();
  }

  /**
   * Access to the Scout DTO auto update manager.
   *
   * @return The manager instance or null if the Scout SDK plugin has not yet been started.
   * @see IDtoAutoUpdateManager
   */
  public static IDtoAutoUpdateManager getDtoAutoUpdateManager() {
    ScoutSdk activatorInstance = ScoutSdk.getDefault();
    if (activatorInstance != null) {
      return activatorInstance.getAutoUpdateManager();
    }
    return null;
  }
}
