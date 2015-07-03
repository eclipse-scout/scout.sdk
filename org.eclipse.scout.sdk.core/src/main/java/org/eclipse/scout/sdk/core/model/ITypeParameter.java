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
package org.eclipse.scout.sdk.core.model;

import org.apache.commons.collections4.set.ListOrderedSet;

/**
 * <h3>{@link ITypeParameter}</h3>
 * Represents a type parameter.
 *
 * @author Andreas Hoegger
 * @since 4.1.0 09.11.2014
 */
public interface ITypeParameter {

  /**
   * Gets the name of this {@link ITypeParameter}.<br>
   * <br>
   * <b>Example: </b><code>T</code> or <code>VALUE_TYPE</code>.
   *
   * @return The name of this {@link ITypeParameter}.
   */
  String getName();

  /**
   * Gets all bounds of this {@link ITypeParameter}. The first bound will be the class parameter (if existing) followed
   * by all interface bounds in the order as it is defined in the source or class file.<br>
   * <br>
   * <b>Example: </b>
   * <code>ChildClass&lt;X extends AbstractList&lt;String&gt; & Runnable & Serializable&gt;: .getBounds() = {AbstractList&lt;String&gt;, Runnable, Serializable}</code>
   *
   * @return A {@link ListOrderedSet} containing all bounds of this {@link ITypeParameter}.
   */
  ListOrderedSet<IType> getBounds();

  /**
   * Gets the {@link IType} this {@link ITypeParameter} belongs to.
   *
   * @return The {@link IType} this {@link ITypeParameter} belongs to.
   */
  IType getType();

}
