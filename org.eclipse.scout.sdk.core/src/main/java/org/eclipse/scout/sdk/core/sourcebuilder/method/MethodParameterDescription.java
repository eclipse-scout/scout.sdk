/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.sourcebuilder.method;

import org.eclipse.scout.sdk.core.model.Flags;

/**
 *
 */
public class MethodParameterDescription {

  private String m_signature;
  private String m_name;
  private int m_flags;

  public MethodParameterDescription(String name, String signature) {
    m_name = name;
    m_signature = signature;
    m_flags = Flags.AccDefault;
  }

  /**
   * @return
   */
  public int getFlags() {
    return m_flags;
  }

  public void setFlags(int flags) {
    m_flags = flags;
  }

  /**
   * @return
   */
  public String getSignature() {
    return m_signature;
  }

  public void setSignature(String signature) {
    m_signature = signature;
  }

  /**
   * @return
   */
  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_flags;
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    result = prime * result + ((m_signature == null) ? 0 : m_signature.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof MethodParameterDescription)) {
      return false;
    }
    MethodParameterDescription other = (MethodParameterDescription) obj;
    if (m_flags != other.m_flags) {
      return false;
    }
    if (m_name == null) {
      if (other.m_name != null) {
        return false;
      }
    }
    else if (!m_name.equals(other.m_name)) {
      return false;
    }
    if (m_signature == null) {
      if (other.m_signature != null) {
        return false;
      }
    }
    else if (!m_signature.equals(other.m_signature)) {
      return false;
    }
    return true;
  }
}
