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
package org.eclipse.scout.sdk.core.s.util;

import java.math.BigDecimal;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 *
 */
public final class ScoutUtils {
  private ScoutUtils() {
  }

  /**
   * Gets the value of the @Order annotation of the given {@link IAnnotatable}.
   *
   * @param a
   *          The {@link IAnnotatable} for which to return the @Order annotation value
   * @return The order annotation value or <code>null</code> if there is no order {@link IAnnotation} on the given
   *         element.
   */
  public static Double getOrderAnnotationValue(IAnnotatable a) {
    IAnnotation annotation = CoreUtils.getAnnotation(a, IRuntimeClasses.Order);
    BigDecimal orderValue = CoreUtils.getAnnotationValueNumeric(annotation, IRuntimeClasses.ORDER_ANNOTATION_VALUE);
    if (orderValue == null) {
      return null;
    }
    return Double.valueOf(orderValue.doubleValue());
  }

  /**
   * Checks whether an @Replace annotation exists on the given element.
   *
   * @param element
   *          The element to check.
   * @return <code>true</code> if a @Replace annotation exists on the given element, <code>false</code> otherwise.
   */
  public static boolean existsReplaceAnnotation(IAnnotatable element) {
    return CoreUtils.getAnnotation(element, IRuntimeClasses.Replace) != null;
  }
}
