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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
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
import org.eclipse.scout.sdk.util.NamingUtility;
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
public class HandlerNewWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_TYPE_NAME = "typeName";
  public static final String PROP_PACKAGE_NAME = "packageName";
  public static final String PROP_TRANSACTIONAL = "transactional";
  public static final String PROP_SESSION_TYPE = "sessionFactory";
  public static final String PROP_SUPER_TYPE = "superType";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;

  private StyledTextField m_typeNameField;
  private StyledTextField m_superTypeField;
  private StyledTextField m_packageField;
  private Button m_packageBrowseButton;
  private Button m_superTypeBrowseButton;
  private JavadocTooltip m_tooltipSuperType;

  private Button m_transactionalButton;
  private StyledTextField m_sessionFactoryField;
  private Text m_descriptionSessionFactory;
  private Button m_sessionFactoryBrowseButton;
  private JavadocTooltip m_tooltipSessionFactory;

  public HandlerNewWizardPage(IScoutBundle bundle) {
    super(HandlerNewWizardPage.class.getName());
    setTitle(Texts.get("CreateHandler"));
    setDescription(Texts.get("ByClickingFinishHandlerIsCreated"));

    m_bundle = bundle;
    m_propertySupport = new BasicPropertySupport(this);
    setPackageName(JaxWsSdkUtility.getRecommendedHandlerPackageName(m_bundle));
    setTransactional(true);

    try {
      String defaultSessionFactory = (String) TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getMethod(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY, new String[0]).getDefaultValue().getValue();
      setSessionFactoryType(TypeUtility.getType(defaultSessionFactory));
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError(e);
    }
    setSuperType(TypeUtility.getType(SOAPHandler.class.getName()));
  }

  @Override
  protected void createContent(Composite parent) {
    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("Name"));
    m_typeNameField.setReadOnlySuffix(JaxWsConstants.SUFFIX_HANDLER);
    m_typeNameField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createStyledTextField(parent, Texts.get("SuperType"));
    if (TypeUtility.exists(getSuperType())) {
      m_superTypeField.setText(getSuperType().getElementName());
    }
    m_superTypeField.setEditable(false);
    m_tooltipSuperType = new JavadocTooltip(m_superTypeField.getTextComponent());
    updateJavaDoc(m_tooltipSuperType, getSuperType());

    m_superTypeBrowseButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_superTypeBrowseButton.setText(Texts.get("Browse"));
    m_superTypeBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          IJavaSearchScope searchScope = createSuperTypeSearchScope();

          SelectionDialog dialog = JavaUI.createTypeDialog(ScoutSdkUi.getShell(), null, searchScope, IJavaElementSearchConstants.CONSIDER_CLASSES | IJavaElementSearchConstants.CONSIDER_INTERFACES, false, "*.*");
          dialog.setTitle(Texts.get("SuperType"));
          dialog.setMessage(Texts.get("ChooseXY", Texts.get("SuperType")));
          dialog.setBlockOnOpen(true);
          if (dialog.open() == Window.OK) {
            if (dialog.getResult() != null) {
              IType type = (IType) dialog.getResult()[0];
              setSuperType(type);
            }
          }
        }
        catch (JavaModelException exception) {
          JaxWsSdk.logError(exception);
        }
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

    m_packageBrowseButton = new Button(parent, SWT.PUSH | SWT.FLAT);
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

    m_transactionalButton = new Button(parent, SWT.CHECK);
    m_transactionalButton.setText(Texts.get("RunHandlerInScoutTransaction"));
    m_transactionalButton.setSelection(isTransactional());
    m_transactionalButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        setTransactionalInternal(m_transactionalButton.getSelection());
        m_sessionFactoryBrowseButton.setEnabled(isTransactional());
        m_sessionFactoryField.setEnabled(isTransactional());
      }
    });

    m_descriptionSessionFactory = new Text(parent, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
    m_descriptionSessionFactory.setEnabled(false);
    m_descriptionSessionFactory.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    m_descriptionSessionFactory.setText(Texts.get("DescriptionHandlerSession"));

    m_sessionFactoryField = getFieldToolkit().createStyledTextField(parent, Texts.get("SessionFactory"));
    if (TypeUtility.exists(getSuperType())) {
      m_sessionFactoryField.setText(getSessionFactoryType().getElementName());
    }
    m_sessionFactoryField.setEditable(false);
    m_tooltipSessionFactory = new JavadocTooltip(m_sessionFactoryField.getTextComponent());
    updateJavaDoc(m_tooltipSessionFactory, getSessionFactoryType());

    m_sessionFactoryBrowseButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_sessionFactoryBrowseButton.setText(Texts.get("Browse"));
    m_sessionFactoryBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          IJavaSearchScope searchScope = createSubClassesSearchScope(TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory));

          SelectionDialog dialog = JavaUI.createTypeDialog(ScoutSdkUi.getShell(), null, searchScope, IJavaElementSearchConstants.CONSIDER_CLASSES, false, "*.*");
          dialog.setTitle(Texts.get("SessionFactory"));
          dialog.setMessage(Texts.get("ChooseXY", Texts.get("SessionFactory")));
          dialog.setBlockOnOpen(true);
          if (dialog.open() == Window.OK) {
            if (dialog.getResult() != null) {
              IType type = (IType) dialog.getResult()[0];
              setSessionFactoryType(type);
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
    formData.top = new FormAttachment(m_packageBrowseButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_superTypeField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_superTypeField, -2, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_superTypeBrowseButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_superTypeBrowseButton, 25, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_transactionalButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_transactionalButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 2);
    formData.right = new FormAttachment(100, 0);
    m_descriptionSessionFactory.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_descriptionSessionFactory, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_sessionFactoryField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_sessionFactoryField, -2, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_sessionFactoryBrowseButton.setLayoutData(formData);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    validateType(multiStatus);

    if (isTransactional() && !TypeUtility.exists(getSessionFactoryType())) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", Texts.get("SessionFactory"))));
    }
  }

  protected void validateType(MultiStatus multiStatus) {
    // package
    if (StringUtility.isNullOrEmpty(getPackageName())) {
      multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("UsageOfDefaultPackageDiscouraged")));
    }
    else {
      multiStatus.add(JavaConventionsUtil.validatePackageName(getPackageName(), m_bundle.getJavaProject()));

      String recommendedPackageName = JaxWsSdkUtility.getRecommendedHandlerPackageName(m_bundle);
      if (!getPackageName().equals(recommendedPackageName)) {
        multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("ByConventionXShouldByY", Texts.get("Package"), recommendedPackageName)));
      }
    }

    // name
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(JaxWsConstants.SUFFIX_HANDLER)) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("XMustNotBeEmpty", Texts.get("Name"))));
    }
    else {
      multiStatus.add(JavaConventionsUtil.validateJavaTypeName(getTypeName(), m_bundle.getJavaProject()));
      if (Character.isLowerCase(getTypeName().charAt(0))) {
        multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("LowerCaseTypeName")));
      }
    }
    if (multiStatus.getSeverity() < IStatus.ERROR) {
      try {
        String fqn = StringUtility.join(".", getPackageName(), getTypeName());
        if (TypeUtility.existsType(fqn)) {
          multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("TypeAlreadyExsits", fqn)));
        }
      }
      catch (Exception e) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("InvalidJavaType", getTypeName())));
      }
    }
  }

  public void setTransactional(boolean transactional) {
    try {
      setStateChanging(true);
      setTransactionalInternal(transactional);
      if (isControlCreated()) {
        m_transactionalButton.setSelection(transactional);
        m_sessionFactoryBrowseButton.setEnabled(isTransactional());
        m_sessionFactoryField.setEnabled(isTransactional());
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTransactionalInternal(boolean transactional) {
    m_propertySupport.setPropertyBool(PROP_TRANSACTIONAL, transactional);
  }

  public boolean isTransactional() {
    return m_propertySupport.getPropertyBool(PROP_TRANSACTIONAL);
  }

  private void updateJavaDoc(JavadocTooltip javadocTooltip, IType type) {
    try {
      javadocTooltip.setMember(null);
      if (TypeUtility.exists(type)) {
        javadocTooltip.setMember(type);
      }
    }
    catch (Exception e) {
      JaxWsSdk.logWarning("Could not render tooltip", e);
    }
  }

  public void setSessionFactoryType(IType sessionFactoryType) {
    try {
      setStateChanging(true);
      setSessionFactoryTypeInternal(sessionFactoryType);
      if (isControlCreated()) {
        if (sessionFactoryType != null) {
          m_sessionFactoryField.setText(sessionFactoryType.getElementName());
        }
        else {
          m_sessionFactoryField.setText("");
        }
        updateJavaDoc(m_tooltipSessionFactory, getSessionFactoryType());
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setSessionFactoryTypeInternal(IType sessionFactoryType) {
    m_propertySupport.setProperty(PROP_SESSION_TYPE, sessionFactoryType);
  }

  public IType getSessionFactoryType() {
    return (IType) m_propertySupport.getProperty(PROP_SESSION_TYPE);
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      setSuperTypeInternal(superType);
      if (isControlCreated()) {
        if (superType != null) {
          m_superTypeField.setText(superType.getElementName());
        }
        else {
          m_superTypeField.setText("");
        }
        updateJavaDoc(m_tooltipSuperType, getSuperType());
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setSuperTypeInternal(IType superType) {
    m_propertySupport.setProperty(PROP_SUPER_TYPE, superType);
  }

  public IType getSuperType() {
    return (IType) m_propertySupport.getProperty(PROP_SUPER_TYPE);
  }

  public void setTypeName(String typeName) {
    try {
      typeName = NamingUtility.ensureStartWithUpperCase(typeName);

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
    m_propertySupport.setPropertyString(PROP_PACKAGE_NAME, packageName);
  }

  public String getPackageName() {
    return m_propertySupport.getPropertyString(PROP_PACKAGE_NAME);
  }

  private IJavaSearchScope createSubClassesSearchScope(IType superType) {
    // do not use PrimaryTypeHierarchy to get subtypes due to static inner classes
    IType[] subTypes = JaxWsSdkUtility.getJdtSubTypes(m_bundle, superType.getFullyQualifiedName(), false, false, true, false);
    return SearchEngine.createJavaSearchScope(subTypes);
  }

  private IJavaSearchScope createSuperTypeSearchScope() {
    // do not use PrimaryTypeHierarchy to get subtypes due to static inner classes
    List<IType> types = new ArrayList<IType>();
    types.add(TypeUtility.getType(SOAPHandler.class.getName()));
    types.add(TypeUtility.getType(LogicalHandler.class.getName()));

    types.addAll(Arrays.asList(JaxWsSdkUtility.getJdtSubTypes(m_bundle, LogicalHandler.class.getName(), true, true, false, false)));
    types.addAll(Arrays.asList(JaxWsSdkUtility.getJdtSubTypes(m_bundle, SOAPHandler.class.getName(), true, true, false, false)));

    IType authHandlerProv = TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerProvider);
    IType authHandlerCons = TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerConsumer);

    // remove authentication handlers (provider)
    IType[] providerAuthHandlers = JaxWsSdkUtility.getJdtSubTypes(m_bundle, authHandlerProv.getFullyQualifiedName(), true, true, false, false);
    types.removeAll(Arrays.asList(providerAuthHandlers));
    types.remove(TypeUtility.getType(authHandlerProv.getFullyQualifiedName()));

    // remove authentication handlers (consumer)
    IType[] consumerAuthHandlers = JaxWsSdkUtility.getJdtSubTypes(m_bundle, authHandlerCons.getFullyQualifiedName(), true, true, false, false);
    types.removeAll(Arrays.asList(consumerAuthHandlers));
    types.remove(TypeUtility.getType(authHandlerCons.getFullyQualifiedName()));

    // remove internal classes
    Iterator<IType> iterator = types.iterator();
    while (iterator.hasNext()) {
      IType candidate = iterator.next();
      if (Signature.getQualifier(candidate.getFullyQualifiedName()).contains("internal")) {
        iterator.remove();
      }
    }

    JaxWsSdkUtility.sortTypesByName(types, true);

    return SearchEngine.createJavaSearchScope(types.toArray(new IJavaElement[types.size()]));
  }

  private IPackageFragment[] openBrowsePackagesDialog() {
    IPackageFragment[] packageFragments = null;
    IRunnableContext context = new BusyIndicatorRunnableContext();
    IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[]{m_bundle.getJavaProject()});
    SelectionDialog dialog = JavaUI.createPackageDialog(ScoutSdkUi.getShell(), context, searchScope, false, true, null);
    dialog.setTitle(Texts.get("Package"));
    dialog.setMessage(Texts.get("ChoosePackageForHandler"));

    if (dialog.open() == Window.OK) {
      if (dialog.getResult() != null) {
        packageFragments = Arrays.asList(dialog.getResult()).toArray(new IPackageFragment[0]);
      }
    }
    return packageFragments;
  }
}
