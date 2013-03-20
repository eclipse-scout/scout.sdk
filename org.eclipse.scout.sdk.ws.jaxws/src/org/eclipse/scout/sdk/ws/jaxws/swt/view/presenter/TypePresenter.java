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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.OpenNewClassWizardAction;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.fields.tooltip.JavadocTooltip;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.util.IScoutSeverityListener;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.SelectionDialog;

@SuppressWarnings("restriction")
public class TypePresenter extends AbstractPropertyPresenter<String> {

  protected StyledText m_textField;
  protected Button m_button;

  private IType m_superType;
  private String[] m_interfaceSignatures;
  private boolean m_allowChangeOfInterfaceType;
  private ISearchJavaSearchScopeFactory m_searchScopeFactory;
  private String m_defaultPackageNameNewType;
  private ModifyListener m_modifyListener;
  private boolean m_allowChangeOfSourceFolder;
  private JavadocTooltip m_javaDocTooltip;

  private P_ScoutSeverityListener m_severityListener;

  public TypePresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    this(parent, toolkit, AbstractPropertyPresenter.DEFAULT_LABEL_WIDTH, true);
  }

  public TypePresenter(Composite parent, PropertyViewFormToolkit toolkit, int labelWidth, boolean initialize) {
    super(parent, toolkit, labelWidth, false);
    setLabel(Texts.get("class"));
    setUseLinkAsLabel(true);
    m_modifyListener = new P_ModifyListener();
    m_severityListener = new P_ScoutSeverityListener();
    ScoutSeverityManager.getInstance().addQualityManagerListener(m_severityListener);
    if (initialize) {
      callInitializer();
    }
  }

  @Override
  protected Control createContent(Composite parent) {
    Composite composite = getToolkit().createComposite(parent, SWT.NONE);
    m_textField = new StyledText(composite, SWT.SINGLE | SWT.BORDER);
    m_textField.setEditable(true);
    m_textField.addModifyListener(m_modifyListener);
    m_textField.addFocusListener(new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        IType type = validateType(m_textField.getText());
        if (type != null) {
          setValueFromUI(type.getFullyQualifiedName());
        }
      }
    });
    m_javaDocTooltip = new JavadocTooltip(m_textField);

    m_button = new Button(composite, SWT.PUSH | SWT.FLAT);
    m_button.setText(Texts.get("Browse"));
    m_button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IType newType = openBrowseTypesDialog();
        if (TypeUtility.exists(newType)) {
          setInputInternal(newType.getFullyQualifiedName());
          setValueFromUI(newType.getFullyQualifiedName(), true);
        }
      }
    });
    m_button.setEnabled(m_searchScopeFactory != null);

    // layout
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    composite.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    m_textField.setLayoutData(gd);

    gd = new GridData();
    m_button.setLayoutData(gd);

    return composite;
  }

  @Override
  public void dispose() {
    if (m_severityListener != null) {
      ScoutSeverityManager.getInstance().removeQualityManagerListener(m_severityListener);
    }
  }

  @Override
  protected void execLinkAction() throws CoreException {
    String fqn = m_textField.getText();
    if (TypeUtility.existsType(fqn)) {
      try {
        JavaUI.openInEditor(TypeUtility.getType(fqn));
      }
      catch (Exception e) {
        JaxWsSdk.logWarning(e);
      }
    }
    else {
      IType newType = openNewTypeDialog();
      if (TypeUtility.exists(newType)) {
        setInputInternal(newType.getFullyQualifiedName());
        setValueFromUI(newType.getFullyQualifiedName());
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

    String fqn = StringUtility.nvl(input, "");
    if (TypeUtility.existsType(fqn)) {
      setTooltip(Signature.getSimpleName(fqn));
      m_javaDocTooltip.setMember(TypeUtility.getType(fqn));
    }
    else {
      setTooltip(null);
      m_javaDocTooltip.setMember(null);
    }

    validateType(fqn);
  }

  public ISearchJavaSearchScopeFactory getSearchScopeFactory() {
    return m_searchScopeFactory;
  }

  public void setSearchScopeFactory(ISearchJavaSearchScopeFactory searchScopeFactory) {
    m_searchScopeFactory = searchScopeFactory;

    if (isControlCreated()) {
      m_button.setEnabled(m_searchScopeFactory != null);
    }
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
  }

  /**
   * To set the super interface types. Fore interface signatures, use
   * {@link TypePresenter#setInterfaceSignatures(String[])}
   * 
   * @param interfaceTypes
   */
  public void setInterfaceTypes(IType[] interfaceTypes) {
    List<String> signatures = new ArrayList<String>();
    for (IType type : interfaceTypes) {
      signatures.add(SignatureCache.createTypeSignature(type.getFullyQualifiedName()));
    }
    setInterfaceSignatures(signatures.toArray(new String[signatures.size()]));
  }

  public void setInterfaceSignatures(String[] interfaceSignatures) {
    m_interfaceSignatures = interfaceSignatures;
  }

  public void setAllowChangeOfInterfaceType(boolean allowChangeOfInterfaceType) {
    m_allowChangeOfInterfaceType = allowChangeOfInterfaceType;
  }

  public String getDefaultPackageNameNewType() {
    return m_defaultPackageNameNewType;
  }

  public void setDefaultPackageNameNewType(String defaultPackageNameNewType) {
    m_defaultPackageNameNewType = defaultPackageNameNewType;
  }

  protected IType openNewTypeDialog() throws JavaModelException {
    NewClassWizardPage page = new NewClassWizardPage();
    page.setDescription(Texts.get("CreateNewType"));
    page.setEnclosingTypeSelection(false, false);
    page.setMethodStubSelection(false, true, true, true);
    String qualifier = Signature.getQualifier(m_textField.getText());
    IPackageFragment packageFragment = null;
    if (qualifier != null && qualifier.length() > 0) {
      packageFragment = m_bundle.getPackageFragment(qualifier);
    }
    else if (m_defaultPackageNameNewType != null) {
      packageFragment = m_bundle.getPackageFragment(m_defaultPackageNameNewType);
    }

    if (packageFragment != null) {
      page.setPackageFragmentRoot((IPackageFragmentRoot) packageFragment.getParent(), isAllowChangeOfSourceFolder());
      page.setPackageFragment(packageFragment, true);
    }
    else {
      String rootPackageName = m_bundle.getSymbolicName();
      page.setPackageFragmentRoot((IPackageFragmentRoot) m_bundle.getPackageFragment(rootPackageName).getParent(), true);
    }
    if (m_superType != null) {
      page.setSuperClass(m_superType.getFullyQualifiedName(), false);
    }
    if (m_interfaceSignatures != null && m_interfaceSignatures.length > 0) {
      List<String> interfaceTypeNames = new LinkedList<String>();
      for (String signature : m_interfaceSignatures) {
        interfaceTypeNames.add(stripSignatureToFQN(signature, false));
      }
      page.setSuperInterfaces(interfaceTypeNames, m_allowChangeOfInterfaceType);
    }
    page.setTypeName(Signature.getSimpleName(m_textField.getText()), true);
    OpenNewClassWizardAction action = new OpenNewClassWizardAction();
    action.setConfiguredWizardPage(page);
    action.run();

    IType type = page.getCreatedType();
    if (TypeUtility.exists(type)) {
      return type;
    }
    return null;
  }

  private IType openBrowseTypesDialog() {
    String fqn = m_textField.getText();
    IType type = null;
    if (TypeUtility.existsType(fqn)) {
      type = TypeUtility.getType(fqn);
    }

    try {
      SelectionDialog dialog = JavaUI.createTypeDialog(ScoutSdkUi.getShell(), null, m_searchScopeFactory.create(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false, "*.*");
      dialog.setTitle(Texts.get("BrowseForType"));
      if (TypeUtility.exists(type)) {
        dialog.setInitialSelections(new IType[]{type});
      }
      dialog.setBlockOnOpen(true);
      if (dialog.open() == Window.OK) {
        if (dialog.getResult() != null) {
          type = (IType) dialog.getResult()[0];
          if (TypeUtility.exists(type)) {
            return type;
          }
        }
      }
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError(e);
    }
    return null;
  }

  public boolean isAllowChangeOfSourceFolder() {
    return m_allowChangeOfSourceFolder;
  }

  public boolean isAllowChangeOfInterfaceType() {
    return m_allowChangeOfInterfaceType;
  }

  public void setAllowChangeOfSourceFolder(boolean allowChangeOfSourceFolder) {
    m_allowChangeOfSourceFolder = allowChangeOfSourceFolder;
  }

  private IType validateType(String fullyQualifiedName) {
    if (!TypeUtility.existsType(fullyQualifiedName)) {
      setInfo(IMarker.SEVERITY_ERROR, Texts.get("TypeDoesNotExistClickOnXToCreate", Texts.get("class")));
      return null;
    }

    IType type = TypeUtility.getType(fullyQualifiedName);

    // check validity of type
    if (m_interfaceSignatures != null) {
      for (String signature : m_interfaceSignatures) {
        IType interfaceType = TypeUtility.getType(stripSignatureToFQN(signature, true));
        if (!JaxWsSdkUtility.isJdtSubType(interfaceType.getFullyQualifiedName(), type)) {
          setInfo(IMarker.SEVERITY_ERROR, Texts.get("XMustBeSubtypeOfY", type.getElementName(), interfaceType.getFullyQualifiedName()));
          return null;
        }
      }
    }
    if (m_superType != null && !JaxWsSdkUtility.isJdtSubType(m_superType.getFullyQualifiedName(), type)) {
      setInfo(IMarker.SEVERITY_ERROR, Texts.get("XMustBeSubtypeOfY", type.getElementName(), m_superType.getFullyQualifiedName()));
      return null;
    }

    clearInfo();
    return type;
  }

  @Override
  protected void updateSeverity(List<SeverityEntry> statusList) throws CoreException {
    if (StringUtility.hasText(getValue()) && (TypeUtility.existsType(getValue()))) {
      int quality = ScoutSeverityManager.getInstance().getSeverityOf(TypeUtility.getType(getValue()));
      if (quality > IMarker.SEVERITY_INFO) {
        statusList.add(new SeverityEntry(quality, "Unresolved compilation errors"));
      }
    }
  }

  private class P_ModifyListener implements ModifyListener {

    @Override
    public void modifyText(ModifyEvent e) {
      validateType(m_textField.getText());
    }
  }

  private String stripSignatureToFQN(String signature, boolean eraseGenericTypes) {
    if (signature == null) {
      return null;
    }

    String typeSignature = SignatureUtil.stripSignatureToFQN(signature);
    if (!eraseGenericTypes) {
      // support for generic type parameters
      String typeArguments = null;
      for (String typeArgument : Signature.getTypeArguments(signature)) {
        typeArguments = StringUtility.join(",", typeArguments, SignatureUtil.stripSignatureToFQN(typeArgument));
      }
      if (typeArguments != null) {
        typeSignature = typeSignature + Signature.C_GENERIC_START + typeArguments + Signature.C_GENERIC_END;
      }
    }

    return typeSignature;
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_button.setEnabled(enabled);
    m_textField.setEnabled(enabled);

    if (enabled) {
      m_textField.setBackground(null);
    }
    else {
      m_textField.setBackground(JaxWsSdkUtility.getColorLightGray());
    }

    super.setEnabled(enabled);
  }

  public static interface ISearchJavaSearchScopeFactory {

    /**
     * For default search scope, use {@link SearchEngine#createWorkspaceScope()}
     * 
     * @return
     */
    public IJavaSearchScope create();
  }

  private class P_ScoutSeverityListener implements IScoutSeverityListener {

    @Override
    public void severityChanged(final IResource resource) {
      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          if (isDisposed()) {
            return;
          }

          String value = getValue();
          if (!StringUtility.hasText(value)) {
            return;
          }
          String simpleName = Signature.getSimpleName(value);
          if (TypeUtility.existsType(value) &&
              resource.getType() == IResource.FILE &&
              resource.getFileExtension() != null &&
              resource.getFileExtension().equalsIgnoreCase("java") &&
              (resource.getName().endsWith(simpleName + ".java") ||
              resource.getName().endsWith(simpleName + ".class"))) {
            IType type = TypeUtility.getType(getValue());
            ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) resource);

            if (cu == null) {
              return;
            }

            try {
              for (IType typeChanged : cu.getTypes()) {
                if (TypeUtility.exists(typeChanged)) {
                  typeChanged = TypeUtility.getToplevelType(typeChanged);

                  if ((CompareUtility.equals(typeChanged, type))) {
                    updateInfo();
                    return;
                  }
                }
              }
            }
            catch (Exception e) {
              JaxWsSdk.logError(e);
            }
          }
        }
      });
    }
  }
}
