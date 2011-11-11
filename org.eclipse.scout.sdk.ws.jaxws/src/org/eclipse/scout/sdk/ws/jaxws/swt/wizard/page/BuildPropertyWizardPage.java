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
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class BuildPropertyWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_DIRECTIVE = "name";
  public static final String PROP_VALUE = "value";

  private BasicPropertySupport m_propertySupport;
  private StyledTextField m_directiveField;
  private StyledTextField m_valueField;
  private Set<String> m_illegalNames;

  public BuildPropertyWizardPage() {
    super(BuildPropertyWizardPage.class.getName());
    setTitle(Texts.get("WsBuildDirective"));
    m_illegalNames = new HashSet<String>();
    m_propertySupport = new BasicPropertySupport(this);
    setDescription("");
  }

  @Override
  protected void createContent(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);

    m_directiveField = getFieldToolkit().createStyledTextField(container, Texts.get("Name"));
    m_directiveField.setText(StringUtility.nvl(getDirective(), ""));
    m_directiveField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setDirectiveInternal(m_directiveField.getText());
        pingStateChanging();
      }
    });

    m_valueField = getFieldToolkit().createStyledTextField(container, Texts.get("Value"));
    m_valueField.setText(StringUtility.nvl(getValue(), ""));
    m_valueField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setValueInternal(m_valueField.getText());
        pingStateChanging();
      }
    });

    // layout
    container.setLayout(new GridLayout(1, true));
    m_directiveField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    m_valueField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (!StringUtility.hasText(getDirective())) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", Texts.get("Name"))));
    }
    else {
      if (!getDirective().equals(JaxWsConstants.OPTION_BINDING_FILE) && m_illegalNames.contains(getDirective())) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("DirectiveXMustBeUnique", getDirective())));
      }
    }
  }

  public void setDirective(String directive) {
    try {
      setStateChanging(true);
      setDirectiveInternal(directive);
      if (isControlCreated()) {
        m_directiveField.setText(directive);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setDirectiveInternal(String directive) {
    m_propertySupport.setPropertyString(PROP_DIRECTIVE, directive);
  }

  public String getDirective() {
    return m_propertySupport.getPropertyString(PROP_DIRECTIVE);
  }

  public void setValue(String value) {
    try {
      setStateChanging(true);
      setValueInternal(value);
      if (isControlCreated()) {
        m_valueField.setText(value);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setValueInternal(String value) {
    m_propertySupport.setPropertyString(PROP_VALUE, value);
  }

  public String getValue() {
    return m_propertySupport.getPropertyString(PROP_VALUE);
  }

  public Set<String> getIllegalNames() {
    return m_illegalNames;
  }

  public void setIllegalNames(Set<String> illegalNames) {
    m_illegalNames = illegalNames;
  }
}
