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
package org.eclipse.scout.sdk.util.typecache;

/**
 * <h3>{@link ICachedTypeHierarchyResult}</h3> An {@link ITypeHierarchyResult} that caches the content.
 * 
 * @author Matthias Villiger
 * @since 4.0.0 14.05.2014
 */
public interface ICachedTypeHierarchyResult extends ITypeHierarchyResult {
  /**
   * Adds the given {@link ITypeHierarchyChangedListener} to this hierarchy (if not already added).
   * 
   * @param listener
   *          The {@link ITypeHierarchyChangedListener} to add.
   */
  void addHierarchyListener(ITypeHierarchyChangedListener listener);

  /**
   * Removes the given {@link ITypeHierarchyChangedListener} from the list to notify.
   * 
   * @param listener
   */
  void removeHierarchyListener(ITypeHierarchyChangedListener listener);

  /**
   * Invalidates the hierarchy, next call reloads the type hierarchy. Reloads of large hierarchies are expensive.
   */
  void invalidate();
}
