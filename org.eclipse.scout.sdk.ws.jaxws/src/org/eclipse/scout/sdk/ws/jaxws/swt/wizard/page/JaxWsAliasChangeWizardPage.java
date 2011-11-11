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

import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class JaxWsAliasChangeWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_ALIAS = "jaxWsAlias";

  private BasicPropertySupport m_propertySupport;

  private StyledTextField m_jaxWsAlias;

  public JaxWsAliasChangeWizardPage() {
    super(JaxWsAliasChangeWizardPage.class.getName());
    m_propertySupport = new BasicPropertySupport(this);
    setDescription(Texts.get("ConfigureJaxWsAlias"));
  }

  @Override
  protected void createContent(Composite parent) {
    m_jaxWsAlias = getFieldToolkit().createStyledTextField(parent, Texts.get("jaxWsAlias"));
    m_jaxWsAlias.setReadOnlyPrefix("/");
    m_jaxWsAlias.setText(getJaxWsAlias());
    m_jaxWsAlias.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setJaxWsAliasInternal(m_jaxWsAlias.getText());
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 5);
    formData.right = new FormAttachment(100, 0);
    m_jaxWsAlias.setLayoutData(formData);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (StringUtility.isNullOrEmpty(getJaxWsAlias()) || getJaxWsAlias().equals("/")) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("EnterJaxWsAlias")));
      return;
    }
    if (!getJaxWsAlias().startsWith("/")) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("jaxWsAliasMustStartWithSlash")));
      return;
    }
    if (getJaxWsAlias().endsWith("/")) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("jaxWsAliasMustNotEndWithSlash")));
      return;
    }
  }

  public void setJaxWsAlias(String jaxWsAlias) {
    try {
      setStateChanging(true);
      setJaxWsAliasInternal(jaxWsAlias);
      if (isControlCreated() && m_jaxWsAlias != null) {
        m_jaxWsAlias.setText(jaxWsAlias);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setJaxWsAliasInternal(String jaxWsAlias) {
    m_propertySupport.setPropertyString(PROP_ALIAS, jaxWsAlias);
  }

  public String getJaxWsAlias() {
    return m_propertySupport.getPropertyString(PROP_ALIAS);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
    super.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
    super.removePropertyChangeListener(listener);
  }
}
