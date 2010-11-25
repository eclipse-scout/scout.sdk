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
package org.eclipse.scout.sdk.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TemplateFromFromFieldDialog}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 20.09.2010
 */
public class TemplateFromFromFieldDialog extends AbstractStatusDialog {

  private StyledTextField m_templateNameField;
  private Button m_replaceExistingFormField;

  private final IType m_formField;

  private String m_templateName;
  private boolean m_replaceFormField;

  /**
   * @param parentShell
   */
  public TemplateFromFromFieldDialog(Shell parentShell, String templateName, IType formField) {
    super(parentShell);
    m_formField = formField;
    m_templateName = templateName;
    m_replaceFormField = true;
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setTitle("Create a template of '" + getFormField().getElementName() + "'.");
    setMessage("A template is an abstract form field. To use this template create a form field class extending the template class.");
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Template support");
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    m_templateNameField = getFieldToolkit().createStyledTextField(container, "Template name");
    m_templateNameField.setReadOnlyPrefix("Abstract");
    m_templateNameField.setText(getTemplateName());
    m_templateNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_templateName = m_templateNameField.getText();
        pingStateChanging();
      }
    });

    m_replaceExistingFormField = new Button(container, SWT.CHECK);
    m_replaceExistingFormField.setText("Use template for '" + getFormField().getElementName() + "'");
    m_replaceExistingFormField.setSelection(isReplaceFormField());
    m_replaceExistingFormField.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_replaceFormField = m_replaceExistingFormField.getSelection();
        pingStateChanging();
      }
    });

    // layout
    container.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    container.setLayout(new GridLayout(1, true));
    m_templateNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return container;
  }

  @Override
  protected void validate(MultiStatus multiStatus) {
    multiStatus.add(getStatusTemplateName());
  }

  private IStatus getStatusTemplateName() {
    if (StringUtility.isNullOrEmpty(getTemplateName()) || "Abstrat".equals(getTemplateName())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Name not valid.");
    }
    IScoutBundle bundle = SdkTypeUtility.getScoutBundle(getFormField());
    if (TypeUtility.exists(ScoutSdk.getType(bundle.getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_TEMPLATE_FORM_FIELD) + "." + getTemplateName()))) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Template already exists.");
    }
    return Status.OK_STATUS;
  }

  /**
   * @return the formField
   */
  public IType getFormField() {
    return m_formField;
  }

  /**
   * @return the templateName
   */
  public String getTemplateName() {
    return m_templateName;
  }

  /**
   * @return the replaceFormField
   */
  public boolean isReplaceFormField() {
    return m_replaceFormField;
  }

}
