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
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.util.ScoutUtils;

/**
 * Holds scout {@link Comparator}s comparing {@link IType}s
 */
public final class ScoutTypeComparators {
  private ScoutTypeComparators() {
  }

  protected static final Comparator<IType> ORDER_ANNOTATION_COMPARATOR = new Comparator<IType>() {
    @Override
    public int compare(IType t1, IType t2) {
      double val1 = getOrderAnnotationValue(t1);
      double val2 = getOrderAnnotationValue(t2);
      int result = Double.compare(val1, val2);
      if (result != 0) {
        return result;
      }
      return t1.getName().compareTo(t2.getName());
    }

    private double getOrderAnnotationValue(IType type) {
      if (type != null) {
        Double order = ScoutUtils.getOrderAnnotationValue(type);
        if (order != null) {
          return order.doubleValue();
        }
      }
      return ISdkProperties.DEFAULT_ORDER; // default order of the scout runtime
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
