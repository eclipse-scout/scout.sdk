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
package org.eclipse.scout.sdk.ui.wizard.form;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>SearchFormNewWizardPage</h3> ...
 */
public class SearchFormNewWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_NLS_NAME = "nlsName";
  public static final String PROP_TYPE_NAME = "typeName";
  public static final String PROP_SUPER_TYPE = "superType";
  public static final String PROP_TABLE_PAGE = "tablePage";
  public static final String PROP_TARGET_PACKAGE = "targetPackage";

  private final IType iSearchForm = TypeUtility.getType(IRuntimeClasses.ISearchForm);

  // ui fields
  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_tablePageField;
  private EntityTextField m_entityField;

  // process members
  private final IScoutBundle m_clientBundle;
  private IType m_abstractSearchForm;

  public SearchFormNewWizardPage(IScoutBundle clientBundle) {
    super(SearchFormNewWizardPage.class.getName());
    m_clientBundle = clientBundle;

    setTitle(Texts.get("SearchForm2"));
    setDescription(Texts.get("CreateANewSearchForm"));

    if (clientBundle != null) {
      m_abstractSearchForm = RuntimeClasses.getSuperType(IRuntimeClasses.ISearchForm, ScoutUtility.getJavaProject(clientBundle));
      setSuperTypeInternal(m_abstractSearchForm);
      setTargetPackage(DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_SEARCHFORMS));
    }
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;
    boolean isEnabled = getClientBundle() != null;

    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("SearchForm"));

    INlsProject nls = null;
    if (getClientBundle() != null) {
      nls = getClientBundle().getNlsProject();
    }
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(group, nls, Texts.get("Name"), labelColWidthPercent);
    m_nlsNameField.acceptProposal(getNlsName());
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = getNlsName();
          INlsEntry newName = (INlsEntry) event.proposal;
          setNlsNameInternal(newName);
          if (newName != null) {
            if (oldEntry == null || StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText()) || oldEntry.getKey().equals(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(NamingUtility.toJavaCamelCase(newName.getKey(), false));
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });
    m_nlsNameField.setEnabled(isEnabled);

    m_typeNameField = getFieldToolkit().createStyledTextField(group, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_SEARCH_FORM);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });
    m_typeNameField.setEnabled(isEnabled);

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(group, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iSearchForm, ScoutUtility.getJavaProject(getClientBundle()), m_abstractSearchForm), labelColWidthPercent);
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });
    m_superTypeField.setEnabled(isEnabled);

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(group, Texts.get("EntityTextField"), getClientBundle(), labelColWidthPercent);
      m_entityField.setText(getTargetPackage(null));
      m_entityField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setTargetPackageInternal(m_entityField.getText());
          pingStateChanging();
        }
      });
      m_entityField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
      m_entityField.setEnabled(isEnabled);
    }

    Control tablePageGroup = createTablePageGroup(parent, labelColWidthPercent);

    // layout
    parent.setLayout(new GridLayout(1, true));
    group.setLayout(new GridLayout(1, true));
    group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    tablePageGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    m_nlsNameField.setFocus();
  }

  private Control createTablePageGroup(Composite parent, int labelColWidthPercent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("AutoCreate"));
    Label label = new Label(group, SWT.NONE);
    label.setText(Texts.get("SelectTablePageForSearchForm"));
    m_tablePageField = getFieldToolkit().createJavaElementProposalField(group, Texts.get("TablePage"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);
        IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
        IType[] list = TypeUtility.getPrimaryTypeHierarchy(iPage).getAllSubtypes(iPageWithTable, TypeFilters.getTypesOnClasspath(ScoutUtility.getJavaProject(getClientBundle())), TypeComparators.getTypeNameComparator());
        return new Object[][]{list};
      }
    }, labelColWidthPercent);
    m_tablePageField.acceptProposal(getTablePageType());
    m_tablePageField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setTablePageTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });
    m_tablePageField.setEnabled(getClientBundle() != null);

    // layout
    group.setLayout(new GridLayout(1, true));

    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_tablePageField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return group;
  }

  @Override
  public void setFocus() {
    if (m_tablePageField.getSelectedProposal() == null) {
      m_tablePageField.setFocus();
    }
    else {
      m_nlsNameField.setFocus();
    }
  }

  @Override
  public SearchFormNewWizard getWizard() {
    return (SearchFormNewWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusWorkspace());
    multiStatus.add(getStatusNameField());
    multiStatus.add(getStatusSuperType());
    multiStatus.add(getStatusTargetPackge());
  }

  protected IStatus getStatusWorkspace() {
    if (getClientBundle() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoNewXWithoutScoutBundle", Texts.get("SearchForm")));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusTargetPackge() {
    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      return ScoutUtility.validatePackageName(getTargetPackage(null));
    }
    else {
      return Status.OK_STATUS;
    }
  }

  protected IStatus getStatusNameField() {
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(getTypeName(), SdkProperties.SUFFIX_SEARCH_FORM);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    IStatus existingStatus = ScoutUtility.validateTypeNotExisting(getClientBundle(), getTargetPackage(IDefaultTargetPackage.CLIENT_SEARCHFORMS), getTypeName());
    if (!existingStatus.isOK()) {
      return existingStatus;
    }
    return javaFieldNameStatus;
  }

  protected IStatus getStatusSuperType() {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public INlsEntry getNlsName() {
    return (INlsEntry) getProperty(PROP_NLS_NAME);
  }

  public void setNlsName(INlsEntry proposal) {
    try {
      setStateChanging(true);
      setNlsNameInternal(proposal);
      if (isControlCreated()) {
        m_nlsNameField.acceptProposal(proposal);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setNlsNameInternal(INlsEntry proposal) {
    setProperty(PROP_NLS_NAME, proposal);
  }

  public String getTypeName() {
    return getPropertyString(PROP_TYPE_NAME);
  }

  public void setTypeName(String typeName) {
    try {
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
    setPropertyString(PROP_TYPE_NAME, typeName);
  }

  public IType getSuperType() {
    return (IType) getProperty(PROP_SUPER_TYPE);
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      setSuperTypeInternal(superType);
      if (isControlCreated()) {
        m_superTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setSuperTypeInternal(IType superType) {
    setProperty(PROP_SUPER_TYPE, superType);
  }

  public IType getTablePageType() {
    return (IType) getProperty(PROP_TABLE_PAGE);
  }

  public void setTablePageType(IType superType) {
    try {
      setStateChanging(true);
      setTablePageTypeInternal(superType);
      if (isControlCreated()) {
        m_tablePageField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTablePageTypeInternal(IType superType) {
    setProperty(PROP_TABLE_PAGE, superType);
  }

  public String getTargetPackage(String packageId) {
    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      return (String) getProperty(PROP_TARGET_PACKAGE);
    }
    else {
      return DefaultTargetPackage.get(null, packageId);
    }
  }

  public void setTargetPackage(String targetPackage) {
    try {
      setStateChanging(true);
      setTargetPackageInternal(targetPackage);
      if (isControlCreated() && m_entityField != null) {
        m_entityField.setText(targetPackage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTargetPackageInternal(String targetPackage) {
    setProperty(PROP_TARGET_PACKAGE, targetPackage);
  }
}
