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

import org.eclipse.swt.graphics.Image;

/**
 * <h3>AbstractContentProposal</h3> ...
 */
public abstract class AbstractContentProposalEx implements IContentProposalEx {

  private final String m_text;
  private final Image m_image;

  public AbstractContentProposalEx(String text, Image image) {
    m_text = text;
    m_image = image;
  }

  public int getCursorPosition(boolean selected, boolean expertMode) {
    return m_text.length();
  }

  public Image getImage(boolean selected, boolean expertMode) {
    return m_image;
  }

  public String getLabel(boolean selected, boolean expertMode) {
    return m_text;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AbstractContentProposalEx)) {
      return false;
    }
    return compareTo((AbstractContentProposalEx) obj) == 0;
  }

  public int compareTo(AbstractContentProposalEx o) {
    int c = compareImpl(o.m_text, m_text);
    if (c != 0) return c;
    c = compareImpl(o.m_image, m_image);
    return c;
  }

  @SuppressWarnings("unchecked")
  protected int compareImpl(Object a, Object b) {
    if (a == null && b == null) return 0;
    if (a == null) return -1;
    if (b == null) return 1;
    if ((a instanceof Comparable) && (b instanceof Comparable)) return ((Comparable) a).compareTo(b);
    return a.toString().compareTo(b.toString());
  }

  @Override
  public int hashCode() {
    long h = 0;
    h = h ^ m_text.hashCode() ^ m_image.hashCode();
    return (int) h;
  }

  protected String getText() {
    return m_text;
  }

  protected Image getImage() {
    return m_image;
  }
}
