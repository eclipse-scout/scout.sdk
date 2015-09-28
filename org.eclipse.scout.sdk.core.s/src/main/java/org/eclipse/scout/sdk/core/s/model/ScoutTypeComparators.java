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
package org.eclipse.scout.sdk.core.s.model;

import java.util.Comparator;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation;

/**
 * Holds scout {@link Comparator}s comparing {@link IType}s
 */
public final class ScoutTypeComparators {
  private ScoutTypeComparators() {
  }

  protected static final Comparator<IType> ORDER_ANNOTATION_COMPARATOR = new Comparator<IType>() {
    @Override
    public int compare(IType t1, IType t2) {
      // 1. order annotation value
      double val1 = OrderAnnotation.valueOf(t1);
      double val2 = OrderAnnotation.valueOf(t2);
      int result = Double.compare(val1, val2);
      if (result != 0) {
        return result;
      }
      // 2. simple name
      result = t1.elementName().compareTo(t2.elementName());
      if (result != 0) {
        return result;
      }
      // 3. fully qualified name
      return t1.name().compareTo(t2.name());
    }
  };

  /**
   * Gets a {@link Comparator} that sorts {@link IType}s by their @Order annotation value.
   *
   * @return The order annotation value comparator.
   */
  public static Comparator<IType> getOrderAnnotationComparator() {
    return ORDER_ANNOTATION_COMPARATOR;
  }
}
