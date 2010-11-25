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
package org.eclipse.scout.nls.sdk.internal.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class CopyPasteAction extends Action {

  private String m_menuName;
  private String m_toCopy;
  private Display m_display;

  public CopyPasteAction(String menuName, String toCopy, Display display) {
    m_toCopy = toCopy;
    m_menuName = menuName;
    m_display = display;
  }

  @Override
  public void run() {
    Clipboard clipboard = new Clipboard(m_display);
    String rtfData = "{\\rtf1\\b\\i " + m_toCopy + "}";
    TextTransfer textTransfer = TextTransfer.getInstance();
    RTFTransfer rtfTransfer = RTFTransfer.getInstance();
    Transfer[] transfers = new Transfer[]{textTransfer, rtfTransfer};
    Object[] data = new Object[]{m_toCopy, rtfData};
    clipboard.setContents(data, transfers);
    clipboard.dispose();
  }

  @Override
  public String getText() {

    return m_menuName;
  }
}
