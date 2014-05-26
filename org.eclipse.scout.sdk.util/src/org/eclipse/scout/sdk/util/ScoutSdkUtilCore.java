/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util;

import org.eclipse.scout.sdk.util.internal.typecache.HierarchyCache;
import org.eclipse.scout.sdk.util.internal.typecache.JavaResourceChangedEmitter;
import org.eclipse.scout.sdk.util.internal.typecache.TypeCache;
import org.eclipse.scout.sdk.util.internal.typecache.WorkingCopyManager;
import org.eclipse.scout.sdk.util.typecache.IHierarchyCache;
import org.eclipse.scout.sdk.util.typecache.IJavaResourceChangedEmitter;
import org.eclipse.scout.sdk.util.typecache.ITypeCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link ScoutSdkUtilCore}</h3>
 * 
 * @author Matthias Villiger
 * @since 4.0.0 16.05.2014
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ScoutSdkUtilCore {

  private ScoutSdkUtilCore() {
  }

  /**
   * Creates and returns a new working copy manager instance.
   * 
   * @return The newly created working copy manager.
   * @see IWorkingCopyManager
   */
  public static IWorkingCopyManager createWorkingCopyManger() {
    return new WorkingCopyManager();
  }

  /**
   * Access to the type cache.
   * 
   * @return The type cache instance.
   * @see ITypeCache
   */
  public static ITypeCache getTypeCache() {
    return TypeCache.getInstance();
  }

  /**
   * Access to the type hierarchy cache.
   * 
   * @return The type hierarchy cache instance.
   * @see IHierarchyCache
   */
  public static IHierarchyCache getHierarchyCache() {
    return HierarchyCache.getInstance();
  }

  /**
   * Access to the java resource change emitter.
   * 
   * @return The emitter instance.
   * @see IJavaResourceChangedEmitter
   */
  public static IJavaResourceChangedEmitter getJavaResourceChangedEmitter() {
    return JavaResourceChangedEmitter.getInstance();
  }
}
