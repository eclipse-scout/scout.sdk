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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.FormStackNewOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
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

  /** {@link NlsProposal} **/
  public static final String PROP_NLS_NAME = "nlsName";
  /** {@link String} **/
  public static final String PROP_TYPE_NAME = "typeName";
  /** {@link ITypeProposal} **/
  public static final String PROP_SUPER_TYPE = "superType";
  /** {@link ITypeProposal} **/
  public static final String PROP_TABLE_PAGE = "tablePage";

  final IType iForm = ScoutSdk.getType(RuntimeClasses.IForm);
  final IType iSearchForm = ScoutSdk.getType(RuntimeClasses.ISearchForm);
  final IType abstractSearchForm = ScoutSdk.getType(RuntimeClasses.AbstractSearchForm);
  final IType iPage = ScoutSdk.getType(RuntimeClasses.IPage);
  final IType iPageWithTable = ScoutSdk.getType(RuntimeClasses.IPageWithTable);
  // ui fields
  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_tablePageField;

  // process members
  private IScoutBundle m_clientBundle;

  public SearchFormNewWizardPage(IScoutBundle clientBundle) {
    super(SearchFormNewWizardPage.class.getName());
    m_clientBundle = clientBundle;
    setTitle("New Search Form");
    setDefaultMessage("Create a new search form.");
    setSuperTypeInternal(ScoutProposalUtility.getScoutTypeProposalsFor(abstractSearchForm)[0]);
  }

  @Override
  protected void createContent(Composite parent) {
    Control tablePageGroup = createTablePageGroup(parent);
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, null, "Name");
    m_nlsNameField.acceptProposal(getNlsName());
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = null;
          if (getNlsName() != null) {
            oldEntry = getNlsName().getNlsEntry();
          }
          NlsProposal newName = (NlsProposal) event.proposal;
          setNlsNameInternal(newName);
          if (newName != null) {
            if (oldEntry == null) {
              if (StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText()) || oldEntry.getKey().equals(m_typeNameField.getModifiableText())) {
                m_typeNameField.setText(newName.getNlsEntry().getKey());
              }
            }

          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, "Type Name");
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_SEARCH_FORM);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createProposalField(parent, null, "Super Type");
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((ITypeProposal) event.proposal);
        pingStateChanging();
      }
    });

    updateClientBundle();
    // layout
    parent.setLayout(new GridLayout(1, true));

    tablePageGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private Control createTablePageGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText("Auto create");
    Label label = new Label(group, SWT.NONE);
    label.setText("Choose a table page to create a 'Search Form' for.");
    m_tablePageField = getFieldToolkit().createProposalField(group, null, "Table Page");
    m_tablePageField.acceptProposal(getTablePageType());
    m_tablePageField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        setTablePageTypeInternal((ITypeProposal) event.proposal);
        pingStateChanging();
      }
    });

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

  private void updateClientBundle() {
    DefaultProposalProvider superTypeProvider = new DefaultProposalProvider();
    DefaultProposalProvider tablePageProvider = new DefaultProposalProvider();
    INlsProject nlsProject = null;
    if (getClientBundle() != null) {
      nlsProject = getClientBundle().findBestMatchNlsProject();
      ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(ScoutSdk.getType(RuntimeClasses.AbstractSearchForm));
      ICachedTypeHierarchy searchFormHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iSearchForm);
      IType[] abstractSearchForms = searchFormHierarchy.getAllSubtypes(iSearchForm, TypeFilters.getAbstractOnClasspath(getClientBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
      ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(abstractSearchForms);
      superTypeProvider = new DefaultProposalProvider(shotList, proposals);
      IType[] pageWithTables = ScoutSdk.getPrimaryTypeHierarchy(iPage).getAllSubtypes(iPageWithTable, TypeFilters.getTypesOnClasspath(getClientBundle().getJavaProject()), TypeComparators.getTypeNameComparator());

      ITypeProposal[] tpProposals = ScoutProposalUtility.getScoutTypeProposalsFor(pageWithTables);
      tablePageProvider = new DefaultProposalProvider(tpProposals);
    }

    if (nlsProject != null) {
      m_nlsNameField.setNlsProject(nlsProject);
    }
    else {
      m_nlsNameField.setEnabled(false);
      m_nlsNameField.acceptProposal(null);
    }
    ITypeProposal superTypeProp = (ITypeProposal) m_superTypeField.getSelectedProposal();
    m_superTypeField.setContentProposalProvider(superTypeProvider);
    if (superTypeProp != null) {
      if (getClientBundle().isOnClasspath(superTypeProp.getType())) {
        m_superTypeField.acceptProposal(superTypeProp);
      }
    }

    ITypeProposal tablePageTypeProp = (ITypeProposal) m_tablePageField.getSelectedProposal();
    m_tablePageField.setContentProposalProvider(tablePageProvider);
    if (tablePageTypeProp != null) {
      if (getClientBundle().isOnClasspath(tablePageTypeProp.getType())) {
        m_tablePageField.acceptProposal(tablePageTypeProp);
      }
    }
  }

  void fillOperation(FormStackNewOperation operation) {
    // operation.setCreateIdProperty(isCreateFormId());
    // operation.setFormIdName(getFormId());
    operation.setFormName(getTypeName());
    if (getNlsName() != null) {
      operation.setNlsEntry(getNlsName().getNlsEntry());
    }
    ITypeProposal superTypeProp = getSuperType();
    if (superTypeProp != null) {
      operation.setFormSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true));
    }
  }

  @Override
  public SearchFormNewWizard getWizard() {
    return (SearchFormNewWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_SEARCH_FORM)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (ScoutSdk.getType(getClientBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_SEARCHFORMS) + "." + getTypeName()) != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }

    if (getTypeName().matches(Regex.REGEX_WELLFORMD_JAVAFIELD)) {
      return Status.OK_STATUS;
    }
    else if (getTypeName().matches(Regex.REGEX_JAVAFIELD)) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_invalidFieldX", getTypeName()));
    }
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "The super type can not be null!");
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public void setClientBundle(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
    if (isControlCreated()) {
      updateClientBundle();

    }
  }

  public NlsProposal getNlsName() {
    return (NlsProposal) getProperty(PROP_NLS_NAME);
  }

  public void setNlsName(NlsProposal proposal) {
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

  private void setNlsNameInternal(NlsProposal proposal) {
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

  public ITypeProposal getSuperType() {
    return (ITypeProposal) getProperty(PROP_SUPER_TYPE);
  }

  public void setSuperType(ITypeProposal superType) {
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

  private void setSuperTypeInternal(ITypeProposal superType) {
    setProperty(PROP_SUPER_TYPE, superType);
  }

  public ITypeProposal getTablePageType() {
    return (ITypeProposal) getProperty(PROP_TABLE_PAGE);
  }

  public void setTablePageType(ITypeProposal superType) {
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

  private void setTablePageTypeInternal(ITypeProposal superType) {
    setProperty(PROP_TABLE_PAGE, superType);
  }

}
