/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.wizard.page;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.page.PageNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.validation.JavaElementValidator;
import org.eclipse.scout.sdk.workspace.DefaultTargetPackage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>PageNewWizardPage2</h3> ...
 */
public class PageNewAttributesWizardPage extends AbstractWorkspaceWizardPage {

  private IType iPage = TypeUtility.getType(RuntimeClasses.IPage);
  private IType iPageWithNodes = TypeUtility.getType(RuntimeClasses.IPageWithNodes);
  private IType iPageWithTable = TypeUtility.getType(RuntimeClasses.IPageWithTable);
  private IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);

  private INlsEntry m_nlsName;
  private String m_typeName;
  private IType m_superType;
  private IType m_holderType;
  private String m_nameSuffix;
  private String m_packageName;

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_holderTypeField;
  private EntityTextField m_entityField;

  // process members
  private PageNewOperation m_operation;

  private boolean m_hoderTypeEnabled = true;

  private final IScoutBundle m_clientBundle;

  public PageNewAttributesWizardPage(IScoutBundle clientBundle) {
    super(PageNewAttributesWizardPage.class.getName());
    m_clientBundle = clientBundle;
    setTitle(Texts.get("NewPage"));
    setDescription(Texts.get("CreateANewPage"));
    setTargetPackage(DefaultTargetPackage.get(clientBundle, IScoutBundle.CLIENT_PAGES));
    m_nameSuffix = "";
    setSuperType(RuntimeClasses.getSuperType(RuntimeClasses.IPageWithNodes, m_clientBundle.getJavaProject()));
    setOperation(new PageNewOperation(true));
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;
    Group group = new Group(parent, SWT.SHADOW_ETCHED_OUT);
    group.setText(Texts.get("Page"));

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(group, getClientBundle().findBestMatchNlsProject(), Texts.get("Name"), labelColWidthPercent);
    m_nlsNameField.acceptProposal(m_nlsName);
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = getNlsName();
          m_nlsName = (INlsEntry) event.proposal;
          if (m_nlsName != null) {
            if (oldEntry == null || oldEntry.getKey().equals(m_typeNameField.getModifiableText()) || StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(m_nlsName.getKey());
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(group, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(m_nameSuffix);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_entityField = getFieldToolkit().createEntityTextField(group, Texts.get("EntityTextField"), m_clientBundle, labelColWidthPercent);
    m_entityField.setText(getTargetPackage());
    m_entityField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTargetPackageInternal((String) m_entityField.getText());
        pingStateChanging();
      }
    });

    Control parentPageControl = createParentPageGroup(parent);
    // layout
    parent.setLayout(new GridLayout(1, true));
    group.setLayout(new GridLayout(1, true));
    group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_entityField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    parentPageControl.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  protected Control createParentPageGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_OUT);
    group.setText(Texts.get("AddTo"));

    m_holderTypeField = getFieldToolkit().createJavaElementProposalField(group, Texts.get("PageOutline"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        ITypeFilter filter = TypeFilters.getMultiTypeFilter(
            ScoutTypeFilters.getInScoutBundles(getClientBundle()),
            TypeFilters.getClassFilter());

        IType[] pages = TypeUtility.getPrimaryTypeHierarchy(iPage).getAllSubtypes(iPageWithNodes, filter);
        IType[] outlines = TypeUtility.getPrimaryTypeHierarchy(iOutline).getAllSubtypes(iOutline, filter);
        IType[] propTypes = new IType[pages.length + outlines.length];
        System.arraycopy(pages, 0, propTypes, 0, pages.length);
        System.arraycopy(outlines, 0, propTypes, pages.length, outlines.length);
        Arrays.sort(propTypes, TypeComparators.getTypeNameComparator());
        return new Object[][]{propTypes};
      }
    });
    m_holderTypeField.acceptProposal(getHolderType());
    m_holderTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_holderType = (IType) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    m_holderTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return group;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // write back members
    getOperation().setClientBundle(getClientBundle());
    getOperation().setNlsEntry(getNlsName());
    getOperation().setTypeName(getTypeName());
    getOperation().setPackageName(getClientBundle().getPackageName(getTargetPackage()));
    IType superType = getSuperType();
    if (superType != null) {
      getOperation().setSuperTypeSignature(SignatureCache.createTypeSignature(superType.getFullyQualifiedName()));
    }
    getOperation().setHolderType(getHolderType());

    getOperation().run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusClientBundle());
      if (!multiStatus.matches(IStatus.ERROR)) {
        multiStatus.add(getStatusNameField());
        multiStatus.add(getStatusTargetPackge());
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusTargetPackge() {
    return JavaElementValidator.validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusClientBundle() throws JavaModelException {
    if (getClientBundle() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ClientBundleMissing"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(m_nameSuffix)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_className"));
    }
    // check not allowed names
    if (TypeUtility.existsType(getClientBundle().getPackageName(getTargetPackage()) + "." + getTypeName())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    if (Regex.REGEX_WELLFORMD_JAVAFIELD.matcher(getTypeName()).matches()) {
      return Status.OK_STATUS;
    }
    else if (Regex.REGEX_JAVAFIELD.matcher(getTypeName()).matches()) {
      return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_invalidFieldX", getTypeName()));
    }
  }

  public void setOperation(PageNewOperation operation) {
    m_operation = operation;
  }

  public PageNewOperation getOperation() {
    return m_operation;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public INlsEntry getNlsName() {
    return m_nlsName;
  }

  public void setNlsName(INlsEntry nlsName) {
    try {
      setStateChanging(true);
      m_nlsName = nlsName;
      if (isControlCreated()) {
        m_nlsNameField.acceptProposal(nlsName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    try {
      setStateChanging(true);
      m_typeName = typeName;
      if (isControlCreated()) {
        m_typeNameField.setText(typeName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      m_superType = superType;
      if (TypeUtility.exists(superType)) {
        try {
          ITypeHierarchy superTypeHierarchy = superType.newSupertypeHierarchy(null);
          if (superTypeHierarchy.contains(iPageWithNodes)) {
            m_nameSuffix = SdkProperties.SUFFIX_OUTLINE_NODE_PAGE;
          }
          else if (superTypeHierarchy.contains(iPageWithTable)) {
            m_nameSuffix = SdkProperties.SUFFIX_OUTLINE_TABLE_PAGE;
          }
          else {
            m_nameSuffix = SdkProperties.SUFFIX_OUTLINE_PAGE;
          }
        }
        catch (JavaModelException e) {
          ScoutSdkUi.logError("could not create superTypeHierarchy of '" + getSuperType().getFullyQualifiedName() + "'.");
        }
      }
      if (isControlCreated()) {
        m_typeNameField.setReadOnlySuffix(m_nameSuffix);
      }

    }
    finally {
      setStateChanging(false);
    }
  }

  public void setHolderType(IType holderPage) {
    try {
      setStateChanging(true);
      m_holderType = holderPage;
      if (isControlCreated()) {
        m_holderTypeField.acceptProposal(holderPage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public IType getHolderType() {
    return m_holderType;
  }

  public void setHoderTypeEnabled(boolean hoderTypeEnabled) {
    if (isControlCreated()) {
      throw new IllegalStateException("control already created.");
    }
    m_hoderTypeEnabled = hoderTypeEnabled;
  }

  public boolean isHoderTypeEnabled() {
    return m_hoderTypeEnabled;
  }

  public String getTargetPackage() {
    return m_packageName;
  }

  public void setTargetPackage(String targetPackage) {
    try {
      setStateChanging(true);
      setTargetPackageInternal(targetPackage);
      if (isControlCreated()) {
        m_entityField.setText(targetPackage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTargetPackageInternal(String targetPackage) {
    m_packageName = targetPackage;
  }
}
