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

import org.eclipse.scout.sdk.core.model.IAnnotatable;
import org.eclipse.scout.sdk.core.model.IAnnotation;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 *
 */
public final class ScoutUtils {
  private ScoutUtils() {
  }

  public static Double getOrderAnnotationValue(IAnnotatable a) {
    IAnnotation annotation = CoreUtils.getAnnotation(a, IRuntimeClasses.Order);
    BigDecimal orderValue = CoreUtils.getAnnotationValueNumeric(annotation, "value");
    if (orderValue == null) {
      return null;
    }
    return orderValue.doubleValue();
  }

  public static boolean existsReplaceAnnotation(IAnnotatable element) {
    return CoreUtils.getAnnotation(element, IRuntimeClasses.Replace) != null;
  }
}
