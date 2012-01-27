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
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>StringPresenter</h3> Representing a plain text property method. References like 'm_value' or
 * 'IConstants.ASTRING' are handled.
 */
public class ColorPresenter extends AbstractValuePresenter<RGB> {

  private Canvas m_currentColorPresenter;
  private Color m_currentColor;
  private Button m_chooserButton;

  public ColorPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent, ".*");
  }

  @Override
  public void dispose() {
    if (m_currentColor != null) {
      m_currentColor.dispose();
    }
    super.dispose();
  }

  @Override
  protected Control createContent(Composite container) {
    Composite rootPane = getToolkit().createComposite(container);
    m_currentColorPresenter = new P_ColorPresenter(rootPane);
    getToolkit().adapt(m_currentColorPresenter);
    GridData curValData = new GridData(SdkProperties.TOOL_BUTTON_SIZE, SdkProperties.TOOL_BUTTON_SIZE);
    curValData.exclude = true;
    m_currentColorPresenter.setLayoutData(curValData);
    Control text = super.createContent(rootPane);
    m_chooserButton = getToolkit().createButton(rootPane, "", SWT.PUSH);
    m_chooserButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolMagnifier));
    m_chooserButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showColorChooser();
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
  protected void execCurrentSourceValueChanged(RGB value) {
    if (m_currentColor != null) {
      m_currentColor.dispose();
      m_currentColor = null;
    }
    if (m_currentColorPresenter != null && !m_currentColorPresenter.isDisposed()) {

      if (value != null) {
        m_currentColor = new Color(m_currentColorPresenter.getDisplay(), value);
        m_currentColorPresenter.setBackground(m_currentColor);
        ((GridData) m_currentColorPresenter.getLayoutData()).exclude = false;
        m_currentColorPresenter.setVisible(true);
        m_currentColorPresenter.getParent().layout(true);
      }
      else {
        ((GridData) m_currentColorPresenter.getLayoutData()).exclude = true;
        m_currentColorPresenter.setVisible(false);
        m_currentColorPresenter.getParent().layout(true);
      }
    }
  }

  private void showColorChooser() {
    ColorDialog colorDialog = new ColorDialog(getContainer().getShell());
    colorDialog.setRGB(getCurrentSourceValue());
    RGB rgb = colorDialog.open();
    if (rgb != null) {
      try {
        if (!CompareUtility.equals(getCurrentSourceValue(), rgb)) {
          storeValue(rgb);
        }
      }
      catch (CoreException e1) {
        ScoutSdkUi.logWarning("could not parse RGB: " + rgb, e1);
      }
    }
  }

  @Override
  protected RGB parseSourceInput(String input) throws CoreException {
    String value = PropertyMethodSourceUtility.parseReturnParameterString(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    if (value == null) {
      value = "";
    }
    return parseDisplayInput(value);
  }

  @Override
  protected RGB parseDisplayInput(String input) throws CoreException {
    if (!input.matches("(|[A-Fa-f0-9]{6})")) {
      throw new CoreException(new ScoutStatus(input));
    }
    if (input == null || input.length() == 0) {
      return null;
    }
    int i = Integer.parseInt(input, 16);
    return new RGB((i >> 16) & 0xff, (i >> 8) & 0xff, (i) & 0xff);
  }

  @Override
  protected String formatSourceValue(RGB value) throws CoreException {
    if (value == null) {
      return "null";
    }
    String rgbSt = Integer.toHexString((value.red << 16) | (value.green << 8) | (value.blue));
    while (rgbSt.length() < 6) {
      rgbSt = "0" + rgbSt;
    }
    return "\"" + rgbSt.toUpperCase() + "\"";
  }

  @Override
  protected String formatDisplayValue(RGB value) throws CoreException {
    if (value == null) {
      return "";
    }
    String rgbSt = Integer.toHexString((value.red << 16) | (value.green << 8) | (value.blue));
    while (rgbSt.length() < 6) {
      rgbSt = "0" + rgbSt;
    }
    return rgbSt.toUpperCase();
  }

  @Override
  protected synchronized void storeValue(RGB value) throws CoreException {
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + formatSourceValue(value) + ";", false);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

  private class P_ColorPresenter extends Canvas {

    public P_ColorPresenter(Composite parent) {
      super(parent, SWT.NONE);
      addPaintListener(new PaintListener() {
        @Override
        public void paintControl(PaintEvent e) {
          paint(e);
        }
      });
    }

    private void paint(PaintEvent e) {
      GC gc = e.gc;
      gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
      gc.fillRoundRectangle(0, 0, getBounds().width, getBounds().height, 2, 2);
      gc.setBackground(getBackground());
      gc.fillRoundRectangle(1, 1, getBounds().width - 2, getBounds().height - 2, 2, 2);
    }
  }
}
