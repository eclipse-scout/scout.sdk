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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SeparatorType;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility.Registration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class JaxWsServletRegistrationWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_REGISTRATION_BUNDLE = "registrationBundle";
  public static final String PROP_ALIAS = "alias";
  public static final String PROP_URL_PATTERN = "urlPattern";

  private BasicPropertySupport m_propertySupport;

  private ProposalTextField m_registrationBundleField;
  private StyledTextField m_aliasField;
  private Composite m_descriptionContainer;
  private Text m_descriptionField;
  private StyledTextField m_urlPatternField;

  private IScoutBundle m_bundle;
  private IScoutBundle[] m_candidateBundles;
  private Map<IScoutBundle, String> m_servletRegistrationAliasMap;

  private boolean m_urlPatternVisible;

  public JaxWsServletRegistrationWizardPage(IScoutBundle bundle, boolean urlPatternVisible) {
    super(JaxWsServletRegistrationWizardPage.class.getName());
    setDescription(Texts.get("DescriptionJaxWsBundleConfiguration"));
    m_urlPatternVisible = urlPatternVisible;
    m_propertySupport = new BasicPropertySupport(this);
    m_bundle = bundle;
    m_candidateBundles = ServletRegistrationUtility.getJaxWsBundlesOnClasspath(m_bundle);
    m_servletRegistrationAliasMap = new HashMap<IScoutBundle, String>();
    Registration[] registrations = ServletRegistrationUtility.getJaxWsServletRegistrationsOnClasspath(m_bundle);
    for (Registration registration : registrations) {
      m_servletRegistrationAliasMap.put(registration.getBundle(), registration.getAlias());
    }
  }

  @Override
  protected void createContent(Composite parent) {
    // registration bundle
    m_registrationBundleField = getFieldToolkit().createProposalField(parent, Texts.get("ServletRegistrationBundle"));
    m_registrationBundleField.setLabelProvider(new SimpleLabelProvider());
    SimpleProposal[] proposals = new SimpleProposal[m_candidateBundles.length];
    for (int i = 0; i < proposals.length; i++) {
      proposals[i] = new P_BundleProposal(m_candidateBundles[i]);
    }
    m_registrationBundleField.setContentProvider(new SimpleProposalProvider(proposals));
    m_registrationBundleField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        Object proposal = event.proposal;
        if (proposal != null) {
          IScoutBundle registrationBundle = ((P_BundleProposal) proposal).getBundle();
          setRegistrationBundleInternal(registrationBundle);

          if (m_servletRegistrationAliasMap.containsKey(registrationBundle)) {
            // only allow to change the alias if the bundle that hosts the servlet registration is the bundle of the caller itself or has no alias defined
            m_aliasField.setEnabled(registrationBundle.getBundleName().equals(getBundle().getBundleName()) || !StringUtility.hasText(m_servletRegistrationAliasMap.get(registrationBundle)));
            setAlias(StringUtility.nvl(m_servletRegistrationAliasMap.get(registrationBundle), JaxWsConstants.JAX_WS_ALIAS));
          }
          else {
            // the servlet registration does not exist yet in that bundle
            m_aliasField.setEnabled(true);
            setAlias(JaxWsConstants.JAX_WS_ALIAS);
          }
        }
        else {
          setRegistrationBundleInternal(null);
        }
        pingStateChanging();
      }
    });

    // servlet alias
    m_aliasField = getFieldToolkit().createStyledTextField(parent, Texts.get("ServletAlias"));
    m_aliasField.setReadOnlyPrefix("/");
    m_aliasField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setAliasInternal(m_aliasField.getText());
        m_urlPatternField.setReadOnlyPrefix(JaxWsSdkUtility.normalizePath(m_aliasField.getText(), SeparatorType.BothType));
        pingStateChanging();
      }
    });

    // description
    m_descriptionContainer = new Composite(parent, SWT.NONE);
    m_descriptionField = new Text(m_descriptionContainer, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
    m_descriptionField.setEnabled(false);
    m_descriptionField.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    m_descriptionField.setText(Texts.get("DescriptionChangeJaxWsServletAlias"));

    // URL pattern
    m_urlPatternField = getFieldToolkit().createStyledTextField(parent, Texts.get("UrlPattern"));
    m_urlPatternField.setReadOnlyPrefix(JaxWsSdkUtility.normalizePath(StringUtility.emptyIfNull(getAlias()), SeparatorType.BothType));
    m_urlPatternField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setUrlPatternInternal(m_urlPatternField.getText());
        pingStateChanging();
      }
    });
    m_urlPatternField.setText(StringUtility.emptyIfNull(getUrlPattern()));

    // must be after other fields are created
    IScoutBundle registrationBundle = getRegistrationBundle();
    if (registrationBundle != null) {
      m_registrationBundleField.acceptProposal(new P_BundleProposal(registrationBundle));
    }
    m_aliasField.setText(StringUtility.emptyIfNull(getAlias()));

    // layout
    parent.setLayout(new GridLayout(1, true));

    // servlet
    GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    gd.exclude = (m_candidateBundles.length <= 1);
    m_registrationBundleField.setLayoutData(gd);
    // alias
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    m_aliasField.setLayoutData(gd);
    // description
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    m_descriptionContainer.setLayoutData(gd);
    // url pattern
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    gd.exclude = !m_urlPatternVisible;
    gd.verticalIndent = 15;
    m_urlPatternField.setLayoutData(gd);

    m_descriptionContainer.setLayout(new FormLayout());
    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(40, 2);
    formData.right = new FormAttachment(100, 0);
    m_descriptionField.setLayoutData(formData);
  }

  public void initializeDefaultValues(IScoutBundle bundle) {
    Registration registration = ServletRegistrationUtility.getServletRegistration(m_bundle);
    if (registration == null || StringUtility.isNullOrEmpty(registration.getAlias())) {
      Registration[] candidateRegistrations = ServletRegistrationUtility.getJaxWsServletRegistrationsOnClasspath(m_bundle);
      IScoutBundle[] candidateBundles = ServletRegistrationUtility.getJaxWsBundlesOnClasspath(m_bundle);
      if (candidateRegistrations.length > 0) {
        Registration candidateRegistration = candidateRegistrations[0];
        setRegistrationBundle(candidateRegistration.getBundle());
        setAlias(candidateRegistration.getAlias());
      }
      else if (candidateBundles.length > 0) {
        setRegistrationBundle(candidateBundles[0]);
        setAlias(JaxWsConstants.JAX_WS_ALIAS);
      }
      else {
        setAlias(JaxWsConstants.JAX_WS_ALIAS);
      }
    }
    else {
      setAlias(registration.getAlias());
      setRegistrationBundle(registration.getBundle());
    }
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (!isControlCreated()) {
      return;
    }
    if (getRegistrationBundle() == null) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, "Please choose the bundle to contain the JAX-WS servlet registration"));
      return;
    }
    if (StringUtility.isNullOrEmpty(getAlias()) || getAlias().equals("/")) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("EnterJaxWsAlias")));
      return;
    }
    if (!getAlias().startsWith("/")) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("jaxWsAliasMustStartWithSlash")));
      return;
    }
    if (getAlias().endsWith("/")) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("jaxWsAliasMustNotEndWithSlash")));
      return;
    }
    if (!getAlias().matches("[\\w\\-/]*")) { // check for illegal characters
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("InvalidServletAliasX", getAlias())));
      return;
    }

    for (Registration registration : ServletRegistrationUtility.getJaxWsServletRegistrationsOnClasspath(m_bundle)) {
      if (!CompareUtility.equals(getRegistrationBundle().getBundleName(), registration.getBundle().getBundleName()) && CompareUtility.equals(registration.getAlias(), getAlias())) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, "There already exists a JAX-WS servlet registration with the given alias in the bundle '" + registration.getBundle().getBundleName() + "'.\nPlease choose this bundle or another alias."));
        return;
      }
    }

    // URL pattern
    if (m_urlPatternVisible) {
      if (StringUtility.isNullOrEmpty(getUrlPattern()) || JaxWsSdkUtility.normalizePath(getUrlPattern(), SeparatorType.BothType).equals(JaxWsSdkUtility.normalizePath(getAlias(), SeparatorType.BothType))) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", m_urlPatternField.getLabelText())));
        return;
      }
      if (!getUrlPattern().startsWith(getAlias())) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XshouldStartWithY", m_urlPatternField.getLabelText(), getAlias())));
        return;
      }
      if (!getUrlPattern().matches("[\\w\\-/]*")) { // check for illegal characters
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("InvalidUrlX", getUrlPattern())));
        return;
      }
      if (getUrlPattern().endsWith("/")) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, "URL pattern must not end with a '/'."));
        return;
      }
      // TODO check for duplicate URL patterns
    }
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

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public IScoutBundle getRegistrationBundle() {
    return (IScoutBundle) m_propertySupport.getProperty(PROP_REGISTRATION_BUNDLE);
  }

  public void setRegistrationBundle(IScoutBundle registrationBundle) {
    try {
      setStateChanging(true);
      setRegistrationBundleInternal(registrationBundle);

      if (isControlCreated()) {
        if (registrationBundle != null) {
          m_registrationBundleField.acceptProposal(new P_BundleProposal(registrationBundle));
        }
        else {
          m_registrationBundleField.acceptProposal(null);
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setRegistrationBundleInternal(IScoutBundle registrationBundle) {
    m_propertySupport.setProperty(PROP_REGISTRATION_BUNDLE, registrationBundle);
  }

  public void setAlias(String alias) {
    try {
      setStateChanging(true);
      setAliasInternal(alias);
      if (isControlCreated()) {
        m_aliasField.setText(StringUtility.nvl(alias, ""));
        m_urlPatternField.setReadOnlyPrefix(JaxWsSdkUtility.normalizePath(StringUtility.nvl(alias, ""), SeparatorType.BothType));
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setAliasInternal(String alias) {
    m_propertySupport.setPropertyString(PROP_ALIAS, alias);
  }

  public String getAlias() {
    return m_propertySupport.getPropertyString(PROP_ALIAS);
  }

  public void setUrlPattern(String urlPattern) {
    try {
      setStateChanging(true);
      setUrlPatternInternal(urlPattern);
      if (isControlCreated()) {
        m_aliasField.setText(StringUtility.nvl(urlPattern, ""));
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setUrlPatternInternal(String urlPattern) {
    m_propertySupport.setPropertyString(PROP_URL_PATTERN, urlPattern);
  }

  public String getUrlPattern() {
    return m_propertySupport.getPropertyString(PROP_URL_PATTERN);
  }

  private class P_BundleProposal extends SimpleProposal {

    private static final String DATA_BUNDLE = "dataBundle";

    private P_BundleProposal(IScoutBundle bundle) {
      super(bundle.getBundleName(), ScoutSdkUi.getImage(SdkIcons.ServerBundle));
      setData(DATA_BUNDLE, bundle);
    }

    public IScoutBundle getBundle() {
      return (IScoutBundle) getData(DATA_BUNDLE);
    }
  }
}
