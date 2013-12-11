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
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.form.FormStackNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
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
  private final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);

  public static final String PROP_NLS_NAME = "nlsName";
  public static final String PROP_TYPE_NAME = "typeName";
  public static final String PROP_SUPER_TYPE = "superType";
  public static final String PROP_CREATE_FORM_ID = "createFormId";
  public static final String PROP_FORM_ID_NAME = "formIdName";
  public static final String PROP_TARGET_PACKAGE = "targetPackage";

  // ui fields
  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private Button m_createFormIdField;
  private StyledTextField m_formIdField;
  private EntityTextField m_entityField;
  private IType m_abstractForm;

  // process members
  private final IScoutBundle m_clientBundle;

  public FormNewWizardPage(IScoutBundle clientBundle) {
    super(FormNewWizardPage.class.getName());
    m_clientBundle = clientBundle;
    if (clientBundle != null) {
      m_abstractForm = RuntimeClasses.getSuperType(RuntimeClasses.IForm, ScoutUtility.getJavaProject(m_clientBundle));
      setTargetPackage(DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_FORMS));
    }

    setTitle(Texts.get("CreateANewForm"));
    setDescription(Texts.get("CreateANewForm"));
    setSuperTypeInternal(m_abstractForm);
    setCreateFormId(false);
  }

  @Override
  protected void createContent(Composite p) {
    Group group = new Group(p, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("Form"));

    boolean isEnabled = getClientBundle() != null;
    int labelColWidthPercent = 20;

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
    m_nlsNameField.setEnabled(isEnabled);

    m_typeNameField = getFieldToolkit().createStyledTextField(group, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_FORM);
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
        new JavaElementAbstractTypeContentProvider(iForm, ScoutUtility.getJavaProject(getClientBundle()), m_abstractForm), labelColWidthPercent);
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

    Control formIdGroup = createIdGroup(p, labelColWidthPercent);

    m_nlsNameField.setFocus();

    // layout
    p.setLayout(new GridLayout(1, true));
    group.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    formIdGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  protected Control createIdGroup(Composite parent, int labelColWidthPercent) {
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
    m_createFormIdField.setEnabled(getClientBundle() != null);

    m_formIdField = getFieldToolkit().createStyledTextField(group, Texts.get("PropertyNameId"), labelColWidthPercent);
    m_formIdField.setReadOnlySuffix(SdkProperties.SUFFIX_ID);
    m_formIdField.setText(getTypeName());
    m_formIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setFormIdInternal(m_formIdField.getText());
        pingStateChanging();
      }
    });
    m_formIdField.setEnabled(getClientBundle() != null);

    // layout
    group.setLayout(new GridLayout(1, true));
    m_createFormIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_formIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  void fillOperation(FormStackNewOperation operation) {
    if (StringUtility.hasText(getFormId())) {
      operation.setFormIdSignature(SignatureCache.createTypeSignature(Long.class.getName()));
      operation.setFormIdName(getFormId());
    }

    if (getNlsName() != null) {
      operation.setNlsEntry(getNlsName());
    }
    IType superTypeProp = getSuperType();
    if (superTypeProp != null) {
      operation.setSuperTypeSignature(SignatureCache.createTypeSignature(superTypeProp.getFullyQualifiedName()));
    }
  }

  @Override
  public FormNewWizard getWizard() {
    return (FormNewWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusWorkspace());
    multiStatus.add(getStatusNameField());
    multiStatus.add(getStatusSuperType());
    multiStatus.add(getStatusPropertyId());
    multiStatus.add(getStatusTargetPackge());
  }

  protected IStatus getStatusWorkspace() {
    if (getClientBundle() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoNewXWithoutScoutBundle", Texts.get("Form")));
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

  protected IStatus getStatusPropertyId() {
    if (isCreateFormId()) {
      if (StringUtility.isNullOrEmpty(getFormId()) || getFormId().equals(SdkProperties.SUFFIX_ID)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("IdNameMissing"));
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() {
    IStatus javaFieldNameStatus = ScoutUtility.getJavaNameStatus(getTypeName(), SdkProperties.SUFFIX_FORM);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    IStatus existingStatus = ScoutUtility.getTypeExistingStatus(getClientBundle(), getTargetPackage(IDefaultTargetPackage.CLIENT_FORMS), getTypeName());
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
