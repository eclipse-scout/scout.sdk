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
import org.eclipse.scout.nls.sdk.internal.ui.action.NlsProposal;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.FormStackNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
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

  /** {@link NlsProposal} **/
  public static final String PROP_NLS_NAME = "nlsName";
  /** {@link String} **/
  public static final String PROP_TYPE_NAME = "typeName";
  /** {@link IType} **/
  public static final String PROP_SUPER_TYPE = "superType";
  /** {@link IType} **/
  public static final String PROP_TABLE_PAGE = "tablePage";

  final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
  final IType iSearchForm = TypeUtility.getType(RuntimeClasses.ISearchForm);
  final IType abstractSearchForm = TypeUtility.getType(RuntimeClasses.AbstractSearchForm);
  final IType iPage = TypeUtility.getType(RuntimeClasses.IPage);
  final IType iPageWithTable = TypeUtility.getType(RuntimeClasses.IPageWithTable);
  // ui fields
  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_tablePageField;

  // process members
  private final IScoutBundle m_clientBundle;

  public SearchFormNewWizardPage(IScoutBundle clientBundle) {
    super(SearchFormNewWizardPage.class.getName());
    m_clientBundle = clientBundle;
    setTitle(Texts.get("SearchForm2"));
    setDescription(Texts.get("CreateANewSearchForm"));
    setSuperTypeInternal(abstractSearchForm);
  }

  @Override
  protected void createContent(Composite parent) {
    Control tablePageGroup = createTablePageGroup(parent);
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, getClientBundle().findBestMatchNlsProject(), Texts.get("Name"));
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
              m_typeNameField.setText(newName.getKey());
            }

          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_SEARCH_FORM);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iSearchForm, getClientBundle().getJavaProject(), abstractSearchForm));
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    tablePageGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private Control createTablePageGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("AutoCreate"));
    Label label = new Label(group, SWT.NONE);
    label.setText(Texts.get("SelectTablePageForSearchForm"));
    m_tablePageField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("TablePage"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        IType[] list = TypeUtility.getPrimaryTypeHierarchy(iPage).getAllSubtypes(iPageWithTable, TypeFilters.getTypesOnClasspath(getClientBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
        return new Object[][]{list};
      }
    });
    m_tablePageField.acceptProposal(getTablePageType());
    m_tablePageField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setTablePageTypeInternal((IType) event.proposal);
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

  void fillOperation(FormStackNewOperation operation) {
    // operation.setCreateIdProperty(isCreateFormId());
    // operation.setFormIdName(getFormId());
    operation.setFormName(getTypeName());
    if (getNlsName() != null) {
      operation.setNlsEntry(getNlsName());
    }
    IType superTypeProp = getSuperType();
    if (superTypeProp != null) {
      operation.setFormSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getFullyQualifiedName(), true));
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
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_SEARCH_FORM)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.existsType(getClientBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_SEARCHFORMS) + "." + getTypeName())) {
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

  protected IStatus getStatusSuperType() throws JavaModelException {
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

}
