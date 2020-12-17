/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class HttpPatternMatchListener implements IPatternMatchListenerDelegate {

  /**
   * The console associated with this line tracker
   */
  private TextConsole m_console;

  @Override
  public void connect(TextConsole console) {
    m_console = console;
  }

  @Override
  public void disconnect() {
    m_console = null;
  }

  @Override
  public void matchFound(PatternMatchEvent event) {
    try {
      IHyperlink link = new P_HttpHyperlink(m_console);
      m_console.addHyperlink(link, event.getOffset(), event.getLength());
    }
    catch (BadLocationException e) {
      SdkLog.warning("Cannot create hyperlink.", e);
    }
  }

  private static final class P_HttpHyperlink implements IHyperlink {

    private final TextConsole m_sourceConsole;

    private P_HttpHyperlink(TextConsole console) {
      m_sourceConsole = console;
    }

    @Override
    public void linkEntered() {
      // nop
    }

    @Override
    public void linkExited() {
      // nop
    }

    @Override
    public void linkActivated() {
      var region = m_sourceConsole.getRegion(this);
      if (region == null) {
        return;
      }
      var document = m_sourceConsole.getDocument();
      try {
        var link = document.get(region.getOffset(), region.getLength());
        S2eUiUtils.showUrlInBrowser(link);
      }
      catch (BadLocationException e) {
        SdkLog.warning("Cannot create hyperlink.", e);
      }
    }
  }
}
