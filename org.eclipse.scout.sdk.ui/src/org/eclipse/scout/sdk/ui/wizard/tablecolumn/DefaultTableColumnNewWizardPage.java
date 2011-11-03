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
package org.eclipse.scout.sdk.ui.wizard.tablecolumn;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.scout.sdk.operation.TableColumnNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.SelectionButtonGroup;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.SignatureProposal;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.ScoutWizardDialog;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>DefaultTableColumnNewWizardPage</h3> ...
 */
public class DefaultTableColumnNewWizardPage extends AbstractWorkspaceWizardPage {

  final IType iColumn = ScoutSdk.getType(RuntimeClasses.IColumn);

  private NlsProposal m_nlsName;
  private String m_typeName;
  private SignatureProposal m_genericSignature;
  private SiblingProposal m_sibling;

  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_genericTypeField;
  private ProposalTextField m_siblingField;

  private boolean m_addAdditionalColumn;
  private SelectionButtonGroup<Boolean> m_nextStepOptions;

  // process members
  private final IType m_declaringType;
  private IType m_superType;
  private IType m_createdColumn;

  public DefaultTableColumnNewWizardPage(IType declaringType) {
    super(Texts.get("NewTableColumn"));
    setTitle(Texts.get("NewTableColumn"));
    setDescription(Texts.get("CreateANewTableColumn"));
    // default values
    m_genericSignature = new SignatureProposal(Signature.createTypeSignature(Long.class.getName(), true));
    m_declaringType = declaringType;
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  protected void createContent(Composite parent) {

    createFieldGroup(parent);
    createNextStepsGroup(parent);

    parent.setLayout(new GridLayout(1, false));
  }

  private void createNextStepsGroup(Composite p) {
    Group g = new Group(p, SWT.NONE);
    g.setText(Texts.get("NextStep"));
    g.setLayout(new GridLayout(1, false));
    g.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    m_nextStepOptions = new SelectionButtonGroup<Boolean>(g, SelectionButtonGroup.BUTTON_TYPE_RADIO);
    m_nextStepOptions.addButton(Texts.get("CreateMoreColumn"), true);
    m_nextStepOptions.addButton(Texts.get("FinishWizard"), false);
    m_nextStepOptions.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_addAdditionalColumn = m_nextStepOptions.getValue();
      }
    });
    m_nextStepOptions.setValue(false);
    m_nextStepOptions.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private void createFieldGroup(Composite p) {
    Group g = new Group(p, SWT.NONE);
    g.setText(Texts.get("Column"));

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(g, null, Texts.get("Name"));
    INlsProject nlsProject = SdkTypeUtility.findNlsProject(m_declaringType);
    if (nlsProject != null) {
      m_nlsNameField.setNlsProject(nlsProject);
    }
    else {
      m_nlsNameField.setEnabled(false);
    }
    m_nlsNameField.acceptProposal(m_nlsName);
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = null;
          if (getNlsName() != null) {
            oldEntry = getNlsName().getNlsEntry();
          }
          m_nlsName = (NlsProposal) event.proposal;
          if (m_nlsName != null) {
            if (oldEntry == null || oldEntry.getKey().equals(m_typeNameField.getModifiableText()) || StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(m_nlsName.getNlsEntry().getKey());
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(g, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_TABLE_COLUMN);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_genericTypeField = getFieldToolkit().createSignatureProposalField(g, SdkTypeUtility.getScoutBundle(m_declaringType), Texts.get("GenericType"));
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_genericSignature = (SignatureProposal) event.proposal;
        pingStateChanging();
      }
    });

    SiblingProposal[] availableSiblings = ScoutProposalUtility.getSiblingProposals(SdkTypeUtility.getColumns(m_declaringType));
    m_siblingField = getFieldToolkit().createProposalField(g, new DefaultProposalProvider(availableSiblings), Texts.get("Sibling"));
    m_siblingField.acceptProposal(m_sibling);
    m_siblingField.setEnabled(availableSiblings != null && availableSiblings.length > 0);
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_sibling = (SiblingProposal) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    g.setLayout(new GridLayout(1, false));
    g.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {

    if (m_addAdditionalColumn) {
      // start another wizard if one additional column should be created.
      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          TableColumnNewWizard wizard = new TableColumnNewWizard();
          wizard.initWizard(m_declaringType);
          ScoutWizardDialog wizardDialog = new ScoutWizardDialog(wizard);
          wizardDialog.open();
        }
      });
    }

    TableColumnNewOperation operation = new TableColumnNewOperation(m_declaringType, true);
    // write back members
    IType superTypeProp = getSuperType();
    if (superTypeProp != null) {
      String sig = null;
      if (getGenericSignature() != null) {
        sig = Signature.createTypeSignature(superTypeProp.getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature().getSignature()) + ">", true);
      }
      else {
        sig = Signature.createTypeSignature(superTypeProp.getFullyQualifiedName(), true);
      }
      operation.setSuperTypeSignature(sig);
    }
    if (getNlsName() != null) {
      operation.setNlsEntry(getNlsName().getNlsEntry());
    }
    operation.setTypeName(getTypeName());
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = SdkTypeUtility.createStructuredTable(m_declaringType);
      operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_COLUMN));
    }
    else {
      operation.setSibling(getSibling().getScoutType());
    }
    operation.validate();
    operation.run(monitor, workingCopyManager);
    m_createdColumn = operation.getCreatedColumn();
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusGenericType());
      if (isControlCreated()) {
        m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_TABLE_COLUMN)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.exists(m_declaringType.getType(getTypeName()))) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    if (Regex.REGEX_WELLFORMD_JAVAFIELD.matcher(getTypeName()).matches()) {
      return Status.OK_STATUS;
    }
    else if (Regex.REGEX_JAVAFIELD.matcher(getTypeName()).matches()) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_invalidFieldX", getTypeName()));
    }
  }

  /**
   * @return the createdColumn
   */
  public IType getCreatedColumn() {
    return m_createdColumn;
  }

  protected IStatus getStatusGenericType() throws JavaModelException {
    if (TypeUtility.isGenericType(getSuperType())) {
      if (getGenericSignature() == null) {
        return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Generic type can not be null!");
      }
    }
    return Status.OK_STATUS;
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      if (TypeUtility.isGenericType(getSuperType())) {
        setGenericSignature(new SignatureProposal(Signature.createTypeSignature(Long.class.getName(), true)));
      }
      else {
        setGenericSignature(null);
      }
      m_superType = superType;
    }
    finally {
      setStateChanging(false);
    }
  }

  public IType getSuperType() {
    return m_superType;
  }

  public NlsProposal getNlsName() {
    return m_nlsName;
  }

  public void setNlsName(NlsProposal nlsName) {
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

  public void setGenericSignature(SignatureProposal genericSignature) {
    try {
      setStateChanging(true);
      m_genericSignature = genericSignature;
      if (isControlCreated()) {
        m_genericTypeField.acceptProposal(genericSignature);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public SignatureProposal getGenericSignature() {
    return m_genericSignature;
  }

  public SiblingProposal getSibling() {
    return m_sibling;
  }

  public void setSibling(SiblingProposal sibling) {
    try {
      setStateChanging(true);
      m_sibling = sibling;
      if (isControlCreated()) {
        m_siblingField.acceptProposal(sibling);
      }
    }
    finally {
      setStateChanging(false);
    }
  }
}
