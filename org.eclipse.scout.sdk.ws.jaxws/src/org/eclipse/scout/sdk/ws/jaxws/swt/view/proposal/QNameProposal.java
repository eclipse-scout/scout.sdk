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

import org.eclipse.scout.sdk.ui.fields.proposal.SimpleProposal;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class QNameProposal extends SimpleProposal {

  private static final String DATA_QNAME = "dataQname";
  private static final String DATA_ICON_NAME = "dataiconName";

  public QNameProposal(QName qname) {
    this(qname, null);
  }

  public QNameProposal(QName qname, String icon) {
    super(qname.getLocalPart(), (icon != null) ? (JaxWsSdk.getImage(icon)) : (null));
    setData(DATA_QNAME, qname);
    setData(DATA_ICON_NAME, icon);
  }

  public QName getQname() {
    return (QName) getData(DATA_QNAME);
  }

  @Override
  public String getTextSelected() {
    return getQname().toString();
  }

}
