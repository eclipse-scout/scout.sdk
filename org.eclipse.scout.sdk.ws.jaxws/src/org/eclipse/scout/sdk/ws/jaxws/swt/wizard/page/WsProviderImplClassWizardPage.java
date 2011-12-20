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
import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.tooltip.JavadocTooltip;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

@SuppressWarnings("restriction")
public class WsProviderImplClassWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_PACKAGE = "package";
  public static final String PROP_TYPE_NAME = "name";
  public static final String PROP_ANNOTATE_IMPL = "createScoutWsAnnotation";
  public static final String PROP_SESSION = "session";
  public static final String PROP_AUTHENTICATION_HANDLER = "authenticationHandler";
  public static final String PROP_CREDENTIAL_VALIDATION_STRATEGY = "credentialValidationStrategy";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;

  private Button m_createImplClassButton;
  private StyledTextField m_packageField;
  private Button m_packageBrowseButton;
  private StyledTextField m_typeNameField;
  private Button m_annotateImplButton;
  private Text m_descriptionScoutAnnotation;
  private Text m_descriptionSessionFactory;
  private Text m_descriptionAuthenticationHandler;
  private Text m_descriptionCredentialValidationStrategy;
  private Composite m_containerAnnotation;
  private StyledTextField m_sessionFactoryField;
  private Button m_sessionFactoryBrowseButton;
  private StyledTextField m_authenticationHandlerField;
  private Button m_authenticationHandlerBrowseButton;
  private StyledTextField m_credentialValidationStrategyField;
  private Button m_credentialValidationStrategyButton;

  private JavadocTooltip m_tooltipSessionFactory;
  private JavadocTooltip m_tooltipAuthenticationHandler;
  private JavadocTooltip m_tooltipCredentialValidationStrategy;

  public WsProviderImplClassWizardPage(IScoutBundle bundle) {
    super(WsProviderImplClassWizardPage.class.getName());
    setTitle(Texts.get("ConfigureImplementingClass"));
    setDescription(Texts.get("ConfigureImplementingClass"));

    m_propertySupport = new BasicPropertySupport(this);
    m_bundle = bundle;
    applyDefaults();
  }

  private void applyDefaults() {
    setAnnotateImplClass(true);
    setSessionFactory(JaxWsRuntimeClasses.DefaultServerSessionFactory.getFullyQualifiedName());
    setAuthenticationHandler(JaxWsRuntimeClasses.BasicAuthenticationHandlerProvider.getFullyQualifiedName());
    setCredentialValidationStrategy(JaxWsRuntimeClasses.ConfigIniCredentialValidationStrategy.getFullyQualifiedName());

    setPackageName(JaxWsSdkUtility.getRecommendedProviderImplPackageName(m_bundle));
  }

  @Override
  public void postActivate() {
    // set value to intall tooltip
    setSessionFactory(getSessionFactory());
    setAuthenticationHandler(getAuthenticationHandler());
    setCredentialValidationStrategy(getCredentialValidationStrategy());
  }

  @Override
  protected void createContent(Composite parent) {
    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("Name"));
    m_typeNameField.setReadOnlySuffix(JaxWsConstants.SUFFIX_WS_PROVIDER);
    m_typeNameField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_packageField = getFieldToolkit().createStyledTextField(parent, Texts.get("Package"));
    m_packageField.setText(getPackageName());
    m_packageField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setPackageNameInternal(m_packageField.getText());
        pingStateChanging();
      }
    });

    m_packageBrowseButton = new Button(parent, SWT.PUSH);
    m_packageBrowseButton.setText(Texts.get("Browse"));
    m_packageBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IPackageFragment[] packageFragment = openBrowsePackagesDialog();
        if (packageFragment != null && packageFragment.length > 0) {
          setPackageName(packageFragment[0].getElementName());
        }
      }
    });

    m_annotateImplButton = new Button(parent, SWT.CHECK);
    m_annotateImplButton.setText(Texts.get("AnnotateImplClass", JaxWsRuntimeClasses.ScoutWebService.getElementName()));
    m_annotateImplButton.setSelection(isAnnotateImplClass());
    m_annotateImplButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        setAnnotateImplClass(m_annotateImplButton.getSelection());
      }
    });

    m_containerAnnotation = new Composite(parent, SWT.NONE);

    m_descriptionScoutAnnotation = new Text(m_containerAnnotation, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
    m_descriptionScoutAnnotation.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    m_descriptionScoutAnnotation.setText(Texts.get("DescriptionScoutWebServiceAnnotation", JaxWsRuntimeClasses.ScoutWebService.getElementName()));

    m_descriptionSessionFactory = new Text(m_containerAnnotation, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
    m_descriptionSessionFactory.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    m_descriptionSessionFactory.setText(Texts.get("DescriptionSessionFactory"));

    m_sessionFactoryField = getFieldToolkit().createStyledTextField(m_containerAnnotation, Texts.get("SessionFactory"));
    m_sessionFactoryField.setText(getSessionFactory());
    m_sessionFactoryField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setSessionFactoryInternal(m_sessionFactoryField.getText());
        pingStateChanging();
      }
    });
    m_tooltipSessionFactory = new JavadocTooltip(m_sessionFactoryField.getTextComponent());

    m_sessionFactoryBrowseButton = new Button(m_containerAnnotation, SWT.PUSH);
    m_sessionFactoryBrowseButton.setText(Texts.get("Browse"));
    m_sessionFactoryBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          IJavaSearchScope searchScope = createSubClassesSearchScope(JaxWsRuntimeClasses.IServerSessionFactory);

          SelectionDialog dialog = JavaUI.createTypeDialog(ScoutSdkUi.getShell(), null, searchScope, IJavaElementSearchConstants.CONSIDER_CLASSES, false, "*.*");
          dialog.setTitle(Texts.get("SessionFactory"));
          dialog.setMessage(Texts.get("ChooseXY", Texts.get("SessionFactory")));
          dialog.setBlockOnOpen(true);
          if (dialog.open() == Window.OK) {
            if (dialog.getResult() != null) {
              IType type = (IType) dialog.getResult()[0];
              setSessionFactory(type.getFullyQualifiedName());
            }
          }
        }
        catch (JavaModelException exception) {
          JaxWsSdk.logError(exception);
        }
      }
    });

    m_descriptionAuthenticationHandler = new Text(m_containerAnnotation, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
    m_descriptionAuthenticationHandler.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    m_descriptionAuthenticationHandler.setText(Texts.get("DescriptionAuthenticationMechanism"));

    m_authenticationHandlerField = getFieldToolkit().createStyledTextField(m_containerAnnotation, Texts.get("AuthenticationMechanism"));
    m_authenticationHandlerField.setText(getAuthenticationHandler());
    m_authenticationHandlerField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setAuthenticationHandlerInternal(m_authenticationHandlerField.getText());
        pingStateChanging();
      }
    });
    m_tooltipAuthenticationHandler = new JavadocTooltip(m_authenticationHandlerField.getTextComponent());

    m_authenticationHandlerBrowseButton = new Button(m_containerAnnotation, SWT.PUSH);
    m_authenticationHandlerBrowseButton.setText(Texts.get("Browse"));
    m_authenticationHandlerBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          IJavaSearchScope searchScope = createSubClassesSearchScope(JaxWsRuntimeClasses.IAuthenticationHandlerProvider);

          SelectionDialog dialog = JavaUI.createTypeDialog(ScoutSdkUi.getShell(), null, searchScope, IJavaElementSearchConstants.CONSIDER_CLASSES, false, "*.*");
          dialog.setTitle(Texts.get("AuthenticationMechanism"));
          dialog.setMessage(Texts.get("ChooseXY", Texts.get("AuthenticationMechanism")));
          dialog.setBlockOnOpen(true);
          if (dialog.open() == Window.OK) {
            if (dialog.getResult() != null) {
              IType type = (IType) dialog.getResult()[0];
              setAuthenticationHandler(type.getFullyQualifiedName());
            }
          }
        }
        catch (JavaModelException exception) {
          JaxWsSdk.logError(exception);
        }
      }
    });

    m_descriptionCredentialValidationStrategy = new Text(m_containerAnnotation, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
    m_descriptionCredentialValidationStrategy.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    m_descriptionCredentialValidationStrategy.setText(Texts.get("DescriptionCredentialValidationValidation"));

    m_credentialValidationStrategyField = getFieldToolkit().createStyledTextField(m_containerAnnotation, Texts.get("CredentialValidationStrategy"));
    m_credentialValidationStrategyField.setText(getCredentialValidationStrategy());
    m_credentialValidationStrategyField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setCredentialValidationStrategyInternal(m_credentialValidationStrategyField.getText());
        pingStateChanging();
      }
    });
    m_tooltipCredentialValidationStrategy = new JavadocTooltip(m_credentialValidationStrategyField.getTextComponent());

    m_credentialValidationStrategyButton = new Button(m_containerAnnotation, SWT.PUSH);
    m_credentialValidationStrategyButton.setText(Texts.get("Browse"));
    m_credentialValidationStrategyButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          IJavaSearchScope searchScope = createSubClassesSearchScope(JaxWsRuntimeClasses.ICredentialValidationStrategy);

          SelectionDialog dialog = JavaUI.createTypeDialog(ScoutSdkUi.getShell(), null, searchScope, IJavaElementSearchConstants.CONSIDER_CLASSES, false, "*.*");
          dialog.setTitle(Texts.get("CredentialValidationStrategy"));
          dialog.setMessage(Texts.get("ChooseXY", Texts.get("CredentialValidationStrategy")));
          dialog.setBlockOnOpen(true);
          if (dialog.open() == Window.OK) {
            if (dialog.getResult() != null) {
              IType type = (IType) dialog.getResult()[0];
              setCredentialValidationStrategy(type.getFullyQualifiedName());
            }
          }
        }
        catch (JavaModelException exception) {
          JaxWsSdk.logError(exception);
        }
      }
    });
    // layout
    parent.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_typeNameField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_typeNameField, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_packageField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_packageField, 0, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_packageBrowseButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_packageField, 20, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_annotateImplButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_annotateImplButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_containerAnnotation.setLayoutData(formData);

    m_containerAnnotation.setLayout(new FormLayout());

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(40, 2);
    formData.right = new FormAttachment(100, 0);
    m_descriptionScoutAnnotation.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_descriptionScoutAnnotation, 10, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 2);
    formData.right = new FormAttachment(100, 0);
    m_descriptionSessionFactory.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_descriptionSessionFactory, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_sessionFactoryField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_sessionFactoryField, 0, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_sessionFactoryBrowseButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_sessionFactoryBrowseButton, 10, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 2);
    formData.right = new FormAttachment(100, 0);
    m_descriptionAuthenticationHandler.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_descriptionAuthenticationHandler, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_authenticationHandlerField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_authenticationHandlerField, 0, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_authenticationHandlerBrowseButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_authenticationHandlerBrowseButton, 10, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 2);
    formData.right = new FormAttachment(100, 0);
    m_descriptionCredentialValidationStrategy.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_descriptionCredentialValidationStrategy, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_credentialValidationStrategyField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_credentialValidationStrategyField, 0, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_credentialValidationStrategyButton.setLayoutData(formData);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (m_bundle == null) { // not fully initialized yet
      return;
    }

    validateType(multiStatus);

    if (isAnnotateImplClass()) {
      validateFactoryType(multiStatus, Texts.get("SessionFactory"), getSessionFactory(), JaxWsRuntimeClasses.IServerSessionFactory, JaxWsSdkUtility.getRecommendedSessionPackageName(m_bundle));
      validateFactoryType(multiStatus, Texts.get("AuthenticationFactory"), getAuthenticationHandler(), JaxWsRuntimeClasses.IAuthenticationHandlerProvider, JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
      validateFactoryType(multiStatus, Texts.get("CredentialValidationFactory"), getCredentialValidationStrategy(), JaxWsRuntimeClasses.ICredentialValidationStrategy, JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
    }
  }

  protected void validateType(MultiStatus multiStatus) {
    // package
    if (StringUtility.isNullOrEmpty(getPackageName())) {
      multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("UsageOfDefaultPackageDiscouraged")));
    }
    else {
      multiStatus.add(JavaConventionsUtil.validatePackageName(getPackageName(), m_bundle.getJavaProject()));

      String recommendedPackageName = JaxWsSdkUtility.getRecommendedProviderImplPackageName(m_bundle);
      if (!getPackageName().equals(recommendedPackageName)) {
        multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("ByConventionXShouldByY", Texts.get("Package"), recommendedPackageName)));
      }
    }
    // name
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(JaxWsConstants.SUFFIX_WS_PROVIDER)) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("MissingNameForImplementingClass")));
    }
    else {
      multiStatus.add(JavaConventionsUtil.validateJavaTypeName(getTypeName(), m_bundle.getJavaProject()));

      if (Character.isLowerCase(getTypeName().charAt(0))) {
        multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("LowerCaseTypeName")));
      }
    }

    // type
    if (multiStatus.getSeverity() < IStatus.ERROR) {
      String fullyQualifiedName = StringUtility.join(".", getPackageName(), getTypeName());
      try {
        if (TypeUtility.existsType(fullyQualifiedName)) {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("TypeAlreadyExsits", fullyQualifiedName)));
        }
      }
      catch (Exception e) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("InvalidJavaType", fullyQualifiedName)));
      }
    }
  }

  protected void validateFactoryType(MultiStatus multiStatus, String label, String qualifiedName, IType interfaceType, String recommendedPackage) {
    if (StringUtility.isNullOrEmpty(qualifiedName)) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("NoClassSpecified", label)));
      return;
    }

    String packageName = Signature.getQualifier(qualifiedName);
    String className;
    if (StringUtility.isNullOrEmpty(packageName)) {
      className = qualifiedName;
    }
    else {
      className = Signature.getSimpleName(qualifiedName);
    }

    if (StringUtility.isNullOrEmpty(packageName)) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("UsageOfDefaultPackageNotAllowed")));
      return;
    }
    else {
      multiStatus.add(JavaConventionsUtil.validatePackageName(packageName, m_bundle.getJavaProject()));
    }

    try {
      String fqn = StringUtility.join(".", packageName, className);
      if (TypeUtility.existsType(fqn)) {
        IType type = TypeUtility.getType(fqn);
        if (!type.newSupertypeHierarchy(new NullProgressMonitor()).contains(interfaceType)) {
          type.getJavadocRange().getOffset();
          type.getJavadocRange().getLength();
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustBeOfTheTypeY", label, interfaceType.getFullyQualifiedName())));
        }
      }
      else {
        if (!packageName.equals(recommendedPackage)) {
          multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("ByConventionXShouldByY", Texts.get("package"), recommendedPackage)));
        }
      }
    }
    catch (Exception e) {
      // nop
    }
  }

  public void setTypeName(String typeName) {
    try {
      typeName = JaxWsSdkUtility.toStartWithUpperCase(typeName);

      setStateChanging(true);
      setTypeNameInternal(typeName);
      if (isControlCreated()) {
        m_typeNameField.setText(typeName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTypeNameInternal(String typeName) {
    m_propertySupport.setProperty(PROP_TYPE_NAME, typeName);
  }

  public String getTypeName() {
    return m_propertySupport.getPropertyString(PROP_TYPE_NAME);
  }

  public void setPackageName(String packageName) {
    try {
      setStateChanging(true);
      setPackageNameInternal(packageName);
      if (isControlCreated()) {
        m_packageField.setText(packageName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setPackageNameInternal(String packageName) {
    m_propertySupport.setPropertyString(PROP_PACKAGE, packageName);
  }

  public String getPackageName() {
    return m_propertySupport.getPropertyString(PROP_PACKAGE);
  }

  public void setAnnotateImplClass(boolean annotateImplClass) {
    try {
      setStateChanging(true);
      setAnnotateImplClassInternal(annotateImplClass);
      if (isControlCreated()) {
        m_annotateImplButton.setSelection(annotateImplClass);
        JaxWsSdkUtility.setView(m_containerAnnotation, annotateImplClass);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setAnnotateImplClassInternal(boolean annotateImplClass) {
    m_propertySupport.setProperty(PROP_ANNOTATE_IMPL, annotateImplClass);
  }

  public boolean isAnnotateImplClass() {
    return m_propertySupport.getPropertyBool(PROP_ANNOTATE_IMPL);
  }

  public void setSessionFactory(String sessionFactory) {
    try {
      setStateChanging(true);
      setSessionFactoryInternal(sessionFactory);
      if (isControlCreated()) {
        m_sessionFactoryField.setText(sessionFactory);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setSessionFactoryInternal(String sessionFactory) {
    m_propertySupport.setPropertyString(PROP_SESSION, sessionFactory);

    if (isControlCreated()) {
      // display JavaDoc
      try {
        m_tooltipSessionFactory.setMember(null);

        if (TypeUtility.existsType(sessionFactory)) {
          IType type = TypeUtility.getType(sessionFactory);
          if (type.newSupertypeHierarchy(new NullProgressMonitor()).contains(JaxWsRuntimeClasses.IServerSessionFactory)) {
            m_tooltipSessionFactory.setMember(type);
          }
        }
      }
      catch (Exception e) {
        JaxWsSdk.logWarning("Could not render tooltip", e);
      }
    }
  }

  public String getSessionFactory() {
    return m_propertySupport.getPropertyString(PROP_SESSION);
  }

  public void setAuthenticationHandler(String authenticationHandler) {
    try {
      setStateChanging(true);
      setAuthenticationHandlerInternal(authenticationHandler);
      if (isControlCreated()) {
        m_authenticationHandlerField.setText(authenticationHandler);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setAuthenticationHandlerInternal(String authenticationHandler) {
    m_propertySupport.setPropertyString(PROP_AUTHENTICATION_HANDLER, authenticationHandler);

    if (isControlCreated()) {
      // display JavaDoc
      m_tooltipAuthenticationHandler.setMember(null);
      if (TypeUtility.existsType(authenticationHandler)) {
        IType type = TypeUtility.getType(authenticationHandler);
        try {
          if (type.newSupertypeHierarchy(new NullProgressMonitor()).contains(JaxWsRuntimeClasses.IAuthenticationHandlerProvider)) {
            m_tooltipAuthenticationHandler.setMember(type);
          }
        }
        catch (Exception e) {
          JaxWsSdk.logWarning("Could not render tooltip", e);
        }
      }
    }
  }

  public String getAuthenticationHandler() {
    return m_propertySupport.getPropertyString(PROP_AUTHENTICATION_HANDLER);
  }

  public void setCredentialValidationStrategy(String credentialValidationStrategy) {
    try {
      setStateChanging(true);
      setCredentialValidationStrategyInternal(credentialValidationStrategy);
      if (isControlCreated()) {
        m_credentialValidationStrategyField.setText(credentialValidationStrategy);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setCredentialValidationStrategyInternal(String credentialValidationStrategy) {
    m_propertySupport.setPropertyString(PROP_CREDENTIAL_VALIDATION_STRATEGY, credentialValidationStrategy);

    if (isControlCreated()) {
      // display JavaDoc
      m_tooltipCredentialValidationStrategy.setMember(null);
      if (TypeUtility.existsType(credentialValidationStrategy)) {
        IType type = TypeUtility.getType(credentialValidationStrategy);
        try {
          if (type.newSupertypeHierarchy(new NullProgressMonitor()).contains(JaxWsRuntimeClasses.ICredentialValidationStrategy)) {
            m_tooltipCredentialValidationStrategy.setMember(type);
          }
        }
        catch (Exception e) {
          JaxWsSdk.logWarning("Could not render tooltip", e);
        }
      }
    }
  }

  public String getCredentialValidationStrategy() {
    return m_propertySupport.getPropertyString(PROP_CREDENTIAL_VALIDATION_STRATEGY);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  private IPackageFragment[] openBrowsePackagesDialog() {
    IPackageFragment[] packageFragments = null;
    IRunnableContext context = new BusyIndicatorRunnableContext();
    SelectionDialog dialog = JavaUI.createPackageDialog(ScoutSdkUi.getShell(), context, m_bundle.getSearchScope(), false, true, null);
    dialog.setTitle(Texts.get("Package"));
    dialog.setMessage(Texts.get("ChoosePackageForImplementingClass"));

    if (dialog.open() == Window.OK) {
      if (dialog.getResult() != null) {
        packageFragments = Arrays.asList(dialog.getResult()).toArray(new IPackageFragment[0]);
      }
    }
    if (packageFragments != null) {
      return packageFragments;
    }
    else {
      return null;
    }
  }

  private IJavaSearchScope createSubClassesSearchScope(IType superType) {
    // do not use PrimaryTypeHierarchy to get subtypes due to static inner classes
    IType[] subTypes = JaxWsSdkUtility.getJdtSubTypes(m_bundle, superType.getFullyQualifiedName(), false, false, true, false);
    return SearchEngine.createJavaSearchScope(subTypes);
  }
}
