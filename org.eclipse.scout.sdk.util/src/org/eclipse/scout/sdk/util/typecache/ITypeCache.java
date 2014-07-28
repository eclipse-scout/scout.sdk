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

import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link ITypeCache}</h3> Type cache handling access to {@link IType} instances.<br>
 * <br>
 * Use the {@link TypeUtility} to search for types hold by the type cache.
 *
 * @author Matthias Villiger
 * @since 3.4
 * @see TypeUtility
 * @see IType
 */
public interface ITypeCache {
  /**
   * Returns the first {@link IType} having the given name that is on all classpaths in the
   * workspace (all {@link IJavaProject}s).
   *
   * @param typeName
   *          The fully qualified or simple name of the {@link IType} to return.
   * @return The first {@link IType} having the given name that is on all classpaths in the
   *         workspace (all {@link IJavaProject}s).
   */
  IType getType(String typeName);

  /**
   * Returns all {@link IType}s with the given name that are on all classpaths in the
   * workspace (all {@link IJavaProject}s).
   *
   * @param typeName
   *          The fully qualified or simple name of the {@link IType}s to return.
   * @return All {@link IType}s with the given name that are on all classpaths in the
   *         workspace (all {@link IJavaProject}s). Never returns null.
   */
  Set<IType> getTypes(String typeName);

  /**
   * Disposes the cache. Releases all cached instances and stops listening for workspace changes.
   */
  void dispose();
}
