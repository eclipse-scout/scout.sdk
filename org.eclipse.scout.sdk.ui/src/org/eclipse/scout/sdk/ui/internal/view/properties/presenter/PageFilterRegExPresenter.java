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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter;

import org.eclipse.scout.sdk.ui.internal.view.properties.model.EnableRegExProperty;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class PageFilterRegExPresenter extends AbstractPresenter {

  private EnableRegExProperty m_prop;
  private Button m_field;
  private Label m_label;

  public PageFilterRegExPresenter(FormToolkit toolkit, Composite parent, EnableRegExProperty prop) {
    super(toolkit, parent);
    m_prop = prop;
    createContent(parent);
    parent.setLayout(new GridLayout(2, false));
  }

  protected void createContent(Composite parent) {
    m_label = getToolkit().createLabel(parent, "Use RegEx");
    m_field = getToolkit().createButton(parent, "", SWT.CHECK);
    m_field.setSelection(m_prop.isRegExFilter());
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = 150;
    m_field.setLayoutData(gd);
    // add listener
    Listener selectListener = new Listener() {
      public void handleEvent(Event e) {
        doVerifyInput();
      }
    };
    m_field.addListener(SWT.Selection, selectListener);

  }

  private void doVerifyInput() {
    boolean newValue = m_field.getSelection();
    m_field.setBackground(null);
    try {
      if (newValue != m_prop.isRegExFilter()) {
        m_prop.setRegExFilter(newValue);
      }
    }
    catch (Exception e) {
      m_field.setBackground(m_field.getDisplay().getSystemColor(SWT.COLOR_RED));
    }
  }
}
