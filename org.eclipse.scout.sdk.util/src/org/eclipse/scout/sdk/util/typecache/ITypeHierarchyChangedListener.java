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

import java.util.EventListener;

/**
 * Listener to be invoked when a cached hierarchy has been invalidated.
 * 
 * @see ICachedTypeHierarchyResult
 */
public interface ITypeHierarchyChangedListener extends EventListener {

  /**
   * Called after the {@link ICachedTypeHierarchyResult} has been invalidated.<br>
   * Note: At the time this method is called the {@link ICachedTypeHierarchyResult} may already have been revalidated
   * again.
   */
  void hierarchyInvalidated();

}
