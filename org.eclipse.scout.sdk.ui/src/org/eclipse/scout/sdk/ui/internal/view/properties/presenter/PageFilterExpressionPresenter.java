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

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.FilterExpressionProperty;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PageFilterExpressionPresenter extends AbstractPresenter {

  private Text m_textField;
  private Button m_useRegexField;
  private final FilterExpressionProperty m_filterProperty;

  public PageFilterExpressionPresenter(PropertyViewFormToolkit toolkit, Composite parent, FilterExpressionProperty filterProperty) {
    super(toolkit, parent);
    m_filterProperty = filterProperty;
    createContent(getContainer());
    // layout
    GridLayout glayout = new GridLayout(1, true);
    glayout.horizontalSpacing = 0;
    glayout.marginHeight = 0;
    glayout.marginWidth = 0;
    glayout.verticalSpacing = 0;
    getContainer().setLayout(glayout);
  }

  protected void createContent(Composite parent) {
    Composite container = getToolkit().createComposite(parent);
    Label label = getToolkit().createLabel(container, Texts.get("Find"));
    m_textField = getToolkit().createText(container, "", SWT.BORDER);
    m_useRegexField = getToolkit().createButton(container, Texts.get("UseRegularExpression"), SWT.CHECK);
    m_textField.setText(m_filterProperty.getFilterText());
    m_useRegexField.setSelection(m_filterProperty.isRegexFilter());
    // add listener
    m_textField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        doVerifyInput();
      }
    });
    m_useRegexField.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        doVerifyInput();
      }
    });

    // layout
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.marginWidth = 0;
    container.setLayout(layout);
    // label
    GridData data = new GridData();
    data.widthHint = 50;
    data.verticalAlignment = GridData.CENTER;
    label.setLayoutData(data);
    // text
    data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
    m_textField.setLayoutData(data);

    // button
    data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    m_useRegexField.setLayoutData(data);

    container.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
  }

  private void doVerifyInput() {
    String s = m_textField.getText();
    if (s == null) {
      s = "";
    }
    else {
      s = s.trim();
    }
    try {
      m_textField.setBackground(null);
      String newValue = s;
      m_filterProperty.updateFilter(newValue, m_useRegexField.getSelection());
    }
    catch (Exception e) {
      m_textField.setBackground(m_textField.getDisplay().getSystemColor(SWT.COLOR_RED));
    }
  }

}
