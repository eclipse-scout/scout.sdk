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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.commons.CompareUtility;

/**
 * <h3>{@link JavaElementComparator}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 06.03.2012
 */
public class JavaElementComparator implements Comparator<IJavaElement> {

  @Override
  public int compare(IJavaElement o1, IJavaElement o2) {
    if (o1 == null && o2 == null) {
      return 0;
    }
    else if (o1 == null) {
      return 1;
    }
    else if (o2 == null) {
      return -1;
    }
    if (o1.getElementType() != o2.getElementType()) {
      return o2.getElementType() - o1.getElementType();
    }
    int diff = CompareUtility.compareTo(o1.getElementName(), o2.getElementName());
    return diff;
  }

}
