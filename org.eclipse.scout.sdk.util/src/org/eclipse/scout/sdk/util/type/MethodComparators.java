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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.commons.CompositeObject;

/**
 *
 */
public final class MethodComparators {

  private MethodComparators() {
  }

  public static Comparator<IMethod> getNameComparator() {
    return new Comparator<IMethod>() {
      @Override
      public int compare(IMethod m1, IMethod m2) {
        CompositeObject m1c = new CompositeObject(m1.getElementName(), m1);
        CompositeObject m2c = new CompositeObject(m2.getElementName(), m2);
        return m1c.compareTo(m2c);
      }
    };
  }
}
