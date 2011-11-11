/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.swt.view.proposal;

import javax.xml.namespace.QName;

import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.swt.graphics.Image;

public class QNameProposal implements IContentProposalEx {

  private QName m_qname;
  private String m_icon;

  public QNameProposal(QName qname) {
    m_qname = qname;
  }

  public QNameProposal(QName qname, String icon) {
    m_qname = qname;
    m_icon = icon;
  }

  public QName getQname() {
    return m_qname;
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return 0;
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    if (m_icon != null) {
      return JaxWsSdk.getImage(m_icon);
    }
    return null;
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    if (selected) {
      return m_qname.toString();
    }
    else {
      return m_qname.getLocalPart();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_qname == null) ? 0 : m_qname.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    QNameProposal other = (QNameProposal) obj;
    if (m_qname == null) {
      if (other.m_qname != null) return false;
    }
    else if (!m_qname.equals(other.m_qname)) return false;
    return true;
  }
}
