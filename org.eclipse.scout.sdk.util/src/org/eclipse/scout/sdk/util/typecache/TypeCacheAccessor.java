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

import org.eclipse.scout.sdk.util.internal.typecache.HierarchyCache;
import org.eclipse.scout.sdk.util.internal.typecache.JavaResourceChangedEmitter;
import org.eclipse.scout.sdk.util.internal.typecache.TypeCache;
import org.eclipse.scout.sdk.util.internal.typecache.WorkingCopyManager;

public class TypeCacheAccessor {
  public static IWorkingCopyManager createWorkingCopyManger() {
    return new WorkingCopyManager();
  }

  public static ITypeCache getTypeCache() {
    return TypeCache.getInstance();
  }

  public static IHierarchyCache getHierarchyCache() {
    return HierarchyCache.getInstance();
  }

  public static IJavaResourceChangedEmitter getJavaResourceChangedEmitter() {
    return JavaResourceChangedEmitter.getInstance();
  }
}
