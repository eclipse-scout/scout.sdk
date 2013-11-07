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

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;

/**
 * <h3>{@link MethodParameter}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 06.12.2012
 */
public class MethodParameter {

  private final String m_name;
  private final String m_signature;

  public MethodParameter(String name, String signature) {
    m_name = name;
    m_signature = signature;
  }

  public String getName() {
    return m_name;
  }

  public String getSignature() {
    return m_signature;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MethodParameter)) {
      return false;
    }
    MethodParameter refParam = (MethodParameter) obj;
    if (!CompareUtility.equals(refParam.getName(), getName())) {
      return false;
    }
    if (!CompareUtility.equals(refParam.getSignature(), getSignature())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    if (StringUtility.hasText(getName())) {
      hashCode = getName().hashCode();
    }
    if (StringUtility.hasText(getSignature())) {
      hashCode ^= getSignature().hashCode();
    }
    return hashCode;
  }
}
