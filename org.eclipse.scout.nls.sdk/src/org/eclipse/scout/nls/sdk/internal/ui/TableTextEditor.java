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
package org.eclipse.scout.nls.sdk.internal.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TableTextEditor {
  private static final int BORDER_WIDTH = 1;
  private static Point MULTILINE_EDITOR_SIZE = new Point(594, 100);
  private final Text m_text;
  private final Shell m_shell;

  /**
   * @param parent
   */
  public TableTextEditor(TableCursor parent, int style) {
    m_shell = new Shell(parent.getShell(), SWT.TOOL);
    m_shell.setBounds(computeBounds(parent, style));
    m_shell.setBackground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_GREEN));

    FillLayout layout = new FillLayout();
    layout.marginHeight = BORDER_WIDTH;
    layout.marginWidth = BORDER_WIDTH;
    m_shell.setLayout(layout);

    m_text = new Text(m_shell, style);
    m_text.setBackground(m_text.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
  }

  private Rectangle computeBounds(TableCursor cursor, int style) {
    Rectangle bounds = new Rectangle(0, 0, 0, 0);
    Point size = null;
    if ((style & SWT.MULTI) != 0) {
      size = new Point(MULTILINE_EDITOR_SIZE.x, MULTILINE_EDITOR_SIZE.y);
    }
    else {
      size = new Point(cursor.getBounds().width, cursor.getBounds().height);
    }
    // max size is the table size
    Rectangle displayBounds = cursor.getDisplay().getBounds();
    size.x = Math.min(displayBounds.width, size.x);
    size.y = Math.min(displayBounds.height, size.y);
    bounds.width = size.x;
    bounds.height = size.y;

    Point shellPosition = cursor.toDisplay(new Point(0, 0));
    bounds.x = shellPosition.x;
    bounds.y = shellPosition.y;

    if ((bounds.x + bounds.width) > (displayBounds.x + displayBounds.width)) {
      bounds.x = displayBounds.x + displayBounds.width - bounds.width;
    }
    if ((bounds.y + bounds.height) > (displayBounds.y + displayBounds.height)) {
      bounds.x = displayBounds.y + displayBounds.height - bounds.height;
    }

    return bounds;
  }

  public void setFocus() {
    m_shell.setFocus();
  }

  public void open() {
    m_shell.open();
  }

  public void dispose() {
    m_shell.dispose();
  }

  public Display getDisplay() {
    return m_shell.getDisplay();
  }

  public boolean isDisposed() {
    return m_shell.isDisposed();
  }

  /**
   * @param input
   */
  public void setText(String input) {
    m_text.setText(input);
  }

  public void insertText(String text) {
    m_text.insert(text);
  }

  public String getText() {
    return m_text.getText();
  }

  /**
   * @param length
   */
  public void setSelection(int start) {
    m_text.setSelection(start);
  }

  public void setSelection(int start, int end) {
    m_text.setSelection(start, end);
  }

  public Point getSelection() {
    return m_text.getSelection();
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#addFocusListener(org.eclipse.swt.events.FocusListener)
   */
  public void addFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
   */
  public void addKeyListener(KeyListener listener) {
    m_text.addKeyListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Text#addModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  public void addModifyListener(ModifyListener listener) {
    m_text.addModifyListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Text#addVerifyListener(org.eclipse.swt.events.VerifyListener)
   */
  public void addVerifyListener(VerifyListener listener) {
    m_text.addVerifyListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#removeFocusListener(org.eclipse.swt.events.FocusListener)
   */
  public void removeFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
   */
  public void removeKeyListener(KeyListener listener) {
    m_text.removeKeyListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Text#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  public void removeModifyListener(ModifyListener listener) {
    m_text.removeModifyListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Text#removeVerifyListener(org.eclipse.swt.events.VerifyListener)
   */
  public void removeVerifyListener(VerifyListener listener) {
    m_text.removeVerifyListener(listener);
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBackground()
   */
  public Color getBackground() {
    return m_text.getBackground();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getForeground()
   */
  public Color getForeground() {
    return m_text.getForeground();
  }

  /**
   * @param color
   * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
   */
  public void setBackground(Color color) {
    m_text.setBackground(color);
  }

  /**
   * @param color
   * @see org.eclipse.swt.widgets.Control#setForeground(org.eclipse.swt.graphics.Color)
   */
  public void setForeground(Color color) {
    m_text.setForeground(color);
  }
}
