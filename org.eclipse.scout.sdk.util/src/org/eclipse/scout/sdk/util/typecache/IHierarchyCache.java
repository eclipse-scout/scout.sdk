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

import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.internal.typecache.TypeHierarchy;

public interface IHierarchyCache {
  IPrimaryTypeTypeHierarchy getPrimaryTypeHierarchy(IType type) throws IllegalArgumentException;

  TypeHierarchy getSuperHierarchy(IType type);

  TypeHierarchy getLocalHierarchy(IRegion region);

  IPrimaryTypeTypeHierarchy[] getAllCachedHierarchies();

  void dispose();
}
