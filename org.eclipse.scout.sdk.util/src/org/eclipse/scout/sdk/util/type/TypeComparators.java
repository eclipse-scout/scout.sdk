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
package org.eclipse.scout.sdk.util.type;

import java.util.Comparator;

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompositeObject;

/**
 * Contains comparators for {@link IType}s.
 */
public class TypeComparators {

  protected TypeComparators() {
  }

  protected static final Comparator<IType> NAME_COMPARATOR = new Comparator<IType>() {
    @Override
    public int compare(IType t1, IType t2) {
      CompositeObject ct1 = new CompositeObject(t1.getElementName(), t1.getFullyQualifiedName(), t1);
      CompositeObject ct2 = new CompositeObject(t2.getElementName(), t2.getFullyQualifiedName(), t2);
      return ct1.compareTo(ct2);
    }
  };

  protected static final Comparator<IType> HASH_CODE_COMPARATOR = new Comparator<IType>() {
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

  protected static final Comparator<IType> SOURCE_RANGE_COMPARATOR = new Comparator<IType>() {

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
        try {
          ISourceRange t1Range = t1.getSourceRange();
          ISourceRange t2Range = t2.getSourceRange();
          return t1Range.getOffset() - t2Range.getOffset();
        }
        catch (JavaModelException e) {
          throw new IllegalArgumentException(e);
        }

      }
    }
  };

  public static Comparator<IType> getTypeNameComparator() {
    return NAME_COMPARATOR;
  }

  public static Comparator<IType> getHashCodeComparator() {
    return HASH_CODE_COMPARATOR;
  }

  public static Comparator<IType> getSourceRangeComparator() {
    return SOURCE_RANGE_COMPARATOR;
  }
}
