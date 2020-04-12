/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

/**
 * <h3>{@link ISourceRange}</h3>
 *
 * @since 5.1.0
 */
public interface ISourceRange {

  /**
   * Gets the source of the element this {@link ISourceRange} belongs to.
   *
   * @return the source as {@link CharSequence}.
   */
  CharSequence asCharSequence();

  /**
   * Gets the zero based index of the first character of this element relative to the source of the entire compilation
   * unit.
   *
   * @return The start index of the source of the element this range belongs to or a negative value if no source is
   *         available.
   */
  int start();

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
}
