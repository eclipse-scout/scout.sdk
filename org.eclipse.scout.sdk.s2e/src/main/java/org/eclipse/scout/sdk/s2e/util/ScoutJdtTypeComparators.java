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
package org.eclipse.scout.sdk.s2e.util;

import java.math.BigDecimal;
import java.util.Comparator;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link ScoutJdtTypeComparators}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class ScoutJdtTypeComparators {
  private ScoutJdtTypeComparators() {
  }

  public static Comparator<IType> getTypeNameComparator() {
    return new Comparator<IType>() {
      @Override
      public int compare(IType o1, IType o2) {
        if (o1 == o2) {
          return 0;
        }
        if (!S2eUtils.exists(o1)) {
          return -1;
        }
        if (!S2eUtils.exists(o2)) {
          return 1;
        }
        int result = o1.getElementName().compareTo(o2.getElementName());
        if (result != 0) {
          return result;
        }
        return o1.getFullyQualifiedName().compareTo(o2.getFullyQualifiedName());
      }
    };
  }

  public static Comparator<IType> getOrderAnnotationComparator(final boolean isBean) {
    return new Comparator<IType>() {
      @Override
      public int compare(IType t1, IType t2) {
        // 1. order annotation value
        int result = Double.compare(getOrderAnnotationValue(t1, isBean), getOrderAnnotationValue(t2, isBean));
        if (result != 0) {
          return result;
        }

        // 2. simple name
        result = t1.getElementName().compareTo(t2.getElementName());
        if (result != 0) {
          return result;
        }

        // 3. fully qualified name
        return t1.getFullyQualifiedName().compareTo(t2.getFullyQualifiedName());
      }

      private double getOrderAnnotationValue(IType type, boolean b) {
        if (S2eUtils.exists(type)) {
          IAnnotation annotation = S2eUtils.getAnnotation(type, IScoutRuntimeTypes.Order);
          try {
            BigDecimal val = S2eUtils.getAnnotationValueNumeric(annotation, "value");
            if (val != null) {
              return val.doubleValue();
            }
          }
          catch (JavaModelException e) {
            SdkLog.warning("Unable to get @Order annotation value of type '" + type.getFullyQualifiedName() + "'.", e);
          }
        }
        if (b) {
          return ISdkProperties.DEFAULT_BEAN_ORDER; // default order of the scout runtime for beans
        }
        return ISdkProperties.DEFAULT_VIEW_ORDER; // default order of the scout runtime for view elements
      }
    };
  }
}
