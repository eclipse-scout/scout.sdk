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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.OpenNewPackageWizardAction;
import org.eclipse.jdt.ui.wizards.NewPackageWizardPage;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;

@SuppressWarnings("restriction")
public class PackagePresenter extends AbstractPropertyPresenter<String> {

  private Text m_textField;
  private Button m_button;

  private ModifyListener m_modifyListener;
  private String m_sourceFolder;
  private boolean m_allowChangeOfSourceFolder;

  public PackagePresenter(Composite parent, FormToolkit toolkit) {
    super(parent, toolkit, false);
    setLabel(Texts.get("package"));
    setUseLinkAsLabel(true);
    m_modifyListener = new P_ModifyListener();
    callInitializer();
  }

  @Override
  protected Control createContent(Composite parent) {
    Composite composite = getToolkit().createComposite(parent, SWT.NONE);
    m_textField = getToolkit().createText(composite, "", SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
    m_textField.setEditable(true);
    m_textField.addModifyListener(m_modifyListener);
    m_textField.addFocusListener(new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        String packageName = m_textField.getText();
        if (validate(packageName)) {
          setValueFromUI(packageName);
        }
      }
    });

    m_button = new Button(composite, SWT.PUSH);
    m_button.setText(Texts.get("Browse") + "...");
    m_button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        String packageName = openPackageBrowserDialog();
        if (packageName == null) {
          return; // dialog canceled
        }
        if (validate(packageName)) {
          setInputInternal(packageName);
          setValueFromUI(packageName);
        }
      }
    });

    // layout
    composite.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(m_button, -3, SWT.LEFT);
    formData.bottom = new FormAttachment(100, 0);
    m_textField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    formData.bottom = new FormAttachment(100, 0);
    m_button.setLayoutData(formData);

    return composite;
  }

  @Override
  protected void execLinkAction() throws CoreException {
    IPackageFragment packageFragment = getPackageFragment(m_textField.getText());
    if (packageFragment != null) {
      try {
        JavaUI.openInEditor(packageFragment);
      }
      catch (Exception e) {
        JaxWsSdk.logWarning(e);
      }
    }
    else {
      String packageName = openPackageNewDialog();
      if (packageName != null) {
        setInputInternal(packageName);
        setValueFromUI(packageName, true);
      }
    }
  }

  @Override
  protected void setInputInternal(String input) {
    m_textField.removeModifyListener(m_modifyListener);
    try {
      m_textField.setText(StringUtility.nvl(input, ""));
    }
    finally {
      m_textField.addModifyListener(m_modifyListener);
    }
    validate(input);
  }

  private String openPackageNewDialog() throws JavaModelException {
    NewPackageWizardPage page = new NewPackageWizardPage();
    page.setDescription(Texts.get("CreateNewPackage"));
    page.setPackageText(m_textField.getText(), true);

    if (!isAllowChangeOfSourceFolder() && !JaxWsSdkUtility.isValidSourceFolder(m_bundle, m_sourceFolder)) {
      return null;
    }

    if (m_sourceFolder != null) {
      IPath sourceFolder = m_bundle.getProject().getFullPath().append(m_sourceFolder);
      IPackageFragmentRoot root = m_bundle.getJavaProject().findPackageFragmentRoot(sourceFolder);
      page.setPackageFragmentRoot(root, isAllowChangeOfSourceFolder());
    }

    OpenNewPackageWizardAction action = new OpenNewPackageWizardAction();
    action.setConfiguredWizardPage(page);
    action.run();

    IPackageFragment packageFragment = page.getNewPackageFragment();
    if (packageFragment != null && packageFragment.exists() && validate(packageFragment.getElementName())) {
      m_sourceFolder = page.getPackageFragmentRoot().getPath().lastSegment();
      return packageFragment.getElementName();
    }
    return null;
  }

  private String openPackageBrowserDialog() {
    IRunnableContext context = new BusyIndicatorRunnableContext();
    SelectionDialog dialog = JavaUI.createPackageDialog(ScoutSdkUi.getShell(), context, createSearchScopeForSourceFolder(), false, true, null);
    dialog.setTitle(Texts.get("CreateNewPackage"));

    if (dialog.open() == Window.OK && dialog.getResult() != null && dialog.getResult().length > 0) {
      IPackageFragment packageFragment = (IPackageFragment) dialog.getResult()[0];

      if (TypeUtility.exists(packageFragment)) {
        m_sourceFolder = packageFragment.getParent().getPath().lastSegment();
        return packageFragment.getElementName();
      }
    }
    return null;
  }

  private boolean validate(String packageName) {
    setUseLinkAsLabel(false);

    if (!JaxWsSdkUtility.isValidSourceFolder(m_bundle, m_sourceFolder)) {
      setInfo(IMarker.SEVERITY_ERROR, "Source folder not set set or does not exist");
      return false;
    }
    if (packageName == null) {
      clearInfo();
      return isAcceptNullValue();
    }
    try {
      IPath sourceFolder = m_bundle.getProject().getFullPath().append(m_sourceFolder);
      IPackageFragmentRoot root = m_bundle.getJavaProject().findPackageFragmentRoot(sourceFolder);
      if (!TypeUtility.exists(root)) {
        setInfo(IMarker.SEVERITY_ERROR, "Source folder could not be found");
        return false;
      }
      IPackageFragment packageFragment = root.getPackageFragment(packageName);
      if ((packageFragment != null && !packageFragment.exists()) || (packageFragment == null && !isAcceptNullValue())) {
        setUseLinkAsLabel(true);
        setInfo(IMarker.SEVERITY_WARNING, Texts.get("PackageDoesNotExistClickOnLinkToCreate"));
        return false;
      }
      clearInfo();
      return true;
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("failed to validate package name", e);
    }
    clearInfo();
    return false;
  }

  private IPackageFragment getPackageFragment(String packageName) {
    if (validate(packageName)) {
      IPath sourceFolder = m_bundle.getProject().getFullPath().append(m_sourceFolder);
      try {
        IPackageFragmentRoot root = m_bundle.getJavaProject().findPackageFragmentRoot(sourceFolder);
        return root.getPackageFragment(packageName);
      }
      catch (JavaModelException e) {
        JaxWsSdk.logError("failed to get package fragment", e);
      }
    }
    return null;
  }

  public String getSourceFolder() {
    return m_sourceFolder;
  }

  public void setSourceFolder(String sourceFolder) {
    m_sourceFolder = sourceFolder;
    if (isControlCreated()) {
      validate(m_textField.getText());
    }
  }

  public boolean isAllowChangeOfSourceFolder() {
    return m_allowChangeOfSourceFolder;
  }

  public void setAllowChangeOfSourceFolder(boolean allowChangeOfSourceFolder) {
    m_allowChangeOfSourceFolder = allowChangeOfSourceFolder;
  }

  private IJavaSearchScope createSearchScopeForSourceFolder() {
    if (JaxWsSdkUtility.isValidSourceFolder(m_bundle, m_sourceFolder)) {
      try {
        IPath sourceFolder = m_bundle.getProject().getFullPath().append(m_sourceFolder);
        IPackageFragmentRoot root = m_bundle.getJavaProject().findPackageFragmentRoot(sourceFolder);
        return SearchEngine.createJavaSearchScope(new IJavaElement[]{root});
      }
      catch (JavaModelException e) {
        JaxWsSdk.logError("failed to create Java search scope for package elements", e);
      }
    }
    return SearchEngine.createJavaSearchScope(new IJavaElement[0]); // empty search scope
  }

  private class P_ModifyListener implements ModifyListener {

    @Override
    public void modifyText(ModifyEvent e) {
      if (validate(m_textField.getText())) {
        setValueFromUI(m_textField.getText());
      }
    }
  }
}
