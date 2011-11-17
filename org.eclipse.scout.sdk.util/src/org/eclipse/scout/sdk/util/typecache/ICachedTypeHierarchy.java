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
package org.eclipse.scout.sdk.util.typecache;

/**
 *
 */
public interface ICachedTypeHierarchy extends ITypeHierarchy {
  /**
   * @param listener
   */
  void addHierarchyListener(ITypeHierarchyChangedListener listener);

  /**
   * @param listener
   */
  void removeHierarchyListener(ITypeHierarchyChangedListener listener);

  /**
   * @return true if the hierarchy is created false otherwise
   */
  boolean isCreated();

  /**
   * invalidates the hierarchy, next call reloads the type hierarchy. Reloads of large hierarchies are expensive.
   */
  void invalidate();
}
