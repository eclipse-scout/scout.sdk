/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static org.eclipse.scout.sdk.core.util.JavaTypes.arrayMarker;

import java.util.Objects;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link TypeNameDescriptor}</h3>
 *
 * @since 7.0.0
 */
public class TypeNameDescriptor {

  private final String m_primaryTypeName;
  private final String m_innerTypeNames;
  private final int m_arrayDimension;

  public static TypeNameDescriptor of(String fqn) {
    // check for inner types
    var firstDollarPos = Ensure.notNull(fqn).indexOf(JavaTypes.C_DOLLAR);
    var arrayDim = getArrayDimension(fqn);
    if (arrayDim > 0) {
      // remove array indicators
      fqn = fqn.substring(0, fqn.length() - arrayDim * 2);
    }
    if (firstDollarPos > 0) {
      var primaryType = fqn.substring(0, firstDollarPos);
      var innerTypePart = fqn.substring(firstDollarPos + 1);
      return new TypeNameDescriptor(primaryType, innerTypePart, arrayDim);
    }
    return new TypeNameDescriptor(fqn, null, arrayDim);
  }

  protected static int getArrayDimension(String fqn) {
    var pos = fqn.indexOf(arrayMarker());
    if (pos < 0) {
      return 0;
    }
    return (fqn.length() - pos) / 2;
  }

  protected TypeNameDescriptor(String primaryTypeName, String innerTypeNames, int arrayDimension) {
    m_primaryTypeName = primaryTypeName;
    m_innerTypeNames = innerTypeNames;
    m_arrayDimension = arrayDimension;
  }

  public boolean hasInnerType() {
    return m_innerTypeNames != null;
  }

  public String getPrimaryTypeName() {
    return m_primaryTypeName;
  }

  public String getInnerTypeNames() {
    return m_innerTypeNames;
  }

  public int getArrayDimension() {
    return m_arrayDimension;
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = 1;
    result = prime * result + m_arrayDimension;
    result = prime * result + ((m_innerTypeNames == null) ? 0 : m_innerTypeNames.hashCode());
    result = prime * result + m_primaryTypeName.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    var other = (TypeNameDescriptor) obj;
    if (m_arrayDimension != other.m_arrayDimension) {
      return false;
    }
    return Objects.equals(m_innerTypeNames, other.m_innerTypeNames)
        && Objects.equals(m_primaryTypeName, other.m_primaryTypeName);
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append("TypeNameDescriptor [");
    builder.append("primaryTypeName=").append(getPrimaryTypeName()).append(", ");
    if (m_innerTypeNames != null) {
      builder.append("innerTypeNames=").append(m_innerTypeNames).append(", ");
    }
    builder.append("arrayDimension=").append(getArrayDimension()).append(']');
    return builder.toString();
  }
}
