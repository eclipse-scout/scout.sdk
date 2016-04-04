/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.fields.text;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <h3>{@link StyledTextEx}</h3> provides paste handling.
 *
 * @author Andreas Hoegger
 * @since 3.10.0 2013-11-08
 */
public class StyledTextEx extends StyledText {

  public static final int PASTE = 229;

  private final Clipboard m_clipboard;

  public void addPasteListener(Listener pasteListener) {
    addListener(PASTE, pasteListener);
  }

  public void removePasteListener(Listener pasteListener) {
    removeListener(PASTE, pasteListener);
  }

  /**
   * @param parent
   * @param style
   */
  public StyledTextEx(Composite parent, int style) {
    super(parent, style);
    m_clipboard = new Clipboard(getDisplay());
    addDisposeListener(new DisposeListener() {

      @Override
      public void widgetDisposed(DisposeEvent e) {
        handleWidgetDisposed();
      }
    });
  }

  /**
   *
   */
  protected void handleWidgetDisposed() {
    m_clipboard.dispose();
  }

  @Override
  public void paste() {
    TextTransfer plainTextTransfer = TextTransfer.getInstance();
    String clipboardContent = (String) m_clipboard.getContents(plainTextTransfer, DND.CLIPBOARD);
    if (StringUtils.isNotBlank(clipboardContent)) {
      Event e = new Event();
      e.doit = true;
      e.text = clipboardContent;
      e.widget = this;
      e.display = getDisplay();
      for (Listener l : getListeners(PASTE)) {
        l.handleEvent(e);
        if (!e.doit) {
          break;
        }
      }
      if (e.doit) {
        super.paste();
      }
    }
  }
}
