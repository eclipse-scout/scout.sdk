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
package org.eclipse.scout.sdk.core.util;

import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;

/**
 * Contains {@link IFilter}s for {@link IAnnotation}s.
 */
public final class AnnotationFilters {

  private AnnotationFilters() {
  }

  /**
   * Creates and returns a new {@link IFilter} that evaluates to <code>true</code> if an annotation name (
   * {@link IAnnotation#name()}) matches the given name.
   *
   * @param name
   *          The name for which the {@link IFilter} should evaluate to <code>true</code>.
   * @return The new created {@link IFilter} matching the given name.
   */
  public static IFilter<IAnnotation> name(final String name) {
    return new IFilter<IAnnotation>() {
      @Override
      public boolean evaluate(IAnnotation annotation) {
        return Objects.equals(annotation.type().name(), name);
      }
    };
  }

  /**
   * Creates and returns a new {@link IFilter} that evaluates to <code>true</code> if a annotation name (
   * {@link IAnnotation#name()}) matches the given regular expression.
   *
   * @param regex
   *          The regex for which the {@link IFilter} should evaluate to <code>true</code>.
   * @return The new created {@link IFilter} matching the given regular expression.
   * @see Pattern
   */
  public static IFilter<IAnnotation> nameRegex(final Pattern regex) {
    return new IFilter<IAnnotation>() {
      @Override
      public boolean evaluate(IAnnotation annotation) {
        return regex.matcher(annotation.type().name()).matches();
      }
    };
  }
}
