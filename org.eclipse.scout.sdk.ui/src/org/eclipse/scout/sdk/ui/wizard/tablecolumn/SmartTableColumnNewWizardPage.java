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

import java.util.List;

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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.SmartTableColumnNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.buttongroup.ButtonGroup;
import org.eclipse.scout.sdk.ui.fields.buttongroup.IButtonGroupListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.SignatureProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.ScoutWizardDialog;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

/**
 * <h3> {@link SmartTableColumnNewWizardPage}</h3> ...
 */
public class SmartTableColumnNewWizardPage extends AbstractWorkspaceWizardPage {

  private static enum CONTINUE_OPERATION {
    ADD_MORE_COLUMNS, FINISH
  }

  final IType iColumn = TypeUtility.getType(RuntimeClasses.IColumn);
  final IType lookupCall = TypeUtility.getType(RuntimeClasses.LookupCall);
  final IType iCodeType = TypeUtility.getType(RuntimeClasses.ICodeType);

  private NlsProposal m_nlsName;
  private String m_typeName;
  private SignatureProposal m_genericSignature;
  private ITypeProposal m_lookupCall;
  private ITypeProposal m_codeType;
  private SiblingProposal m_sibling;

  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_genericTypeField;
  private ProposalTextField m_lookupCallField;
  private ProposalTextField m_codeTypeField;
  private ProposalTextField m_siblingField;

  private CONTINUE_OPERATION m_continueOperation;

  // process members
  private final IType m_declaringType;
  private IType m_superType;
  private IType m_createdColumn;

  public SmartTableColumnNewWizardPage(IType declaringType) {
    super(Texts.get("NewSmartTableColumn"));
    setTitle(Texts.get("NewSmartTableColumn"));
    setDescription(Texts.get("CreateANewSmartTableColumn"));
    m_declaringType = declaringType;
    // default values
    m_genericSignature = new SignatureProposal(Signature.createTypeSignature(Long.class.getName(), true));
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  protected void createContent(Composite parent) {

    createColumnGroup(parent);
    createNextStepsGroup(parent);

    parent.setLayout(new GridLayout(1, true));
  }

  private void createColumnGroup(Composite p) {
    Group g = new Group(p, SWT.NONE);
    g.setText(Texts.get("Column"));

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(g, ScoutTypeUtility.findNlsProject(m_declaringType), Texts.get("Name"));
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
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_TABLE_COLUMN);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_genericTypeField = getFieldToolkit().createSignatureProposalField(g, ScoutTypeUtility.getScoutBundle(m_declaringType), Texts.get("GenericType"));
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_genericSignature = (SignatureProposal) event.proposal;
        pingStateChanging();
      }
    });
    ITypeProposal[] lookupCallProps = ScoutProposalUtility.getScoutTypeProposalsFor(ScoutTypeUtility.getClassesOnClasspath(lookupCall, getSharedBundle().getJavaProject()));
    m_lookupCallField = getFieldToolkit().createProposalField(g, new DefaultProposalProvider(lookupCallProps), Texts.get("LookupCall"));
    m_lookupCallField.acceptProposal(getLookupCall());
    m_lookupCallField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_lookupCall = (ITypeProposal) event.proposal;
          m_codeTypeField.acceptProposal(null);
          m_codeTypeField.setEnabled(m_lookupCall == null);
        }
        finally {
          setStateChanging(false);
        }
        pingStateChanging();
      }
    });

    ITypeProposal[] codeTypeProps = ScoutProposalUtility.getScoutTypeProposalsFor(ScoutTypeUtility.getClassesOnClasspath(iCodeType, getSharedBundle().getJavaProject()));
    m_codeTypeField = getFieldToolkit().createProposalField(g, new DefaultProposalProvider(codeTypeProps), Texts.get("CodeType"));
    m_codeTypeField.acceptProposal(getCodeType());
    m_codeTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_codeType = (ITypeProposal) event.proposal;
          m_lookupCallField.acceptProposal(null);
          m_lookupCallField.setEnabled(m_codeType == null);
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    SiblingProposal[] availableSiblings = ScoutProposalUtility.getSiblingProposals(ScoutTypeUtility.getColumns(m_declaringType));
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
    m_lookupCallField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_codeTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private void createNextStepsGroup(Composite p) {
    Group g = new Group(p, SWT.NONE);
    g.setText(Texts.get("NextStep"));
    g.setLayout(new GridLayout(1, false));
    g.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    ButtonGroup<CONTINUE_OPERATION> nextStepOptions = new ButtonGroup<CONTINUE_OPERATION>(g, SWT.RADIO);
    nextStepOptions.createButton(Texts.get("CreateMoreColumn"), CONTINUE_OPERATION.ADD_MORE_COLUMNS);
    nextStepOptions.createButton(Texts.get("FinishWizard"), CONTINUE_OPERATION.FINISH);
    nextStepOptions.addButtonGroupListener(new IButtonGroupListener<CONTINUE_OPERATION>() {

      @Override
      public void handleSelectionChanged(List<CONTINUE_OPERATION> newSelection) {
        m_continueOperation = newSelection.get(0);
      }
    });
    nextStepOptions.setValue(CONTINUE_OPERATION.FINISH);
    nextStepOptions.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (CONTINUE_OPERATION.ADD_MORE_COLUMNS == m_continueOperation) {
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

    SmartTableColumnNewOperation operation = new SmartTableColumnNewOperation(m_declaringType, true);
    // write back members
    IType superType = getSuperType();
    if (superType != null) {
      String sig = null;
      if (getGenericSignature() != null) {
        sig = Signature.createTypeSignature(superType.getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature().getSignature()) + ">", true);
      }
      else {
        sig = Signature.createTypeSignature(superType.getFullyQualifiedName(), true);
      }
      operation.setSuperTypeSignature(sig);
    }
    if (getNlsName() != null) {
      operation.setNlsEntry(getNlsName().getNlsEntry());
    }
    operation.setTypeName(getTypeName());
    if (getCodeType() != null) {
      operation.setCodeType(getCodeType().getType());
    }
    /*if (getLookupCall() != null) {
      operation.setLookupCall(getLookupCall().getType());
    }*/
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredTable(m_declaringType);
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
      multiStatus.add(getStatusLookupCallCodeType());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_TABLE_COLUMN)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.exists(m_declaringType.getType(getTypeName()))) {
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

  protected IStatus getStatusGenericType() throws JavaModelException {
    if (TypeUtility.isGenericType(getSuperType())) {
      if (getGenericSignature() == null) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeCanNotBeNull"));
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusLookupCallCodeType() {
    // if(getCodeType() == null && getLookupCall() == null){
    // return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "A lookup call or a code type must be set.");
    // }
    // else
    if (getCodeType() != null/* && getLookupCall() != null*/) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("OnlyOneOfCodeTypeOrLookupCallCanBeProcessed"));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getClientBundle() {
    return ScoutTypeUtility.getScoutBundle(m_declaringType);
  }

  public IScoutBundle getSharedBundle() {
    return getClientBundle().findBestMatchShared();
  }

  /**
   * @return the createdColumn
   */
  public IType getCreatedColumn() {
    return m_createdColumn;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
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

  public void setLookupCall(ITypeProposal lookupCall) {
    try {
      setStateChanging(true);
      m_lookupCall = lookupCall;
      if (isControlCreated()) {
        m_codeTypeField.acceptProposal(lookupCall);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public ITypeProposal getLookupCall() {
    return m_lookupCall;
  }

  public ITypeProposal getCodeType() {
    return m_codeType;
  }

  public void setCodeType(ITypeProposal codeType) {
    try {
      setStateChanging(true);
      m_codeType = codeType;
      if (isControlCreated()) {
        m_codeTypeField.acceptProposal(codeType);
      }
    }
    finally {
      setStateChanging(false);
    }
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
