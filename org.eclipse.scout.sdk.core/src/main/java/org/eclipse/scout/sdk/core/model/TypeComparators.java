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

/**
 * Contains comparators for {@link IType}s.
 */
public final class TypeComparators {

  private TypeComparators() {
  }

  protected static final Comparator<IType> NAME_COMPARATOR = new Comparator<IType>() {
    @Override
    public int compare(IType t1, IType t2) {
      if (t1 == t2) {
        return 0;
      }
      if (t1 == null) {
        return -1;
      }
      if (t2 == null) {
        return 1;
      }

      int result = t1.getSimpleName().compareTo(t2.getSimpleName());
      if (result != 0) {
        return result;
      }
      return t1.getName().compareTo(t2.getName());
    }
  };

  /**
   * Gets a {@link Comparator} sorting {@link IType}s by simple name first and fully qualified name as second criterion
   * in ascending order.
   *
   * @return The ascending name comparator.
   */
  public static Comparator<IType> getTypeNameComparator() {
    return NAME_COMPARATOR;
  }
}
