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
package org.eclipse.scout.sdk.core.model;

import java.util.Comparator;

import org.eclipse.scout.sdk.core.util.CompositeObject;

/**
 * Contains comparators for {@link IType}s.
 */
public final class TypeComparators {

  private TypeComparators() {
  }

  protected static final Comparator<IType> NAME_COMPARATOR = new Comparator<IType>() {
    @Override
    public int compare(IType t1, IType t2) {
      CompositeObject ct1 = new CompositeObject(t1.getSimpleName(), t1.getName(), t1);
      CompositeObject ct2 = new CompositeObject(t2.getSimpleName(), t2.getName(), t2);
      return ct1.compareTo(ct2);
    }
  };

  public static Comparator<IType> getTypeNameComparator() {
    return NAME_COMPARATOR;
  }
}
