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
package org.eclipse.scout.sdk.core.model;

/**
 * <h3>{@link IMember}</h3>
 * Represents Java elements that are members.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IMember extends IAnnotatable {

  /**
   * Gets the flags of this {@link IMember}. Use the {@link Flags} to access the value in this {@link Integer}.
   *
   * @return The flags of this {@link IMember}.
   * @see Flags
   */
  int getFlags();

  /**
   * @return the name of this {@link IMember}. Never returns <code>null</code>.
   */
  String getName();

  /**
   * @return The {@link IType} this member is defined in. Never returns <code>null</code>.
   */
  IType getDeclaringType();
}
