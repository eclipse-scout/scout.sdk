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
package org.eclipse.scout.sdk.ui.fields.tooltip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>CustomTooltip</h3> ...
 */
public class CustomTooltip extends AbstractTooltip {

  private String m_text;
  private Label m_tooltip;

  public CustomTooltip(Control control, boolean multiline) {
    super(control);
  }

  @Override
  protected void createContent(Composite parent) {
    m_tooltip = new Label(parent, SWT.NONE);
    m_tooltip.setBackground(parent.getBackground());
    m_tooltip.setText(m_text);
  }

  @Override
  protected void show(int x, int y) {
    if (m_text == null || m_text.equals("")) {
      return;
    }
    super.show(x, y);
  }

  public void setText(String source) {
    m_text = source;
  }

}
