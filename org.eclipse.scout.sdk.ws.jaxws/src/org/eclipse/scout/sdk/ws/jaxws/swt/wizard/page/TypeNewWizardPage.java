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

import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.ui.dialogs.SelectionDialog;

@SuppressWarnings("restriction")
public class TypeNewWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_TYPE_NAME = "typeName";
  public static final String PROP_PACKAGE_NAME = "packageName";
  public static final String PROP_INTERFACE_TYPE = "interfaceType";
  public static final String PROP_SUPER_TYPE = "superType";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;

  private String m_elementName;
  private String m_typeSuffix;
  private String m_recommendedPackageFragment;
  private boolean m_allowModifyPackage;
  private boolean m_allowModifySuperType;
  private boolean m_allowModifyInterfaceType;
  private JavaSearchScopeFactory m_superTypeSearchScopeFactory;
  private JavaSearchScopeFactory m_interfaceTypeSearchScopeFactory;

  private StyledTextField m_typeNameField;
  private StyledTextField m_packageField;
  private Button m_packageBrowseButton;
  private StyledTextField m_superTypeField;
  private Button m_superTypeBrowseButton;
  private JavadocTooltip m_tooltipSuperType;
  private StyledTextField m_interfaceTypeField;
  private Button m_interfaceTypeBrowseButton;
  private JavadocTooltip m_tooltipInterfaceType;

  public TypeNewWizardPage(IScoutBundle bundle, String elementName) {
    super(TypeNewWizardPage.class.getName());
    setTitle(Texts.get("CreateNewX", elementName));
    setDescription(Texts.get("ByClickingFinishXIsCreated", getElementName()));

    m_bundle = bundle;
    m_elementName = elementName;
    m_propertySupport = new BasicPropertySupport(this);
  }

  @Override
  protected void createContent(Composite parent) {
    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("Name"));
    m_typeNameField.setReadOnlySuffix(getTypeSuffix());
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
    m_packageField.setEnabled(isAllowModifyPackage());
    m_packageField.setText(StringUtility.nvl(getRecommendedPackageFragment(), ""));

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
    m_packageBrowseButton.setEnabled(isAllowModifyPackage());

    m_interfaceTypeField = getFieldToolkit().createStyledTextField(parent, Texts.get("Interface"));
    if (TypeUtility.exists(getInterfaceType())) {
      m_interfaceTypeField.setText(getInterfaceType().getElementName());
    }
    m_interfaceTypeField.setEditable(false);
    m_interfaceTypeField.setEnabled(isAllowModifyInterfaceType());
    m_tooltipInterfaceType = new JavadocTooltip(m_interfaceTypeField.getTextComponent());
    updateJavaDoc(m_tooltipInterfaceType, getInterfaceType());

    m_interfaceTypeBrowseButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_interfaceTypeBrowseButton.setText(Texts.get("Browse"));
    m_interfaceTypeBrowseButton.setEnabled(isAllowModifyInterfaceType());
    m_interfaceTypeBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          IJavaSearchScope searchScope = getInterfaceTypeSearchScopeFactory().create();

          SelectionDialog dialog = JavaUI.createTypeDialog(ScoutSdkUi.getShell(), null, searchScope, IJavaElementSearchConstants.CONSIDER_CLASSES | IJavaElementSearchConstants.CONSIDER_INTERFACES, false, "*.*");
          dialog.setTitle(Texts.get("Interface"));
          dialog.setMessage(Texts.get("ChooseXY", Texts.get("Interface")));
          dialog.setBlockOnOpen(true);
          if (dialog.open() == Window.OK) {
            if (dialog.getResult() != null) {
              IType type = (IType) dialog.getResult()[0];
              setInterfaceType(type);
            }
          }
        }
        catch (JavaModelException exception) {
          JaxWsSdk.logError(exception);
        }
      }
    });

    m_superTypeField = getFieldToolkit().createStyledTextField(parent, Texts.get("SuperType"));
    if (TypeUtility.exists(getSuperType())) {
      m_superTypeField.setText(getSuperType().getElementName());
    }
    m_superTypeField.setEditable(false);
    m_superTypeField.setEnabled(isAllowModifySuperType());
    m_tooltipSuperType = new JavadocTooltip(m_superTypeField.getTextComponent());
    updateJavaDoc(m_tooltipSuperType, getSuperType());

    m_superTypeBrowseButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_superTypeBrowseButton.setText(Texts.get("Browse"));
    m_superTypeBrowseButton.setEnabled(isAllowModifySuperType());
    m_superTypeBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          IJavaSearchScope searchScope = getSuperTypeSearchScopeFactory().create();

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
    formData.top = new FormAttachment(m_packageBrowseButton, 20, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_interfaceTypeField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_interfaceTypeField, -2, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_interfaceTypeBrowseButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_interfaceTypeField, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_superTypeField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_superTypeField, -2, SWT.TOP);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_superTypeBrowseButton.setLayoutData(formData);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    validateType(multiStatus);
  }

  protected void validateType(MultiStatus multiStatus) {
    // package
    if (StringUtility.isNullOrEmpty(getPackageName())) {
      multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("UsageOfDefaultPackageDiscouraged")));
    }
    else {
      multiStatus.add(JavaConventionsUtil.validatePackageName(getPackageName(), m_bundle.getJavaProject()));

      if (getRecommendedPackageFragment() != null && !getPackageName().equals(getRecommendedPackageFragment())) {
        multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("ByConventionXShouldByY", Texts.get("Package"), getRecommendedPackageFragment())));
      }
    }

    // name
    if (StringUtility.isNullOrEmpty(getTypeName()) || (getTypeSuffix() != null && getTypeName().equals(getTypeSuffix()))) {
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

  public void setInterfaceType(IType interfaceType) {
    try {
      setStateChanging(true);
      setInterfaceTypeInternal(interfaceType);
      if (isControlCreated()) {
        if (interfaceType != null) {
          m_interfaceTypeField.setText(interfaceType.getElementName());
        }
        else {
          m_interfaceTypeField.setText("");
        }
        updateJavaDoc(m_tooltipInterfaceType, getInterfaceType());
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setInterfaceTypeInternal(IType interfaceType) {
    m_propertySupport.setProperty(PROP_INTERFACE_TYPE, interfaceType);
  }

  public IType getInterfaceType() {
    return (IType) m_propertySupport.getProperty(PROP_INTERFACE_TYPE);
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
    m_propertySupport.setPropertyString(PROP_PACKAGE_NAME, packageName);
  }

  public String getPackageName() {
    return m_propertySupport.getPropertyString(PROP_PACKAGE_NAME);
  }

  private IPackageFragment[] openBrowsePackagesDialog() {
    IPackageFragment[] packageFragments = null;
    IRunnableContext context = new BusyIndicatorRunnableContext();
    SelectionDialog dialog = JavaUI.createPackageDialog(ScoutSdkUi.getShell(), context, m_bundle.getSearchScope(), false, true, null);
    dialog.setTitle(Texts.get("Package"));
    dialog.setMessage(Texts.get("ChoosePackageForX", getElementName()));

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

  public String getElementName() {
    return m_elementName;
  }

  public void setElementName(String elementName) {
    m_elementName = elementName;
  }

  public String getTypeSuffix() {
    return m_typeSuffix;
  }

  public void setTypeSuffix(String typeSuffix) {
    m_typeSuffix = typeSuffix;
  }

  public String getRecommendedPackageFragment() {
    return m_recommendedPackageFragment;
  }

  public void setRecommendedPackageFragment(String recommendedPackageFragment) {
    m_recommendedPackageFragment = recommendedPackageFragment;
  }

  public boolean isAllowModifySuperType() {
    return m_allowModifySuperType;
  }

  public void setAllowModifySuperType(boolean allowModifySuperType) {
    m_allowModifySuperType = allowModifySuperType;
  }

  public boolean isAllowModifyInterfaceType() {
    return m_allowModifyInterfaceType;
  }

  public void setAllowModifyInterfaceType(boolean allowModifyInterfaceType) {
    m_allowModifyInterfaceType = allowModifyInterfaceType;
  }

  public boolean isAllowModifyPackage() {
    return m_allowModifyPackage;
  }

  public void setAllowModifyPackage(boolean allowModifyPackage) {
    m_allowModifyPackage = allowModifyPackage;
  }

  public JavaSearchScopeFactory getSuperTypeSearchScopeFactory() {
    return m_superTypeSearchScopeFactory;
  }

  public void setSuperTypeSearchScopeFactory(JavaSearchScopeFactory superTypeSearchScopeFactory) {
    m_superTypeSearchScopeFactory = superTypeSearchScopeFactory;
  }

  public JavaSearchScopeFactory getInterfaceTypeSearchScopeFactory() {
    return m_interfaceTypeSearchScopeFactory;
  }

  public void setInterfaceTypeSearchScopeFactory(JavaSearchScopeFactory interfaceTypeSearchScopeFactory) {
    m_interfaceTypeSearchScopeFactory = interfaceTypeSearchScopeFactory;
  }

  public static class JavaSearchScopeFactory {
    public IJavaSearchScope create() {
      return SearchEngine.createWorkspaceScope();
    }
  }
}
