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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.dialog.FontDialog;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.FontPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.property.FontSpec;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>FontPresenter</h3>
 */
public class FontPresenter extends AbstractValuePresenter<FontSpec> {

  private Font m_currentFont;
  private Font m_defaultFont;
  private Button m_chooserButton;

  private IPropertySourceParser<FontSpec> m_parser;

  public FontPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, ".*");
    m_parser = new FontPropertySourceParser();
  }

  public IPropertySourceParser<FontSpec> getParser() {
    return m_parser;
  }

  @Override
  public void dispose() {
    if (m_currentFont != null) {
      m_currentFont.dispose();
    }
    super.dispose();
  }

  @Override
  protected Control createContent(Composite container) {
    Composite rootPane = getToolkit().createComposite(container);
    Control text = super.createContent(rootPane);
    m_chooserButton = getToolkit().createButton(rootPane, "", SWT.PUSH);
    m_chooserButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolMagnifier));
    m_chooserButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showFontDialog();
      }
    });
    // layout
    GridLayout gLayout = new GridLayout(3, false);
    gLayout.horizontalSpacing = 3;
    gLayout.marginHeight = 0;
    gLayout.marginWidth = 0;
    rootPane.setLayout(gLayout);
    text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_chooserButton.setLayoutData(new GridData(SdkProperties.TOOL_BUTTON_SIZE, SdkProperties.TOOL_BUTTON_SIZE));
    m_defaultFont = getTextComponent().getFont();
    return rootPane;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_chooserButton.setEnabled(enabled);
    }
    super.setEnabled(enabled);
  }

  @Override
  public boolean isEnabled() {
    if (!isDisposed()) {
      return m_chooserButton.getEnabled() && super.isEnabled();
    }
    return false;
  }

  @Override
  protected void execCurrentSourceValueChanged(FontSpec value) {
    if (m_currentFont != null) {
      m_currentFont.dispose();
      m_currentFont = null;
    }
    if (value != null) {
      FontData data = m_defaultFont.getFontData()[0];
      data = new FontData(data.getName(), data.getHeight(), data.getStyle());
      if (value.getName() != null) {
        data.setName(value.getName());
      }
      if (value.getStyle() != null) {
        data.setStyle(value.getStyle());

      }
      if (value.getHeight() != null) {
        data.setHeight(value.getHeight());
      }
      m_currentFont = new Font(getContainer().getDisplay(), data);
    }
    getTextComponent().setFont(m_currentFont);

  }

  private void showFontDialog() {
    FontDialog fontDialog = new FontDialog(getContainer().getShell(), getCurrentSourceValue());
    FontSpec fontSpec = fontDialog.openDialog();
    if (fontSpec != null) {
      try {
        if (!CompareUtility.equals(getCurrentSourceValue(), fontSpec)) {
          storeValue(fontSpec);
        }
      }
      catch (CoreException e1) {
        ScoutSdkUi.logWarning("could not parse FontData: " + fontSpec, e1);
      }
    }
  }

  @Override
  protected FontSpec parseSourceInput(String input) throws CoreException {
    if (input.equals("")) {
      return getDefaultValue();
    }
    else {
      return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    }
  }

  @Override
  protected FontSpec parseDisplayInput(String input) throws CoreException {
    return parseSourceInput(input);
  }

  @Override
  protected String formatDisplayValue(FontSpec value) throws CoreException {
    if (value == null || value.isDefault()) {
      return "";
    }
    else {
      String displayValue = getParser().formatSourceValue(value, null, null);
      displayValue = displayValue.replaceAll("^\\\"(.*)\\\"$", "$1");
      return displayValue;
    }
  }

  @Override
  protected synchronized void storeValue(FontSpec value) throws CoreException {
    try {
      ConfigPropertyUpdateOperation<FontSpec> updateOp = new ConfigPropertyUpdateOperation<FontSpec>(getMethod(), getParser());
      updateOp.setValue(value);
      OperationJob job = new OperationJob(updateOp);
      job.setDebug(true);
      job.schedule();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not update method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }
  }
}
