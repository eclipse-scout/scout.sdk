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
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ScoutSdkUtility;

/**
 *
 */
public class TypeComparators {

  public static Comparator<IType> getOrderAnnotationComparator() {
    return new Comparator<IType>() {
      @Override
      public int compare(IType t1, IType t2) {
        Double val1 = ScoutSdkUtility.getOrderAnnotation(t1);
        Double val2 = ScoutSdkUtility.getOrderAnnotation(t2);
        if (val1 == null && val2 == null) {
          return t1.getElementName().compareTo(t2.getElementName());
        }
        else if (val1 == null) {
          return -1;
        }
        else if (val2 == null) {
          return 1;
        }
        else if (val1.equals(val2)) {
          return t1.getElementName().compareTo(t2.getElementName());
        }
        else {
          return val1.compareTo(val2);
        }
      }
    };
  }

  public static Comparator<IType> getTypeNameComparator() {
    return new Comparator<IType>() {
      @Override
      public int compare(IType t1, IType t2) {
        CompositeObject ct1 = new CompositeObject(t1.getElementName(), t1.getFullyQualifiedName(), t1);
        CompositeObject ct2 = new CompositeObject(t2.getElementName(), t2.getFullyQualifiedName(), t2);
        return ct1.compareTo(ct2);
      }
    };
  }

  public static Comparator<IType> getHashCodeComparator() {
    return new Comparator<IType>() {
      @Override
      public int compare(IType t1, IType t2) {
        if (t1 == null && t2 == null) {
          return 0;
        }
        else if (t1 == null) {
          return -1;
        }
        else if (t2 == null) {
          return 1;
        }
        else {
          return t1.hashCode() - t2.hashCode();
        }
      }
    };
  }

}
