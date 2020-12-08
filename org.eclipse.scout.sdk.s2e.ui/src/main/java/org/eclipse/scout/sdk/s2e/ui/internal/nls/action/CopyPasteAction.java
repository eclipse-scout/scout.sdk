/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class CopyPasteAction extends Action {

  private final String m_toCopy;
  private final Display m_display;

  public CopyPasteAction(String menuName, String toCopy, Display display) {
    super(menuName);
    m_toCopy = toCopy;
    m_display = display;
  }

  @Override
  public void run() {
    var clipboard = new Clipboard(m_display);
    try {
      //noinspection HardcodedLineSeparator
      var rtfData = "{\\rtf1\\b\\i " + m_toCopy + '}'; // formatted as bold and italic
      var textTransfer = TextTransfer.getInstance();
      var rtfTransfer = RTFTransfer.getInstance();
      Transfer[] transfers = {textTransfer, rtfTransfer};
      Object[] data = {m_toCopy, rtfData};
      clipboard.setContents(data, transfers);
    }
    finally {
      clipboard.dispose();
    }
  }
}
