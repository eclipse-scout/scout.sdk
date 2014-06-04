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
package org.eclipse.scout.sdk.workspace.type;

import java.util.Comparator;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class ScoutTypeComparators extends TypeComparators {

  protected static final Comparator<IType> ORDER_ANNOTATION_COMPARATOR = new Comparator<IType>() {
    @Override
    public int compare(IType t1, IType t2) {
      Double val1 = getOrderAnnotation(t1);
      Double val2 = getOrderAnnotation(t2);
      int result = val1.compareTo(val2);
      if (result == 0) {
        return t1.getElementName().compareTo(t2.getElementName());
      }
      return result;
    }

    private Double getOrderAnnotation(IType type) {
      if (TypeUtility.exists(type)) {
        try {
          Double sortNo = ScoutTypeUtility.getOrderAnnotationValue(type);
          if (sortNo != null) {
            return sortNo;
          }
          ScoutSdk.logInfo("could not find @Order annotation of '" + type.getFullyQualifiedName() + "'. ");
        }
        catch (Exception e) {
          ScoutSdk.logWarning("could not determine @Order annotation value of type '" + type.getFullyQualifiedName() + "'.", e);
        }
      }
      return Double.MAX_VALUE; // scout runtime returns max_value when no order annotation can be found.
    }
  };

  public static Comparator<IType> getOrderAnnotationComparator() {
    return ORDER_ANNOTATION_COMPARATOR;
  }
}
