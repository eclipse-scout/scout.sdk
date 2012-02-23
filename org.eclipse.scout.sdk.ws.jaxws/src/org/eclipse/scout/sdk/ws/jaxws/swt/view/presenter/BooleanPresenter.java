/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class BooleanPresenter extends AbstractPropertyPresenter<Boolean> {

  protected Button m_checkbox;
  private SelectionListener m_selectionListener;

  public BooleanPresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    super(parent, toolkit, false);
    m_selectionListener = new P_SelectionListener();
    callInitializer();
  }

  @Override
  protected Control createContent(Composite parent) {
    m_checkbox = new Button(parent, SWT.CHECK);
    return m_checkbox;
  }

  @Override
  protected void setInputInternal(Boolean input) {
    m_checkbox.removeSelectionListener(m_selectionListener);
    try {
      m_checkbox.setSelection(BooleanUtility.nvl(input, false));
    }
    finally {
      m_checkbox.addSelectionListener(m_selectionListener);
    }
  }

  private final class P_SelectionListener extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      setValueFromUI(m_checkbox.getSelection());
    }
  }
}
