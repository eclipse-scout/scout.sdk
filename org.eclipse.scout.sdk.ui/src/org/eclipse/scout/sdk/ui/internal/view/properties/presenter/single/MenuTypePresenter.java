/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.ui.internal.dialog.MenuTypeDialog;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.MenuTypeParsers;
import org.eclipse.scout.sdk.workspace.type.config.parser.MenuTypesConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>{@link MenuTypePresenter}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 03.06.2014
 */
public class MenuTypePresenter extends AbstractValuePresenter<MenuTypesConfig> {

  private Button m_chooserButton;
  private IPropertySourceParser<MenuTypesConfig> m_parser;

  public MenuTypePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, null);
    m_parser = new MenuTypeParsers();
  }

  public IPropertySourceParser<MenuTypesConfig> getParser() {
    return m_parser;
  }

  @Override
  protected Control createContent(Composite container) {
    Composite rootPane = getToolkit().createComposite(container);
    super.createContent(rootPane);

    m_chooserButton = getToolkit().createButton(rootPane, "", SWT.PUSH);
    m_chooserButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolMagnifier));
    m_chooserButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showDialog();
      }
    });

    // layout
    GridLayout gLayout = new GridLayout(3, false);
    gLayout.horizontalSpacing = 3;
    gLayout.marginHeight = 0;
    gLayout.marginWidth = 0;
    rootPane.setLayout(gLayout);
    getTextComponent().setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
    getTextComponent().setEditable(false);
    m_chooserButton.setLayoutData(new GridData(SdkProperties.TOOL_BUTTON_SIZE, SdkProperties.TOOL_BUTTON_SIZE));
    return rootPane;
  }

  private void showDialog() {
    MenuTypeDialog menuTypesDialog = new MenuTypeDialog(getContainer().getShell(), getCurrentSourceValue().clone(), getMethod().getType());
    MenuTypesConfig menuTypesSpec = menuTypesDialog.openDialog();
    if (menuTypesSpec != null) {
      try {
        if (!CompareUtility.equals(getCurrentSourceValue(), menuTypesSpec)) {
          storeValue(menuTypesSpec);
        }
      }
      catch (CoreException e1) {
        ScoutSdkUi.logWarning("could not parse menu types: " + menuTypesSpec, e1);
      }
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_chooserButton.setEnabled(enabled);
    }
    super.setEnabled(enabled);
  }

  @Override
  public boolean isMultiLine() {
    return true;
  }

  @Override
  protected String formatDisplayValue(MenuTypesConfig value) throws CoreException {
    if (value == null) {
      return "";
    }
    else {
      return getParser().formatSourceValue(value, null, null).replace(", ", "\r\n");
    }
  }

  @Override
  protected MenuTypesConfig parseSourceInput(String input) throws CoreException {
    if ("".equals(input)) {
      return getDefaultValue();
    }
    else {
      return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    }
  }

  @Override
  protected MenuTypesConfig parseDisplayInput(String input) throws CoreException {
    return parseSourceInput(input);
  }

  @Override
  protected void storeValue(MenuTypesConfig value) throws CoreException {
    try {
      ConfigPropertyUpdateOperation<MenuTypesConfig> updateOp = new ConfigPropertyUpdateOperation<MenuTypesConfig>(getMethod(), getParser());
      updateOp.setValue(value);
      OperationJob job = new OperationJob(updateOp);
      job.setDebug(true);
      job.schedule();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could update method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }
  }
}
