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
package org.eclipse.scout.sdk.workspace.member;

import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdkUtility;

/**
 *
 */
public class ScoutSearchEngine {

  public static IType[] findInnerTypes(final IType definitionType, final IType declaringType) throws JavaModelException {
    TreeSet<IType> result = new TreeSet<IType>(getOrderAnnotationComparator());

    for (IType candidate : declaringType.getTypes()) {
      ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
      if (hierarchy.contains(definitionType)) {
        result.add(candidate);
      }
    }
    return result.toArray(new IType[result.size()]);
  }

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
        else {
          return val1.compareTo(val2);
        }
      }
    };
  }

}
