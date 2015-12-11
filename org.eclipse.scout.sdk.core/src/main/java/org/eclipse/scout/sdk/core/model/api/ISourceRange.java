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

import org.eclipse.scout.sdk.core.model.api.internal.SourceRange;

/**
 * <h3>{@link ISourceRange}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public interface ISourceRange {

  /**
   * Constant describing that no source is available for an element.
   */
  ISourceRange NO_SOURCE = new SourceRange(null, -1, -1);

  /**
   * Gets the source of the element this {@link ISourceRange} belongs to.
   *
   * @return the source as {@link String}.
   */
  @Override
  String toString();

  /**
   * Gets the zero based index of the first character of this element relative to the source of the entire compilation
   * unit.
   *
   * @return The start index of the source of the element this range belongs to or a negative value if no source is
   *         available.
   */
  int start();

  /**
   * Returns the number of characters of the source code for this element, relative to the source buffer in which this
   * element is contained.
   *
   * @return the number of characters of the source code for this element, relative to the source buffer in which this
   *         element is contained
   */

  /**
   * Gets the number of characters of the source code for this element
   *
   * @return the number of characters or a negative value if no source is available.
   */
  int length();

  /**
   * Gets the zero based index of the last character of this element relative to the source of the entire compilation
   * unit.
   *
   * @return The end index of the source of the element this range belongs to or a negative value if no source is
   *         available.
   */
  int end();

  /**
   * @return <code>true</code> if source is available, <code>false</code> otherwise.
   */
  boolean isAvailable();
}
