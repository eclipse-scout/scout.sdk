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

import org.eclipse.scout.commons.CompositeObject;

/**
 * Convenience class for commonly used {@link IPropertyBean} comparators.
 */
public final class PropertyBeanComparators {

  private PropertyBeanComparators() {
  }

  /**
   * @return Returns a new instance of a name-based {@link IPropertyBean} comparator.
   */
  public static Comparator<IPropertyBean> getNameComparator() {
    return new Comparator<IPropertyBean>() {
      @Override
      public int compare(IPropertyBean p1, IPropertyBean p2) {
        if (p1 == null && p2 == null) {
          return 0;
        }
        else if (p1 == null) {
          return 1;
        }
        else if (p2 == null) {
          return -1;
        }
        CompositeObject m1c = new CompositeObject(p1.getBeanName(), p1);
        CompositeObject m2c = new CompositeObject(p2.getBeanName(), p2);
        return m1c.compareTo(m2c);
      }
    };
  }
}
