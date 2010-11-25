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
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>FormNewWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 03.08.2009
 */
public class FormNewWizardPage extends AbstractWorkspaceWizardPage {

  private static final IType iForm = ScoutSdk.getType(RuntimeClasses.IForm);
  private static final IType abstractForm = ScoutSdk.getType(RuntimeClasses.AbstractForm);

  /** {@link NlsProposal} **/
  public static final String PROP_NLS_NAME = "nlsName";
  /** {@link String} **/
  public static final String PROP_TYPE_NAME = "typeName";
  /** {@link ITypeProposal} **/
  public static final String PROP_SUPER_TYPE = "superType";
  /** {@link Boolean} **/
  public static final String PROP_CREATE_FORM_ID = "createFormId";
  /** {@link String} **/
  public static final String PROP_FORM_ID_NAME = "formIdName";

  // ui fields
  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private Button m_createFormIdField;
  private StyledTextField m_formIdField;

  // process members
  private IScoutBundle m_clientBundle;

  public FormNewWizardPage(IScoutBundle clientBundle) {
    super(FormNewWizardPage.class.getName());
    m_clientBundle = clientBundle;
    setTitle("New Form");
    setDefaultMessage("Create a new form.");
    setSuperTypeInternal(ScoutProposalUtility.getScoutTypeProposalsFor(abstractForm)[0]);
    setCreateFormId(true);
  }

  @Override
  protected void createContent(Composite parent) {
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
            if (StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText()) || oldEntry.getKey().equals(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(newName.getNlsEntry().getKey());
            }
            if (StringUtility.isNullOrEmpty(m_formIdField.getModifiableText()) || oldEntry.getKey().equals(m_formIdField.getModifiableText())) {
              m_formIdField.setText(newName.getNlsEntry().getKey());
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, "Type Name");
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_FORM);
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

    Control formIdGroup = createIdGroup(parent);

    updateClientBundle();
    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    formIdGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

  }

  protected Control createIdGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText("Form ID");

    m_createFormIdField = new Button(group, SWT.CHECK);
    m_createFormIdField.setText("Create form ID");
    m_createFormIdField.setSelection(isCreateFormId());
    m_createFormIdField.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setCreateFormIdInternal(m_createFormIdField.getSelection());
        pingStateChanging();
      }
    });

    m_formIdField = getFieldToolkit().createStyledTextField(group, "Property name ID");
    m_formIdField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_ID);
    m_formIdField.setText(getTypeName());
    m_formIdField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setFormIdInternal(m_formIdField.getText());
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    m_createFormIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_formIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  private void updateClientBundle() {

    DefaultProposalProvider superTypeProvider = new DefaultProposalProvider();
    INlsProject nlsProject = null;
    if (getClientBundle() != null) {
      nlsProject = getClientBundle().findBestMatchNlsProject();
      ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(abstractForm);
      ICachedTypeHierarchy formHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iForm);
      IType[] abstractForms = formHierarchy.getAllClasses(TypeFilters.getAbstractOnClasspath(getClientBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
      ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(abstractForms);
      superTypeProvider = new DefaultProposalProvider(shotList, proposals);
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

  }

  void fillOperation(FormStackNewOperation operation) {
    operation.setCreateIdProperty(isCreateFormId());
    operation.setFormIdName(getFormId());
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
  public FormNewWizard getWizard() {
    return (FormNewWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
      multiStatus.add(getStatusPropertyId());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusPropertyId() {
    if (isCreateFormId()) {
      if (StringUtility.isNullOrEmpty(getFormId()) || getFormId().equals(ScoutIdeProperties.SUFFIX_ID)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Id name is not set.");
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_FORM)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.exists(ScoutSdk.getType(getClientBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_FORMS) + "." + getTypeName()))) {
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

  public boolean isCreateFormId() {
    return getPropertyBool(PROP_CREATE_FORM_ID);
  }

  public void setCreateFormId(boolean createFormId) {
    try {
      setStateChanging(true);
      setCreateFormIdInternal(createFormId);
      if (isControlCreated()) {
        m_createFormIdField.setSelection(createFormId);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setCreateFormIdInternal(boolean createFormId) {
    setPropertyBool(PROP_CREATE_FORM_ID, createFormId);
  }

  public String getFormId() {
    return getPropertyString(PROP_FORM_ID_NAME);
  }

  public void setFormId(String formId) {
    try {
      setStateChanging(true);
      setFormIdInternal(formId);
      if (isControlCreated()) {
        m_formIdField.setText(formId);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setFormIdInternal(String formId) {
    setPropertyString(PROP_FORM_ID_NAME, formId);
  }

}
