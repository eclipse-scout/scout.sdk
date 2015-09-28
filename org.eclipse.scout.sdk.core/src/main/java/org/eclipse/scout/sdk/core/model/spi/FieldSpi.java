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
package org.eclipse.scout.sdk.core.model.spi;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;

/**
 * <h3>{@link FieldSpi}</h3> Represents a field in a java type.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface FieldSpi extends MemberSpi {

  /**
   * Gets the constant value of this {@link FieldSpi}.<br>
   * Please note: The field must be initialized with a constant value so that it can be retrieved using this method.
   *
   * @return The constant value of this {@link FieldSpi} if it can be computed or <code>null</code> otherwise.
   */
  IMetaValue getConstantValue();

  /**
   * Gets the data type of this {@link FieldSpi}.
   *
   * @return The {@link TypeSpi} describing the data type of this {@link FieldSpi}. Never returns <code>null</code>.
   */
  TypeSpi getDataType();

  /**
   * If this {@link FieldSpi} is a synthetic parameterized Field (for example the super class of a parameterized type
   * with applied type arguments) then this method returns the original field without the type arguments applied.
   * <p>
   * Otherwise this is returned
   */
  FieldSpi getOriginalField();

  ISourceRange getSourceOfInitializer();

  @Override
  IField wrap();
}
