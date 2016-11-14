package org.eclipse.scout.sdk.s2e.ui.internal;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.core.util.SdkLog;
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

    public P_HttpHyperlink(TextConsole console) {
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
      IRegion region = m_sourceConsole.getRegion(this);
      if (region == null) {
        return;
      }
      IDocument document = m_sourceConsole.getDocument();
      try {
        String link = document.get(region.getOffset(), region.getLength());
        S2eUiUtils.showUrlInBrowser(link);
      }
      catch (BadLocationException e) {
        SdkLog.warning("Cannot create hyperlink.", e);
      }
    }
  }
}