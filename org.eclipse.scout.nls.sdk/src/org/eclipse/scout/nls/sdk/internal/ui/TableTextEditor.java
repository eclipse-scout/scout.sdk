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
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class TableTextEditor extends Composite {

  private static final int BORDER_WIDTH = 1;
  private Text m_text;

  /**
   * @param parent
   * @param style
   */
  public TableTextEditor(Composite parent) {
    super(parent, SWT.NONE);
    super.setBackground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
    createContent(this);
    FillLayout layout = new FillLayout();
    layout.marginHeight = BORDER_WIDTH;
    layout.marginWidth = BORDER_WIDTH;
    setLayout(layout);
  }

  private void createContent(Composite parent) {
    m_text = new Text(parent, SWT.NONE);
    m_text.setBackground(m_text.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
  }

  /**
   * @param input
   */
  public void setText(String input) {
    m_text.setText(input);
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
  @Override
  public void addFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
   */
  @Override
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
  @Override
  public void removeFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
   */
  @Override
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
  @Override
  public Color getBackground() {
    return m_text.getBackground();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getForeground()
   */
  @Override
  public Color getForeground() {
    return m_text.getForeground();
  }

  /**
   * @param color
   * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
   */
  @Override
  public void setBackground(Color color) {
    m_text.setBackground(color);
  }

  /**
   * @param color
   * @see org.eclipse.swt.widgets.Control#setForeground(org.eclipse.swt.graphics.Color)
   */
  @Override
  public void setForeground(Color color) {
    m_text.setForeground(color);
  }

}
