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
package org.eclipse.scout.sdk.ui.fields.proposal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link SimpleProposal}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 07.02.2012
 */
public class SimpleProposal {

  private final String m_text;
  private final Image m_image;
  private HashMap<String, Object> m_data;

  public SimpleProposal(String text, Image image) {
    m_text = text;
    m_image = image;
    m_data = new HashMap<String, Object>();
  }

  public String getText() {
    return m_text;
  }

  /**
   * might be overridden for advanced text
   * 
   * @return
   */
  public String getTextSelected() {
    return getText();
  }

  public Image getImage() {
    return m_image;
  }

  /**
   * might be overridden for advanced image
   * 
   * @return
   */
  public Image getImageSelected() {
    return getImage();
  }

  public void setData(String key, Object value) {
    m_data.put(key, value);
  }

  public Object getData(String key) {
    return m_data.get(key);
  }

  Map<String, Object> getData() {
    return m_data;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SimpleProposal)) {
      return false;
    }
    SimpleProposal prop = (SimpleProposal) obj;
    if (!CompareUtility.equals(getText(), prop.getText())) {
      return false;
    }
    if (!CompareUtility.equals(getImage(), prop.getImage())) {
      return false;
    }
    Map<String, Object> localData = new HashMap<String, Object>(getData());
    Map<String, Object> propData = new HashMap<String, Object>(prop.getData());
    for (Entry<String, Object> lEntry : localData.entrySet()) {
      if (!CompareUtility.equals(lEntry.getValue(), propData.remove(lEntry.getKey()))) {
        return false;
      }
    }
    return propData.size() == 0;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    if (getText() != null) {
      hashCode ^= getText().hashCode();
    }
    if (getImage() != null) {
      hashCode ^= getImage().hashCode();
    }
    for (Entry<String, Object> entry : getData().entrySet()) {
      if (entry.getKey() != null) {
        hashCode ^= entry.getKey().hashCode();
      }
      if (entry.getValue() != null) {
        hashCode ^= entry.getValue().hashCode();
      }
    }
    return hashCode;
  }

}
