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
package org.eclipse.scout.sdk.core.model.api;

import java.util.List;

import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;

/**
 * <h3>{@link ITypeParameter}</h3> Represents a type parameter.
 *
 * @author Ivan Motsch
 * @since 4.1.0 09.11.2014
 */
public interface ITypeParameter extends IAnnotatable {

  /**
   * Gets all bounds of this {@link ITypeParameter}. The first bound will be the class parameter (if existing) followed
   * by all interface bounds in the order as it is defined in the source or class file.<br>
   * <br>
   * <b>Example: </b>
   * <code>ChildClass&lt;X extends AbstractList&lt;String&gt; & Runnable & Serializable&gt;: .getBounds() = {AbstractList&lt;String&gt;, Runnable, Serializable}</code>
   *
   * @return A {@link List} containing all bounds of this {@link ITypeParameter}.
   */
  List<IType> getBounds();

  /**
   * Gets the {@link IMember} this {@link ITypeParameter} belongs to.
   *
   * @return The {@link IMember} this {@link ITypeParameter} belongs to.
   */
  IMember getDeclaringMember();

  String getSignature();

  @Override
  TypeParameterSpi unwrap();

}