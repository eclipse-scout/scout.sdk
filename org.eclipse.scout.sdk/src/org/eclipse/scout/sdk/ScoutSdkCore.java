package org.eclipse.scout.sdk;

import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.util.typecache.IHierarchyCache;
import org.eclipse.scout.sdk.util.typecache.IJavaResourceChangedEmitter;
import org.eclipse.scout.sdk.util.typecache.ITypeCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
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
   * Creates and returns a new working copy manager instance.
   * 
   * @return The newly created working copy manager.
   * @see IWorkingCopyManager
   */
  public static IWorkingCopyManager createWorkingCopyManger() {
    return TypeCacheAccessor.createWorkingCopyManger();
  }

  /**
   * Access to the type cache.
   * 
   * @return The type cache instance.
   * @see ITypeCache
   */
  public static ITypeCache getTypeCache() {
    return TypeCacheAccessor.getTypeCache();
  }

  /**
   * Access to the type hierarchy cache.
   * 
   * @return The type hierarchy cache instance.
   * @see IHierarchyCache
   */
  public static IHierarchyCache getHierarchyCache() {
    return TypeCacheAccessor.getHierarchyCache();
  }

  /**
   * Access to the java resource change emitter.
   * 
   * @return The emitter instance.
   * @see IJavaResourceChangedEmitter
   */
  public static IJavaResourceChangedEmitter getJavaResourceChangedEmitter() {
    return TypeCacheAccessor.getJavaResourceChangedEmitter();
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
