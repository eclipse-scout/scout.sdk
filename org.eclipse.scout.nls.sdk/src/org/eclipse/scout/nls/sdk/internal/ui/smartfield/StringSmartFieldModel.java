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
package org.eclipse.scout.nls.sdk.internal.ui.smartfield;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

public class StringSmartFieldModel implements ISmartFieldModel {

  private List<Object> m_items;

  public StringSmartFieldModel(Object[] items) {
    m_items = new LinkedList<Object>();
    for (Object item : items) {
      m_items.add(item);
    }

  }

  public List<Object> getProposals(String pattern) {
    LinkedList<Object> items = new LinkedList<Object>();
    for (Object item : m_items) {
      if (getText(item).toUpperCase().startsWith(pattern.toUpperCase())) {
        items.add(item);
      }
    }
    return items;
  }

  public void itemSelected(Object item) {

  }

  public String getText(Object item) {
    return item.toString();
  }

  public Image getImage(Object item) {
    return null;
  }

}
