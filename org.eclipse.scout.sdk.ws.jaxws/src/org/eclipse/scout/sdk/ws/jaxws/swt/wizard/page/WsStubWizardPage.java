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
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
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
import org.eclipse.ui.dialogs.SelectionDialog;

@SuppressWarnings("restriction")
public class WsStubWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_STUB_FOLDER = "stubFolder";
  public static final String PROP_PACKAGE = "package";
  public static final String PROP_CREATE_BINDING_FILE = "createBindingFile";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;

  private StyledTextField m_packageField;
  private Button m_packageBrowseButton;
  private Button m_createBindingFileButton;

  private String m_serviceName;
  private String m_defaultPackageName;

  public WsStubWizardPage(IScoutBundle bundle) {
    super(WsStubWizardPage.class.getName());
    setTitle(Texts.get("ConfigureWebServiceStub"));
    setDescription(Texts.get("ConfigureWebServiceStub"));

    m_bundle = bundle;
    m_propertySupport = new BasicPropertySupport(this);
    applyDefaults();
  }

  private void applyDefaults() {
    setCreateBindingFile(true);
  }

  @Override
  protected void createContent(Composite parent) {
    m_packageField = getFieldToolkit().createStyledTextField(parent, Texts.get("StubPackage"));
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

    m_createBindingFileButton = new Button(parent, SWT.CHECK);
    m_createBindingFileButton.setText(Texts.get("CreateBindingFile"));
    m_createBindingFileButton.setSelection(isCreateBindingFile());
    m_createBindingFileButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        setCreateBindingFileInternal(m_createBindingFileButton.getSelection());
      }
    });

    // layout
    parent.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
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
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_createBindingFileButton.setLayoutData(formData);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (m_bundle == null) { // not fully initialized yet
      return;
    }

    validatePackage(multiStatus);
  }

  public void setPackageName(String packageName) {
    try {
      setStateChanging(true);
      setPackageNameInternal(packageName);
      if (isControlCreated()) {
        m_packageField.setText(StringUtility.nvl(packageName, ""));
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setPackageNameInternal(String packageName) {
    m_propertySupport.setProperty(PROP_PACKAGE, packageName);
  }

  public String getPackageName() {
    return m_propertySupport.getPropertyString(PROP_PACKAGE);
  }

  public String getDefaultPackageName() {
    return m_defaultPackageName;
  }

  public void setDefaultPackageName(String defaultPackageName) {
    m_defaultPackageName = defaultPackageName;
  }

  public void setCreateBindingFile(boolean createBindingFile) {
    try {
      setStateChanging(true);
      setCreateBindingFileInternal(createBindingFile);
      if (isControlCreated()) {
        m_createBindingFileButton.setSelection(createBindingFile);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setCreateBindingFileInternal(boolean createBindingFile) {
    m_propertySupport.setPropertyBool(PROP_CREATE_BINDING_FILE, createBindingFile);
  }

  public boolean isCreateBindingFile() {
    return m_propertySupport.getPropertyBool(PROP_CREATE_BINDING_FILE);
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
    IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[]{m_bundle.getJavaProject()});
    SelectionDialog dialog = JavaUI.createPackageDialog(ScoutSdkUi.getShell(), context, searchScope, false, true, null);
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

  private void validatePackage(MultiStatus multiStatus) {
    if (!StringUtility.hasText(getPackageName()) && m_defaultPackageName == null) {
      //in some rare situation, package name cannot be derived from target namespace / WSDL definition
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("JaxWsCannotDerivePackageNameFromTargetNamespace")));
    }

    if (!CompareUtility.equals(m_defaultPackageName, getPackageName())) {
      multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("ConventionPackageNameEqualsToTargetNamespaceX")));

      // validate custom package name
      multiStatus.add(JavaConventionsUtil.validatePackageName(getPackageName(), m_bundle.getJavaProject()));

      if (StringUtility.isNullOrEmpty(getPackageName())) {
        multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("UsageOfDefaultPackageDiscouraged")));
      }
    }
  }
}
