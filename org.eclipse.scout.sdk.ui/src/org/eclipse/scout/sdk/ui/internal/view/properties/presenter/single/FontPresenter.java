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

import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.dialog.FontDialog;
import org.eclipse.scout.sdk.ui.internal.dialog.FontSpec;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtilities;
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
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>StringPresenter</h3> Representing a plain text property method. References like 'm_value' or
 * 'IConstants.ASTRING' are handled.
 */
public class FontPresenter extends AbstractValuePresenter<FontSpec> {

  private Font m_currentFont;
  private Font m_defaultFont;
  private Button m_chooserButton;

  public FontPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent, ".*");
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
    m_chooserButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.IMG_CHOOSE));
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
    m_chooserButton.setLayoutData(new GridData(ScoutIdeProperties.TOOL_BUTTON_SIZE, ScoutIdeProperties.TOOL_BUTTON_SIZE));
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
    String value = PropertyMethodSourceUtilities.parseReturnParameterString(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    if (value == null) {
      value = "";
    }
    return parseDisplayInput(value);
  }

  @Override
  protected FontSpec parseDisplayInput(String input) throws CoreException {
    FontSpec fontSpec = new FontSpec();
    StringTokenizer tok = new StringTokenizer(input, "-_,/.;");
    while (tok.hasMoreTokens()) {
      String nextToken = tok.nextToken();
      String s = nextToken.toUpperCase();
      // styles
      if (s.equals("PLAIN")) {
        fontSpec.addStyle(SWT.NORMAL);
        // nop
      }
      else if (s.equals("BOLD")) {
        fontSpec.addStyle(SWT.BOLD);
      }
      else if (s.equals("ITALIC")) {
        fontSpec.addStyle(SWT.ITALIC);
      }
      else {
        // size or name
        try {

          // size
          int size = Integer.parseInt(s);
          fontSpec.setHeight(size);
        }
        catch (NumberFormatException nfe) {
          // name
          fontSpec.setName(nextToken);
        }
      }
    }
    return fontSpec;
  }

  @Override
  protected String formatSourceValue(FontSpec value) throws CoreException {
    if (value == null || value.isDefault()) {
      return "null";
    }
    StringBuilder sourceBuilder = new StringBuilder();
    if (value.getStyle() != null) {
      if ((value.getStyle() & SWT.BOLD) != 0) {
        if (sourceBuilder.length() > 0) {
          sourceBuilder.append("-");
        }
        sourceBuilder.append("BOLD");
      }
      if ((value.getStyle() & SWT.ITALIC) != 0) {
        if (sourceBuilder.length() > 0) {
          sourceBuilder.append("-");
        }
        sourceBuilder.append("ITALIC");
      }
      if (sourceBuilder.length() == 0) {
        sourceBuilder.append("PLAIN");

      }
    }
    if (value.getHeight() != null) {
      if (sourceBuilder.length() > 0) {
        sourceBuilder.append("-");
      }

      sourceBuilder.append(value.getHeight());
    }
    if (value.getName() != null) {
      if (sourceBuilder.length() > 0) {
        sourceBuilder.append("-");
      }
      sourceBuilder.append(value.getName());
    }
    return "\"" + sourceBuilder.toString() + "\"";
  }

  @Override
  protected String formatDisplayValue(FontSpec value) throws CoreException {
    if (value == null || value.isDefault()) {
      return "";
    }
    else {
      String displayValue = formatSourceValue(value);
      displayValue = displayValue.replaceAll("^\\\"(.*)\\\"$", "$1");
      return displayValue;
    }
  }

  @Override
  protected synchronized void storeValue(FontSpec value) throws CoreException {
    IOperation op = null;
    if (ScoutSdkUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + formatSourceValue(value) + ";", true);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

}
