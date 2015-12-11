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

  /**
   * Creates a {@link Comparator} to compare two {@link IType}s in respect of their @Order annotation.
   *
   * @param isBean
   *          Specifies if the {@link IType}s to be compared are Scout Beans or not.<br>
   *          Scout Beans and other Scout orderables have different default orders (order if no annotation is present).
   * @return The new created {@link Comparator}.
   */
  public static Comparator<IType> getOrderAnnotationComparator(final boolean isBean) {
    return new Comparator<IType>() {
      @Override
      public int compare(IType t1, IType t2) {
        // 1. order annotation value
        double val1 = OrderAnnotation.valueOf(t1, isBean);
        double val2 = OrderAnnotation.valueOf(t2, isBean);
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
  }

  /**
   * Creates and returns a new {@link Comparator} that compares {@link IType}s according to their simple name (
   * {@link IType#elementName()}) first and fully qualified name ({@link IType#name()}) second.
   * 
   * @return The new comparator
   */
  public static Comparator<IType> getTypeNameComparator() {
    return new Comparator<IType>() {
      @Override
      public int compare(IType o1, IType o2) {
        if (o1 == o2) {
          return 0;
        }
        if (o1 == null) {
          return -1;
        }
        if (o2 == null) {
          return 1;
        }
        int result = o1.elementName().compareTo(o2.elementName());
        if (result != 0) {
          return result;
        }
        return o1.name().compareTo(o2.name());
      }
    };
  }
}
