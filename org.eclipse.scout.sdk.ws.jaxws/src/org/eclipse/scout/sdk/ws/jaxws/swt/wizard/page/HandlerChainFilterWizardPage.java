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
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class HandlerChainFilterWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_FILTER_TYPE = "filterType";
  public static final String PROP_NAMESPACE_PREFIX = "namespacePrefix";
  public static final String PROP_NAMESPACE = "namespace";
  public static final String PROP_PATTERN = "pattern";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;

  private Button m_filterTypeNoneButton;
  private Button m_filterTypeProtocolButton;
  private Button m_filterTypeServiceButton;
  private Button m_filterTypePortButton;

  private Composite m_filterTypeProtocolComposite;
  private Composite m_filterTypeServiceComposite;
  private Composite m_filterTypePortComposite;

  private Map<Button, Composite> m_radioButtonMap;

  private StyledTextField m_protocolBindings;

  private StyledTextField m_serviceNamespacePrefix;
  private StyledTextField m_serviceNamespace;
  private StyledTextField m_servicePattern;

  private StyledTextField m_portNamespacePrefix;
  private StyledTextField m_portNamespace;
  private StyledTextField m_portPattern;

  public HandlerChainFilterWizardPage(IScoutBundle bundle) {
    super(HandlerChainFilterWizardPage.class.getName());
    setTitle(Texts.get("HandlerChainFilter"));
    setDescription(Texts.get("DescriptionHandlerChainFilter"));

    m_radioButtonMap = new HashMap<Button, Composite>();
    m_bundle = bundle;
    m_propertySupport = new BasicPropertySupport(this);
  }

  @Override
  protected void createContent(Composite parent) {
    m_filterTypeNoneButton = createRadioButton(parent, FilterTypeEnum.NoFilter);
    m_radioButtonMap.put(m_filterTypeNoneButton, null);

    // protocol filter
    m_filterTypeProtocolButton = createRadioButton(parent, FilterTypeEnum.ProtocolFilter);
    m_filterTypeProtocolComposite = new Composite(parent, SWT.NONE);
    m_radioButtonMap.put(m_filterTypeProtocolButton, m_filterTypeProtocolComposite);

    m_protocolBindings = new StyledTextField(m_filterTypeProtocolComposite, Texts.get("ProtocolBindings"));
    m_protocolBindings.setToolTipText(Texts.get("TooltipProtocolBindings"));
    m_protocolBindings.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setPatternInternal(m_protocolBindings.getText());
        pingStateChanging();
      }
    });
    if (getFilterType() == FilterTypeEnum.ProtocolFilter) {
      m_protocolBindings.setText(getPattern());
    }

    // service filter
    m_filterTypeServiceButton = createRadioButton(parent, FilterTypeEnum.ServiceFilter);
    m_filterTypeServiceComposite = new Composite(parent, SWT.NONE);
    m_radioButtonMap.put(m_filterTypeServiceButton, m_filterTypeServiceComposite);
    // namespace prefix
    m_serviceNamespacePrefix = new StyledTextField(m_filterTypeServiceComposite, Texts.get("NamespacePrefix"));
    m_serviceNamespacePrefix.setToolTipText(Texts.get("TooltipNamespacePrefix"));
    m_serviceNamespacePrefix.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setNamespacePrefixInternal(m_serviceNamespacePrefix.getText());
        pingStateChanging();
      }
    });
    // namespace
    m_serviceNamespace = new StyledTextField(m_filterTypeServiceComposite, Texts.get("Namespace"));
    m_serviceNamespace.setToolTipText(Texts.get("TooltipNamespace"));
    m_serviceNamespace.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setNamespaceInternal(m_serviceNamespace.getText());
        pingStateChanging();
      }
    });
    // pattern
    m_servicePattern = new StyledTextField(m_filterTypeServiceComposite, Texts.get("RestrictionPattern"));
    m_servicePattern.setToolTipText(Texts.get("TooltipRestriction", Texts.get("services")));
    m_servicePattern.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setPatternInternal(m_servicePattern.getText());
        pingStateChanging();
      }
    });
    if (getFilterType() == FilterTypeEnum.ServiceFilter) {
      m_serviceNamespacePrefix.setText(getNamespacePrefix());
      m_serviceNamespace.setText(getNamespace());
      m_servicePattern.setText(getPattern());
    }

    // port filter
    m_filterTypePortButton = createRadioButton(parent, FilterTypeEnum.PortFilter);
    m_filterTypePortComposite = new Composite(parent, SWT.NONE);
    m_radioButtonMap.put(m_filterTypePortButton, m_filterTypePortComposite);
    // namespace prefix
    m_portNamespacePrefix = new StyledTextField(m_filterTypePortComposite, Texts.get("NamespacePrefix"));
    m_portNamespacePrefix.setToolTipText(Texts.get("TooltipNamespacePrefix"));
    m_portNamespacePrefix.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setNamespacePrefixInternal(m_portNamespacePrefix.getText());
        pingStateChanging();
      }
    });
    // namespace
    m_portNamespace = new StyledTextField(m_filterTypePortComposite, Texts.get("Namespace"));
    m_portNamespace.setToolTipText(Texts.get("TooltipNamespace"));
    m_portNamespace.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setNamespaceInternal(m_portNamespace.getText());
        pingStateChanging();
      }
    });
    // pattern
    m_portPattern = new StyledTextField(m_filterTypePortComposite, Texts.get("RestrictionPattern"));
    m_portPattern.setToolTipText(Texts.get("TooltipRestriction", Texts.get("ports")));
    m_portPattern.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setPatternInternal(m_portPattern.getText());
        pingStateChanging();
      }
    });
    if (getFilterType() == FilterTypeEnum.PortFilter) {
      m_portNamespacePrefix.setText(getNamespacePrefix());
      m_portNamespace.setText(getNamespace());
      m_portPattern.setText(getPattern());
    }

    // layout
    parent.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_filterTypeNoneButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_filterTypeNoneButton, 10, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_filterTypeProtocolButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_filterTypeProtocolButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_filterTypeProtocolComposite.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_filterTypeProtocolComposite, 10, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_filterTypeServiceButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_filterTypeServiceButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_filterTypeServiceComposite.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_filterTypeServiceComposite, 10, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_filterTypePortButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_filterTypePortButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_filterTypePortComposite.setLayoutData(formData);

    // protocol composite
    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginLeft = 0;
    gridLayout.marginRight = 0;
    m_filterTypeProtocolComposite.setLayout(gridLayout);
    m_protocolBindings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    // service composite
    gridLayout = new GridLayout(1, false);
    gridLayout.marginLeft = 0;
    gridLayout.marginRight = 0;
    m_filterTypeServiceComposite.setLayout(gridLayout);
    m_serviceNamespacePrefix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_serviceNamespace.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_servicePattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    // port composite
    gridLayout = new GridLayout(1, false);
    gridLayout.marginLeft = 0;
    gridLayout.marginRight = 0;
    m_filterTypePortComposite.setLayout(gridLayout);
    m_portNamespacePrefix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_portNamespace.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_portPattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
  }

  private Button createRadioButton(Composite parent, final FilterTypeEnum filterType) {
    Button button = new Button(parent, SWT.RADIO);
    button.setData(filterType);
    button.setText(filterType.getLabel());
    button.setSelection(getFilterType() == filterType);
    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        setFilterType(filterType);
      }
    });
    return button;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    switch (getFilterType()) {
      case ServiceFilter:
      case PortFilter:
        if (!StringUtility.hasText(getNamespacePrefix())) {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", Texts.get("NamespacePrefix"))));
        }
        else if (!StringUtility.hasText(getNamespace())) {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", Texts.get("Namespace"))));
        }
        else if (!StringUtility.hasText(getPattern()) || getPattern().equals(getNamespacePrefix() + ":")) {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", Texts.get("RestrictionPattern"))));
        }
        break;
      case ProtocolFilter:
        if (!StringUtility.hasText(getPattern())) {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", Texts.get("ProtocolBindings"))));
        }
        break;
    }
  }

  public void setPattern(String pattern) {
    try {
      setStateChanging(true);
      setPatternInternal(pattern);
      if (isControlCreated()) {
        m_protocolBindings.setText("");
        m_servicePattern.setText("");
        m_portPattern.setText("");

        switch (getFilterType()) {
          case ProtocolFilter:
            m_protocolBindings.setText(pattern);
            break;
          case ServiceFilter:
            m_servicePattern.setText(pattern);
            break;
          case PortFilter:
            m_portPattern.setText(pattern);
            break;
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setPatternInternal(String pattern) {
    m_propertySupport.setPropertyString(PROP_PATTERN, pattern);
  }

  public String getPattern() {
    return m_propertySupport.getPropertyString(PROP_PATTERN);
  }

  public void setNamespacePrefix(String namespacePrefix) {
    try {
      setStateChanging(true);
      setNamespacePrefixInternal(namespacePrefix);
      if (isControlCreated()) {
        switch (getFilterType()) {
          case ServiceFilter:
            m_serviceNamespacePrefix.setText(namespacePrefix);
            break;
          case PortFilter:
            m_portNamespacePrefix.setText(namespacePrefix);
            break;
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void prefixNamespace() {
    if (!isControlCreated()) {
      return;
    }

    String prefix = getNamespacePrefix();
    if (!StringUtility.hasText(prefix)) {
      m_servicePattern.setReadOnlyPrefix(null);
      m_portPattern.setReadOnlyPrefix(null);
    }
    else {
      prefix += ":";
      switch (getFilterType()) {
        case ServiceFilter:
          m_portPattern.setReadOnlyPrefix(null);
          m_servicePattern.setReadOnlyPrefix(prefix);
          break;
        case PortFilter:
          m_servicePattern.setReadOnlyPrefix(null);
          m_portPattern.setReadOnlyPrefix(prefix);
          break;
      }
    }
  }

  private void setNamespacePrefixInternal(String namespacePrefix) {
    m_propertySupport.setPropertyString(PROP_NAMESPACE_PREFIX, namespacePrefix);
    prefixNamespace();
  }

  public String getNamespacePrefix() {
    return m_propertySupport.getPropertyString(PROP_NAMESPACE_PREFIX);
  }

  public void setNamespace(String namespace) {
    try {
      setStateChanging(true);
      setNamespaceInternal(namespace);
      if (isControlCreated()) {
        switch (getFilterType()) {
          case ServiceFilter:
            m_serviceNamespace.setText(namespace);
            break;
          case PortFilter:
            m_portNamespace.setText(namespace);
            break;
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setNamespaceInternal(String namespace) {
    m_propertySupport.setPropertyString(PROP_NAMESPACE, namespace);
  }

  public String getNamespace() {
    return m_propertySupport.getPropertyString(PROP_NAMESPACE);
  }

  public void setFilterType(FilterTypeEnum filterType) {
    try {
      setStateChanging(true);
      setFilterTypeInternal(filterType);
      if (isControlCreated()) {
        for (Entry<Button, Composite> entry : m_radioButtonMap.entrySet()) {
          Button button = entry.getKey();
          Composite composite = entry.getValue();
          FilterTypeEnum id = (FilterTypeEnum) button.getData();
          button.setSelection(id == filterType);
          if (composite != null) {
            JaxWsSdkUtility.setView(composite, id == filterType);
          }

          switch (filterType) {
            case NoFilter:
              m_protocolBindings.setText("");
              m_portNamespacePrefix.setText("");
              m_portNamespace.setText("");
              m_portPattern.setText("");
              m_serviceNamespacePrefix.setText("");
              m_serviceNamespace.setText("");
              m_servicePattern.setText("");
              setNamespace(null);
              setNamespacePrefix(null);
              break;
            case ServiceFilter:
              m_protocolBindings.setText("");
              m_portNamespacePrefix.setText("");
              m_portNamespace.setText("");
              m_portPattern.setText("");
              setNamespace("urn:namespace");
              setNamespacePrefix("ns1");
              break;
            case PortFilter:
              m_protocolBindings.setText("");
              m_serviceNamespacePrefix.setText("");
              m_serviceNamespace.setText("");
              m_servicePattern.setText("");
              setNamespace("urn:namespace");
              setNamespacePrefix("ns1");
              break;
            case ProtocolFilter:
              m_serviceNamespacePrefix.setText("");
              m_serviceNamespace.setText("");
              m_servicePattern.setText("");
              m_portNamespacePrefix.setText("");
              m_portNamespace.setText("");
              m_portPattern.setText("");
              setNamespace(null);
              setNamespacePrefix(null);
              setPattern("##SOAP11_HTTP");
              break;
          }
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setFilterTypeInternal(FilterTypeEnum filterType) {
    m_propertySupport.setProperty(PROP_FILTER_TYPE, filterType);
  }

  public FilterTypeEnum getFilterType() {
    return (FilterTypeEnum) m_propertySupport.getProperty(PROP_FILTER_TYPE);
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

  public static enum FilterTypeEnum {
    NoFilter(Texts.get("NoFilter")), ProtocolFilter(Texts.get("ProtocolFilter")), ServiceFilter(Texts.get("ServiceFilter")), PortFilter(Texts.get("PortFilter"));

    private String m_label;

    private FilterTypeEnum(String label) {
      m_label = label;
    }

    public String getLabel() {
      return m_label;
    }
  }
}
