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
package org.eclipse.scout.sdk.ui.fields.javacode;

/**
 * <h3>CodeRange</h3> ...
 */
public class JavaCodeRange {
  public static final int UNKNOWN = 1;
  public static final int PRIMITIV_TYPE = 2;
  public static final int QUALIFIED_TYPE = 3;

  private int type = UNKNOWN;
  public int offset = -1;
  public int lenght = -1;

  private final String m_text;
  public String m_fullyQuallifiedName;

  public JavaCodeRange(String text) {
    m_text = text;
  }

  public String getFullyQuallifiedName() {
    return m_fullyQuallifiedName;
  }

  public void setFullyQuallifiedName(String fullyQuallifiedName) {
    m_fullyQuallifiedName = fullyQuallifiedName;
  }

  public String getText() {
    return m_text;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JavaCodeRange)) return false;
    else {
      return hashCode() == obj.hashCode();
    }
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    if (m_fullyQuallifiedName != null) {
      hashCode = hashCode ^ m_fullyQuallifiedName.hashCode();
    }
    return hashCode ^ m_text.hashCode() ^ getType() ^ offset ^ lenght;
  }

}
