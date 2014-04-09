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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsdlStyleEnum;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;
import org.eclipse.scout.sdk.ws.jaxws.validator.IUrlPatternValidation;
import org.eclipse.scout.sdk.ws.jaxws.validator.UrlPatternValidator;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings("restriction")
public class WsPropertiesNewWsdlWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_WSDL_NAME = "wsdlName";
  public static final String PROP_DERIVE_OTHER_NAME = "deriveOtherNames";
  public static final String PROP_ALIAS = "alias";
  public static final String PROP_URL_PATTERN = "urlPattern";
  public static final String PROP_TARGET_NAMESPACE = "targetNamespace";
  public static final String PROP_SERVICE_NAME = "serviceName";
  public static final String PROP_PORT_NAME = "port";
  public static final String PROP_BINDING_NAME = "bindingName";
  public static final String PROP_PORT_TYPE_NAME = "portTypeName";
  public static final String PROP_SERVICE_OPERATION_NAME = "serviceOperationName";
  public static final String PROP_WSDL_STYLE = "wsdlStyle";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;
  private String m_jaxWsServletAlias;

  private StyledTextField m_wsdlNameField;
  private Button m_deriveOtherNameButton;
  private StyledTextField m_alias;
  private StyledTextField m_urlPattern;
  private StyledTextField m_targetNamespaceField;
  private StyledTextField m_serviceNameField;
  private StyledTextField m_portNameField;
  private StyledTextField m_bindingNameField;
  private StyledTextField m_portTypeNameField;
  private StyledTextField m_serviceOperationNameField;
  private Composite m_wsdlStyleRadioComposite;
  private List<Button> m_wsdlStyleRadioButtons;

  private Document m_sunJaxWsXml;
  private Set<String> m_illegalAliasNames;
  private Set<String> m_illegalUrlPatterns;

  private boolean m_showOnlyWsdlProperties;

  public WsPropertiesNewWsdlWizardPage(IScoutBundle bundle) {
    super(WsPropertiesNewWsdlWizardPage.class.getName());
    setTitle(Texts.get("ConfigureWebserviceProperties"));
    setDescription(Texts.get("ConfigureWebserviceProperties"));

    m_bundle = bundle;
    m_propertySupport = new BasicPropertySupport(this);
    m_sunJaxWsXml = ResourceFactory.getSunJaxWsResource(bundle).loadXml();
    m_wsdlStyleRadioButtons = new LinkedList<Button>();
    m_jaxWsServletAlias = JaxWsConstants.JAX_WS_ALIAS;

    loadIllegalValues();
    applyDefaults();
  }

  private void applyDefaults() {
    setDeriveOtherNames(true);
    setWsdlStyle(WsdlStyleEnum.DocumentLiteralWrapped);
  }

  @Override
  public void postActivate() {
    if (isShowOnlyWsdlProperties()) {
      deriveOtherNames();
    }
  }

  @Override
  protected void createContent(Composite parent) {
    m_wsdlNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("WsdlName"));
    m_wsdlNameField.setText(getWsdlName());
    m_wsdlNameField.setReadOnlySuffix("WebService.wsdl");
    m_wsdlNameField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setWsdlNameInternal(m_wsdlNameField.getText());
        pingStateChanging();
        deriveOtherNames();
      }
    });
    m_wsdlNameField.setVisible(!isShowOnlyWsdlProperties());

    m_deriveOtherNameButton = new Button(parent, SWT.CHECK);
    m_deriveOtherNameButton.setSelection(isDeriveOtherNames());
    m_deriveOtherNameButton.setText(Texts.get("DeriveOtherNamesFormWsdlFileName"));
    m_deriveOtherNameButton.setVisible(!isShowOnlyWsdlProperties());
    m_deriveOtherNameButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        setDeriveOtherNames(m_deriveOtherNameButton.getSelection());
        deriveOtherNames();
      }
    });

    m_alias = getFieldToolkit().createStyledTextField(parent, Texts.get("Alias"));
    m_alias.setText(getWsdlName());
    m_alias.setVisible(!isShowOnlyWsdlProperties());
    m_alias.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setAliasInternal(m_alias.getText());
        pingStateChanging();
      }
    });

    m_urlPattern = getFieldToolkit().createStyledTextField(parent, Texts.get("UrlPattern"));
    m_urlPattern.setText(getWsdlName());
    m_urlPattern.setReadOnlyPrefix(new Path(PathNormalizer.toServletAlias(m_jaxWsServletAlias)).addTrailingSeparator().toString());
    m_urlPattern.setVisible(!isShowOnlyWsdlProperties());
    m_urlPattern.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setUrlPatternInternal(m_urlPattern.getText());
        pingStateChanging();
      }
    });

    m_targetNamespaceField = getFieldToolkit().createStyledTextField(parent, Texts.get("TargetNamespace"));
    m_targetNamespaceField.setText(getTargetNamespace());
    m_targetNamespaceField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setTargetNamespaceInternal(m_targetNamespaceField.getText());
        pingStateChanging();
      }
    });

    m_serviceNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("Service"));
    m_serviceNameField.setText(getServiceName());
    m_serviceNameField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setServiceNameInternal(m_serviceNameField.getText());
        pingStateChanging();
      }
    });

    m_portNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("Port"));
    m_portNameField.setText(getServiceName());
    m_portNameField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setPortNameInternal(m_portNameField.getText());
        pingStateChanging();
      }
    });

    m_bindingNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("Binding"));
    m_bindingNameField.setText(getBinding());
    m_bindingNameField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setBindingNameInternal(m_bindingNameField.getText());
        pingStateChanging();
      }
    });

    m_portTypeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("PortType"));
    m_portTypeNameField.setText(getPortTypeName());
    m_portTypeNameField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setPortTypeNameInternal(m_portTypeNameField.getText());
        pingStateChanging();
      }
    });

    m_serviceOperationNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("ServiceOperation"));
    m_serviceOperationNameField.setText(getPortTypeName());
    m_serviceOperationNameField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setServiceOperationNameInternal(m_serviceOperationNameField.getText());
        pingStateChanging();
      }
    });

    Label wsdlStyleLabel = new Label(parent, SWT.NONE);
    wsdlStyleLabel.setText(Texts.get("WsdlStyle"));
    m_wsdlStyleRadioComposite = new Composite(parent, SWT.NONE);
    m_wsdlStyleRadioComposite.setLayout(new GridLayout(1, true));
    for (WsdlStyleEnum wsdlStyleEnum : WsdlStyleEnum.values()) {
      createWsdlStyleRadioButton(wsdlStyleEnum);
    }

    // layout
    parent.setLayout(new FormLayout());

    Control previousWidget = null;
    if (!isShowOnlyWsdlProperties()) {
      FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      m_wsdlNameField.setLayoutData(formData);

      formData = new FormData();
      formData.top = new FormAttachment(m_wsdlNameField, 5, SWT.BOTTOM);
      formData.left = new FormAttachment(40, 5);
      formData.right = new FormAttachment(100, 0);
      m_deriveOtherNameButton.setLayoutData(formData);

      formData = new FormData();
      formData.top = new FormAttachment(m_deriveOtherNameButton, 20, SWT.BOTTOM);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      m_alias.setLayoutData(formData);

      formData = new FormData();
      formData.top = new FormAttachment(m_alias, 5, SWT.BOTTOM);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      m_urlPattern.setLayoutData(formData);
      previousWidget = m_urlPattern;
    }

    FormData formData = new FormData();
    if (isShowOnlyWsdlProperties()) {
      formData.top = new FormAttachment(0, 0);
    }
    else {
      formData.top = new FormAttachment(previousWidget, 30, SWT.BOTTOM);
    }
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_targetNamespaceField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_targetNamespaceField, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_serviceNameField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_serviceNameField, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_portNameField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_portNameField, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_bindingNameField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_bindingNameField, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_portTypeNameField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_portTypeNameField, 30, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_serviceOperationNameField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_serviceOperationNameField, 30, SWT.BOTTOM);
    formData.right = new FormAttachment(40, 0);
    wsdlStyleLabel.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(wsdlStyleLabel, -5, SWT.TOP);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_wsdlStyleRadioComposite.setLayoutData(formData);
  }

  protected void createWsdlStyleRadioButton(WsdlStyleEnum wsdlStyleEnum) {
    Button button = new Button(m_wsdlStyleRadioComposite, SWT.RADIO);
    button.setData("ID", wsdlStyleEnum);
    button.setText(wsdlStyleEnum.getLabel());
    button.setSelection((wsdlStyleEnum == getWsdlStyle()));
    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setWsdlStyle((WsdlStyleEnum) e.widget.getData("ID"));
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // layout
    button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_wsdlStyleRadioButtons.add(button);
  }

  @Override
  protected void validatePage(final MultiStatus multiStatus) {
    if (!isControlCreated()) {
      return;
    }

    // WSDL name
    if (!isShowOnlyWsdlProperties()) {
      if (StringUtility.isNullOrEmpty(getWsdlName())) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", m_wsdlNameField.getLabelText())));
      }
      IStatus validationStatus = m_bundle.getProject().getWorkspace().validateName(getWsdlName(), IResource.FILE);
      multiStatus.add(validationStatus);

      // alias
      if (StringUtility.isNullOrEmpty(getAlias())) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", m_alias.getLabelText())));
      }
      else if (m_illegalAliasNames.contains(getAlias())) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XWithYDoesAlreadyExist", m_alias.getLabelText(), getAlias())));
      }
      else {
        IStatus status = m_bundle.getProject().getWorkspace().validateName(getAlias(), IResource.FILE);
        if (status.isOK() == false) {
          multiStatus.add(status);
        }
      }

      final String urlPattern = getUrlPattern();
      UrlPatternValidator.validate(urlPattern, m_jaxWsServletAlias, new IUrlPatternValidation() {

        @Override
        public void onWrongSeparators() {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, "Invalid URL pattern '" + urlPattern + "'. Must start with a slash with no empty segments and no trailing slash."));
        }

        @Override
        public void onNotStartingWithServletAlias() {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XshouldStartWithY", m_urlPattern.getLabelText(), m_jaxWsServletAlias)));
        }

        @Override
        public void onIllegalCharacters() {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("InvalidUrlX", urlPattern)));
        }

        @Override
        public void onEmpty() {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", m_urlPattern.getLabelText())));
        }
      });
      if (m_illegalUrlPatterns.contains(urlPattern)) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XWithYDoesAlreadyExist", m_urlPattern.getLabelText(), urlPattern)));
      }
    }

    // targetName
    validateTargetNamespace(multiStatus);
    // service name
    validateJavaField(multiStatus, m_serviceNameField.getLabelText(), getServiceName());
    // port name
    validateJavaField(multiStatus, m_portNameField.getLabelText(), getPortName());
    // binding
    validateJavaField(multiStatus, m_bindingNameField.getLabelText(), getBinding());
    // port type
    validateJavaField(multiStatus, m_portTypeNameField.getLabelText(), getPortTypeName());
    // service operation name
    if (StringUtility.isNullOrEmpty(getServiceOperationName())) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", m_serviceOperationNameField.getLabelText())));
    }
    else {
      IStatus validationStatus = JavaConventionsUtil.validateMethodName(getServiceOperationName(), m_bundle.getJavaProject());
      multiStatus.add(validationStatus);
    }
  }

  public void setWsdlName(String wsdlName) {
    try {
      setStateChanging(true);
      setWsdlNameInternal(wsdlName);
      if (isControlCreated()) {
        m_wsdlNameField.setText(wsdlName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setWsdlNameInternal(String wsdlName) {
    m_propertySupport.setPropertyString(PROP_WSDL_NAME, wsdlName);
  }

  public String getWsdlName() {
    return m_propertySupport.getPropertyString(PROP_WSDL_NAME);
  }

  public void setDeriveOtherNames(boolean deriveOtherNames) {
    try {
      setStateChanging(true);
      setDeriveOtherNamesInternal(deriveOtherNames);
      if (isControlCreated()) {
        m_deriveOtherNameButton.setSelection(deriveOtherNames);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setDeriveOtherNamesInternal(boolean deriveOtherNames) {
    m_propertySupport.setPropertyBool(PROP_DERIVE_OTHER_NAME, deriveOtherNames);
  }

  public boolean isDeriveOtherNames() {
    return m_propertySupport.getPropertyBool(PROP_DERIVE_OTHER_NAME);
  }

  public void setAlias(String alias) {
    try {
      setStateChanging(true);
      setAliasInternal(alias);
      if (isControlCreated()) {
        m_alias.setText(alias);
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
        m_urlPattern.setText(urlPattern);
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

  public void setJaxWsServletAlias(String jaxWsServletAlias) {
    m_jaxWsServletAlias = jaxWsServletAlias;
    if (isControlCreated()) {
      m_urlPattern.setReadOnlyPrefix(new Path(PathNormalizer.toServletAlias(m_jaxWsServletAlias)).addTrailingSeparator().toString());
    }
  }

  public void setTargetNamespace(String targetNamespace) {
    try {
      setStateChanging(true);
      setTargetNamespaceInternal(targetNamespace);
      if (isControlCreated()) {
        m_targetNamespaceField.setText(targetNamespace);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTargetNamespaceInternal(String targetNamespace) {
    m_propertySupport.setPropertyString(PROP_TARGET_NAMESPACE, targetNamespace);
  }

  public String getTargetNamespace() {
    return m_propertySupport.getPropertyString(PROP_TARGET_NAMESPACE);
  }

  public void setBindingName(String bindingName) {
    try {
      setStateChanging(true);
      setBindingNameInternal(bindingName);
      if (isControlCreated()) {
        m_bindingNameField.setText(bindingName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setBindingNameInternal(String bindingName) {
    m_propertySupport.setPropertyString(PROP_BINDING_NAME, bindingName);
  }

  public String getBinding() {
    return m_propertySupport.getPropertyString(PROP_BINDING_NAME);
  }

  public void setServiceName(String serviceName) {
    try {
      setStateChanging(true);
      setServiceNameInternal(serviceName);
      if (isControlCreated()) {
        m_serviceNameField.setText(serviceName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setServiceNameInternal(String serviceName) {
    m_propertySupport.setPropertyString(PROP_SERVICE_NAME, serviceName);
  }

  public String getServiceName() {
    return m_propertySupport.getPropertyString(PROP_SERVICE_NAME);
  }

  public void setPortTypeName(String portTypeName) {
    try {
      setStateChanging(true);
      setPortTypeNameInternal(portTypeName);
      if (isControlCreated()) {
        m_portTypeNameField.setText(portTypeName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setPortTypeNameInternal(String portTypeName) {
    m_propertySupport.setPropertyString(PROP_PORT_TYPE_NAME, portTypeName);
  }

  public String getPortTypeName() {
    return m_propertySupport.getPropertyString(PROP_PORT_TYPE_NAME);
  }

  public void setPortName(String portName) {
    try {
      setStateChanging(true);
      setPortNameInternal(portName);
      if (isControlCreated()) {
        m_portNameField.setText(portName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setPortNameInternal(String portName) {
    m_propertySupport.setPropertyString(PROP_PORT_NAME, portName);
  }

  public String getPortName() {
    return m_propertySupport.getPropertyString(PROP_PORT_NAME);
  }

  public void setServiceOperationName(String serviceOperationName) {
    try {
      setStateChanging(true);
      setServiceOperationNameInternal(serviceOperationName);
      if (isControlCreated()) {
        m_serviceOperationNameField.setText(serviceOperationName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setServiceOperationNameInternal(String serviceOperationName) {
    m_propertySupport.setPropertyString(PROP_SERVICE_OPERATION_NAME, serviceOperationName);
  }

  public String getServiceOperationName() {
    return m_propertySupport.getPropertyString(PROP_SERVICE_OPERATION_NAME);
  }

  public void setWsdlStyle(WsdlStyleEnum wsdlStyleEnum) {
    try {
      setStateChanging(true);
      setWsdlStyleInternal(wsdlStyleEnum);
      if (isControlCreated()) {
        for (Button radioButton : m_wsdlStyleRadioButtons) {
          radioButton.setSelection(wsdlStyleEnum == (WsdlStyleEnum) radioButton.getData("ID"));
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setWsdlStyleInternal(WsdlStyleEnum wsdlStyleEnum) {
    m_propertySupport.setProperty(PROP_WSDL_STYLE, wsdlStyleEnum);
  }

  public WsdlStyleEnum getWsdlStyle() {
    return (WsdlStyleEnum) m_propertySupport.getProperty(PROP_WSDL_STYLE);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  private void validateTargetNamespace(MultiStatus multiStatus) {
    if (StringUtility.isNullOrEmpty(m_targetNamespaceField.getText())) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", m_targetNamespaceField.getLabelText())));
    }
    else {
      try {
        URIUtil.toURL(URIUtil.fromString(m_targetNamespaceField.getText()));
      }
      catch (Exception e) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("TargetNamespaceMustBeValudUrl")));
      }

      if (!m_targetNamespaceField.getText().endsWith("/")) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, "Target Namespace must end with a slash"));
      }
    }
  }

  private void validateJavaField(MultiStatus multiStatus, String label, String text) {
    if (StringUtility.isNullOrEmpty(text)) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", label)));
      return;
    }
    IStatus validationStatus = m_bundle.getProject().getWorkspace().validateName(text, IResource.FILE);
    multiStatus.add(validationStatus);

    validationStatus = JavaConventionsUtil.validateJavaTypeName(text, m_bundle.getJavaProject());
    multiStatus.add(validationStatus);

    if (Character.isLowerCase(text.charAt(0))) {
      multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("UpperCaseHint")));
    }
  }

  private void deriveOtherNames() {
    if (!isDeriveOtherNames()) {
      return;
    }

    String serviceName = getWsdlName().substring(0, getWsdlName().length() - ".wsdl".length());
    setTargetNamespace(JaxWsSdkUtility.getRecommendedTargetNamespace(m_bundle, serviceName));
    setServiceName(serviceName);
    setPortName(serviceName + "Port");
    setBindingName(serviceName + "PortSoapBinding");
    setPortTypeName(serviceName + "PortType");
    setAlias(serviceName);
    setUrlPattern(serviceName);

    setServiceOperationName(NamingUtility.ensureStartWithLowerCase(serviceName.substring(0, serviceName.length() - "WebService".length())));
  }

  private void loadIllegalValues() {
    Set<String> illegalAliases = new HashSet<String>();
    Set<String> illegalUrlPatterns = new HashSet<String>();

    if (m_sunJaxWsXml != null) {
      String fqn = StringUtility.join(":", JaxWsSdkUtility.getXmlPrefix(m_sunJaxWsXml.getDocumentElement()), SunJaxWsBean.XML_ENDPOINT);
      for (Element xmlSunJaxWs : JaxWsSdkUtility.getChildElements(m_sunJaxWsXml.getDocumentElement().getChildNodes(), fqn)) {
        SunJaxWsBean sunJaxWsBean = new SunJaxWsBean(xmlSunJaxWs);
        illegalAliases.add(sunJaxWsBean.getAlias());
        illegalUrlPatterns.add(sunJaxWsBean.getUrlPattern());
      }
    }

    m_illegalAliasNames = illegalAliases;
    m_illegalUrlPatterns = illegalUrlPatterns;
  }

  public boolean isShowOnlyWsdlProperties() {
    return m_showOnlyWsdlProperties;
  }

  public void setShowOnlyWsdlProperties(boolean showOnlyWsdlProperties) {
    m_showOnlyWsdlProperties = showOnlyWsdlProperties;
  }
}
