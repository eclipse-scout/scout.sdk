/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.parser;

import org.eclipse.scout.sdk.core.model.IType;

/**
 * <h3>{@link ILookupEnvironment}</h3>
 * Represents a lookup environment (classpath) capable to resolve {@link IType}s by name.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface ILookupEnvironment {

  /**
   * Tries to find the {@link IType} with the given name in the receiver {@link ILookupEnvironment} (classpath).
   *
   * @param fqn
   *          The fully qualified name of the {@link IType} to find. For inner {@link IType}s the inner part must be
   *          separated using '$': <code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.
   * @return The {@link IType} matching the given fully qualified name or <code>null</code> if it could not be found.
   */
  IType findType(String fqn);

  /**
   * Checks if a type with given name exists in the receiver {@link ILookupEnvironment} (classpath).
   *
   * @param fqn
   *          The fully qualified name of the {@link IType} to find. For inner {@link IType}s the inner part must be
   *          separated using '$': <code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.
   * @return <code>true</code> if a type matching the given name exists in the receiver {@link ILookupEnvironment}.
   */
  boolean existsType(String fqn);
}
