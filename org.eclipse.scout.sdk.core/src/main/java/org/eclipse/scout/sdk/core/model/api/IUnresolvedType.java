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
package org.eclipse.scout.sdk.core.model.api;

/**
 * <h3>{@link IUnresolvedType}</h3> Represents a java data type. This includes classes, interfaces, enums, primitives,
 * the void-type ({@link #VOID}) & the wildcard-type ("?").
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IUnresolvedType {

  /**
   * Gets the {@link IPackage} of this {@link IUnresolvedType}.<br>
   * For primitives, the void-type and the wildcard-type this method returns the {@link IPackage#DEFAULT_PACKAGE}.
   *
   * @return The {@link IPackage} of this {@link IUnresolvedType} or {@link IPackage#DEFAULT_PACKAGE} for the default
   *         package. Never returns <code>null</code>.
   */
  IPackage getPackage();

  /**
   * Gets the simple name of this {@link IUnresolvedType}.
   *
   * @return The simple name of this {@link IUnresolvedType}.
   */
  String getSimpleName();

  /**
   * Gets the fully qualified name of this {@link IUnresolvedType}.<br>
   * Inner types are separated by '$'.<br>
   * <br>
   * <b>Example: </b><code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.<br>
   *
   * @return The fully qualified name of this {@link IUnresolvedType}.
   */
  String getName();

  String getSignature();

  /**
   * @return true if {@link #getType()} returns not null
   */
  boolean exists();

  /**
   * @return the existing type or null if it does not exist
   */
  IType getType();
}
