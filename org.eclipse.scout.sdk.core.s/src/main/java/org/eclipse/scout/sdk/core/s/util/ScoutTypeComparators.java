/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.util;

import static java.util.Comparator.comparing;

import java.util.Comparator;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation;

/**
 * Holds scout {@link Comparator}s comparing {@link IType}s
 */
public final class ScoutTypeComparators {
  private ScoutTypeComparators() {
  }

  /**
   * A {@link Comparator} that compares {@link IType}s according to their simple name ( {@link IType#elementName()})
   * first and fully qualified name ({@link IType#name()}) second.
   */
  public static final Comparator<IType> BY_NAME = comparing(IType::elementName).thenComparing(IType::name);

  /**
   * Creates a {@link Comparator} to compare two {@link IType}s in respect of their {@code @Order} annotation value.
   *
   * @param isBean
   *          Specifies if the {@link IType}s to be compared are Scout Beans or not.<br>
   *          Scout Beans and other Scout orderables have different default orders (order if no annotation is present).
   * @return The new created {@link Comparator}.
   */
  public static Comparator<IType> orderAnnotationComparator(boolean isBean) {
    return Comparator.<IType> comparingDouble(t -> OrderAnnotation.valueOf(t, isBean))
        .thenComparing(BY_NAME);
  }
}
