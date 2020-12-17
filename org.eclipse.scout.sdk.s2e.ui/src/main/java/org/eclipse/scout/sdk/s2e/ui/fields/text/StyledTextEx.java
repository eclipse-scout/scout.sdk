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
package org.eclipse.scout.sdk.s2e.ui.fields.text;

import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <h3>{@link StyledTextEx}</h3> provides paste handling.
 *
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

  public StyledTextEx(Composite parent, int style) {
    super(parent, style);
    m_clipboard = new Clipboard(getDisplay());
    addDisposeListener(e -> handleWidgetDisposed());
  }

  protected void handleWidgetDisposed() {
    m_clipboard.dispose();
  }

  @Override
  public void paste() {
    var plainTextTransfer = TextTransfer.getInstance();
    var clipboardContent = (String) m_clipboard.getContents(plainTextTransfer, DND.CLIPBOARD);
    if (Strings.hasText(clipboardContent)) {
      var e = new Event();
      e.doit = true;
      e.text = clipboardContent;
      e.widget = this;
      e.display = getDisplay();
      for (var l : getListeners(PASTE)) {
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
