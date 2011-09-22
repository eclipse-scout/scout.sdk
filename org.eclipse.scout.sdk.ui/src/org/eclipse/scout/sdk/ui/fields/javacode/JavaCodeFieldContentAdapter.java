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
package org.eclipse.scout.sdk.ui.fields.javacode;

import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public class JavaCodeFieldContentAdapter implements IControlContentAdapter {

  public JavaCodeFieldContentAdapter() {

  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.dialogs.taskassistance.IControlContentAdapter#getControlContents(org.eclipse.swt.widgets.Control)
   */
  @Override
  public String getControlContents(Control control) {
    return ((StyledText) control).getText();
  }

  /*
   * ss
   * (non-Javadoc)
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setControlContents(org.eclipse.swt.widgets.Control,
   * java.lang.String, int)
   */
  @Override
  public void setControlContents(Control control, String text, int cursorPosition) {
    StyledText tc = (StyledText) control;
    int currentCursorPosition = tc.getSelection().x;
    String newTextBefore = tc.getText().substring(0, currentCursorPosition);
    String newTextAfter = tc.getText().substring(tc.getSelection().y);
    newTextBefore = newTextBefore.replaceFirst("[a-zA-Z$_]*$", text);
    String newText = newTextBefore + newTextAfter;
    ((StyledText) control).setText(newText);
    ((StyledText) control).setSelection(newTextBefore.length(), newTextBefore.length());
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#insertControlContents(org.eclipse.swt.widgets.Control,
   * java.lang.String, int)
   */
  @Override
  public void insertControlContents(Control control, String text, int cursorPosition) {
    Point selection = ((StyledText) control).getSelection();
    ((StyledText) control).insert(text);
    // Insert will leave the cursor at the end of the inserted text. If this
    // is not what we wanted, reset the selection.
    if (cursorPosition < text.length()) {
      ((StyledText) control).setSelection(selection.x + cursorPosition, selection.x + cursorPosition);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getCursorPosition(org.eclipse.swt.widgets.Control)
   */
  @Override
  public int getCursorPosition(Control control) {
    return ((StyledText) control).getCaretOffset();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getInsertionBounds(org.eclipse.swt.widgets.Control)
   */
  @Override
  public Rectangle getInsertionBounds(Control control) {
    StyledText text = (StyledText) control;
    Point caretOrigin = text.getCaret().getLocation();
    return new Rectangle(caretOrigin.x, caretOrigin.y, 1, text.getLineHeight());
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setCursorPosition(org.eclipse.swt.widgets.Control,
   * int)
   */
  @Override
  public void setCursorPosition(Control control, int position) {
    ((StyledText) control).setSelection(new Point(position, position));
  }

}
