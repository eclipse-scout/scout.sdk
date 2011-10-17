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

import org.eclipse.swt.graphics.Image;

/**
 *
 */
public class DefaultProposal implements IContentProposalEx {

  private final String m_label;
  private final Image m_image;

  private HashMap<String, Object> m_data;

  public DefaultProposal(String label, Image img) {
    m_label = label;
    m_image = img;
    m_data = new HashMap<String, Object>();
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    return m_label;
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    return m_image;
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return m_label.length();
  }

  public void setData(String key, Object value) {
    m_data.put(key, value);
  }

  public Object getData(String key) {
    return m_data.get(key);
  }
}
