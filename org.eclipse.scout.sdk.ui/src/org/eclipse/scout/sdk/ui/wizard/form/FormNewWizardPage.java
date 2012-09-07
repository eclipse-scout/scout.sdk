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
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
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

  final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
  final IType abstractForm = TypeUtility.getType(RuntimeClasses.AbstractForm);

  /** {@link NlsProposal} **/
  public static final String PROP_NLS_NAME = "nlsName";
  /** {@link String} **/
  public static final String PROP_TYPE_NAME = "typeName";
  /** {@link IType} **/
  public static final String PROP_SUPER_TYPE = "superType";
  /** {@link Boolean} **/
  public static final String PROP_CREATE_FORM_ID = "createFormId";
  /** {@link String} **/
  public static final String PROP_FORM_ID_NAME = "formIdName";

  // ui fields
  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private Button m_createFormIdField;
  private StyledTextField m_formIdField;

  // process members
  private final IScoutBundle m_clientBundle;

  public FormNewWizardPage(IScoutBundle clientBundle) {
    super(FormNewWizardPage.class.getName());
    m_clientBundle = clientBundle;
    setTitle(Texts.get("Form"));
    setDescription(Texts.get("CreateANewForm"));
    setSuperTypeInternal(abstractForm);
    setCreateFormId(true);
  }

  @Override
  protected void createContent(Composite parent) {
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, getClientBundle().findBestMatchNlsProject(), Texts.get("Name"));
    m_nlsNameField.acceptProposal(getNlsName());
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {

        try {
          setStateChanging(true);
          String oldKey = "";
          if (getNlsName() != null) {
            oldKey = getNlsName().getKey();
          }
          INlsEntry newName = (INlsEntry) event.proposal;
          setNlsNameInternal(newName);
          if (newName != null) {
            if (StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText()) || oldKey.equals(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(newName.getKey());
            }
            if (StringUtility.isNullOrEmpty(m_formIdField.getModifiableText()) || oldKey.equals(m_formIdField.getModifiableText())) {
              m_formIdField.setText(newName.getKey());
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_FORM);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iForm, getClientBundle().getJavaProject(), abstractForm));
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    Control formIdGroup = createIdGroup(parent);

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    formIdGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

  }

  protected Control createIdGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("FormId"));

    m_createFormIdField = new Button(group, SWT.CHECK);
    m_createFormIdField.setText(Texts.get("CreateFormId"));
    m_createFormIdField.setSelection(isCreateFormId());
    m_createFormIdField.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setCreateFormIdInternal(m_createFormIdField.getSelection());
        pingStateChanging();
      }
    });

    m_formIdField = getFieldToolkit().createStyledTextField(group, Texts.get("PropertyNameId"));
    m_formIdField.setReadOnlySuffix(SdkProperties.SUFFIX_ID);
    m_formIdField.setText(getTypeName());
    m_formIdField.addModifyListener(new ModifyListener() {
      @Override
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

  void fillOperation(FormStackNewOperation operation) {
    operation.setCreateIdProperty(isCreateFormId());
    operation.setFormIdName(getFormId());
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
      if (StringUtility.isNullOrEmpty(getFormId()) || getFormId().equals(SdkProperties.SUFFIX_ID)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("IdNameMissing"));
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_FORM)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.existsType(getClientBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_FORMS) + "." + getTypeName())) {
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
