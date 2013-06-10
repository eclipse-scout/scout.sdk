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
package org.eclipse.scout.sdk.util.jdt.finegraned;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.commons.CompareUtility;

public class FineGrainedJavaElementDelta {
  private IJavaElement m_element;

  public FineGrainedJavaElementDelta(IJavaElement element) {
    m_element = element;
  }

  public IJavaElement getElement() {
    return m_element;
  }

  @Override
  public int hashCode() {
    return m_element.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FineGrainedJavaElementDelta)) {
      return false;
    }
    return CompareUtility.equals(this.m_element, ((FineGrainedJavaElementDelta) o).m_element);
  }
}
