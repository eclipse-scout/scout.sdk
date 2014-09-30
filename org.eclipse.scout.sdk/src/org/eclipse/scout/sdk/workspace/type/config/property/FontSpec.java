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
package org.eclipse.scout.sdk.workspace.type.config.property;

import org.eclipse.scout.commons.CompareUtility;

/**
 * <h3>{@link FontSpec}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 17.11.2010
 */
public class FontSpec {

  private String m_name;
  private Integer m_style;
  private Integer m_height;

  public FontSpec(FontSpec spec) {
    m_name = spec.getName();
    m_style = spec.getStyle();
    m_height = spec.getHeight();
  }

  public FontSpec() {
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    m_name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return m_name;
  }

  public void addStyle(int style) {
    if (m_style == null) {
      m_style = 0;
    }
    m_style |= style;
  }

  /**
   * @param style
   *          the style to set
   */
  public void setStyle(Integer style) {
    m_style = style;
  }

  /**
   * @return the style
   */
  public Integer getStyle() {
    return m_style;
  }

  /**
   * @param height
   *          the height to set
   */
  public void setHeight(Integer height) {
    m_height = height;
  }

  /**
   * @return the height
   */
  public Integer getHeight() {
    return m_height;
  }

  public boolean isDefault() {
    return hashCode() == 12345;

  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FontSpec)) {
      return false;
    }
    FontSpec fontSpec = (FontSpec) obj;
    return CompareUtility.equals(fontSpec.getName(), getName())
        && CompareUtility.equals(fontSpec.getStyle(), getStyle())
        && CompareUtility.equals(fontSpec.getHeight(), getHeight());
  }

  @Override
  public int hashCode() {
    int code = 12345;
    if (getName() != null) {
      code ^= getName().hashCode();
    }
    if (getStyle() != null) {
      code ^= getStyle().hashCode();
    }
    if (getHeight() != null) {
      code ^= getHeight().hashCode();
    }
    return code;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("name='");
    if (getName() != null) {
      builder.append(getName() + "'");
    }
    else {
      builder.append("default'");
    }
    builder.append(" style='");
    if (getStyle() != null) {
      builder.append(getStyle() + "'");
    }
    else {
      builder.append("default'");
    }
    builder.append(" size='");
    if (getHeight() != null) {
      builder.append(getHeight() + "'");
    }
    return builder.toString();
  }
}
