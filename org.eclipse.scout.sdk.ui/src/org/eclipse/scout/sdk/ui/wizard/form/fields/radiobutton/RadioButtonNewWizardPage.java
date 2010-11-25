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
package org.eclipse.scout.sdk.ui.wizard.form.fields.radiobutton;

import java.util.ArrayList;
import java.util.Arrays;

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
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.field.RadioButtonNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
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
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>WizardStepNewWizardPage</h3> ...
 */
public class RadioButtonNewWizardPage extends AbstractWorkspaceWizardPage {

  final IType abstractRadioButton = ScoutSdk.getType(RuntimeClasses.AbstractRadioButton);

  private NlsProposal m_nlsName;
  private String m_typeName;
  private ITypeProposal m_superType;
  private SiblingProposal m_sibling;

  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_siblingField;

  // process members
  private final IType m_declaringType;
  private IType m_createdRadioButton;

  public RadioButtonNewWizardPage(IType declaringType) {
    super(RadioButtonNewWizardPage.class.getName());
    setTitle("New Radio Button");
    setDefaultMessage("Create a new radio button.");
    m_declaringType = declaringType;

    // default values
    ITypeProposal superType = getSuperType();
    if (superType == null) {
      superType = ScoutProposalUtility.getScoutTypeProposalsFor(abstractRadioButton)[0];
    }
    m_superType = superType;
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  protected void createContent(Composite parent) {

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, SdkTypeUtility.findNlsProject(m_declaringType), "Name");
    m_nlsNameField.acceptProposal(m_nlsName);
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
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

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, "Type Name");
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_BUTTON);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    ArrayList<IType> abstractRadioButtons = new ArrayList<IType>(Arrays.asList(SdkTypeUtility.getAbstractTypesOnClasspath(abstractRadioButton, m_declaringType.getJavaProject())));
    abstractRadioButtons.add(abstractRadioButton);
    ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(abstractRadioButtons.toArray(new IType[abstractRadioButtons.size()]));

    m_superTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(proposals), "Super Type");
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (ITypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    SiblingProposal[] availableSiblings = ScoutProposalUtility.getSiblingProposals(SdkTypeUtility.getFormFields(m_declaringType));
    m_siblingField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(availableSiblings), "Sibling");
    m_siblingField.acceptProposal(m_sibling);
    m_siblingField.setEnabled(availableSiblings != null && availableSiblings.length > 0);
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        m_sibling = (SiblingProposal) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    RadioButtonNewOperation op = new RadioButtonNewOperation(getDeclaringType());
    op.setTypeName(getTypeName());
    if (getNlsName() != null) {
      op.setNlsEntry(getNlsName().getNlsEntry());
    }
    ITypeProposal superTypeProp = getSuperType();
    if (superTypeProp != null) {
      op.setSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true));
    }
    SiblingProposal siblingProposal = getSibling();
    if (siblingProposal == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = SdkTypeUtility.createStructuredCompositeField(m_declaringType);
      op.setSibling(structuredType.getSibling(CATEGORIES.TYPE_FORM_FIELD));
    }
    else if (siblingProposal != null) {
      op.setSibling(siblingProposal.getScoutType());
    }

    op.setFormatSource(true);
    op.validate();
    op.run(monitor, workingCopyManager);
    m_createdRadioButton = op.getCreatedButton();
    return true;
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
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_BUTTON)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if(SdkTypeUtility.getAllTypes(m_declaringType.getCompilationUnit(), TypeFilters.getRegexSimpleNameFilter(getTypeName())).length > 0){
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

  /**
   * @return the declaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }

  /**
   * @return the createdWizardStep
   */
  public IType getCreatedRadioButton() {
    return m_createdRadioButton;
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

  public ITypeProposal getSuperType() {
    return m_superType;
  }

  public void setSuperType(ITypeProposal superType) {
    try {
      setStateChanging(true);
      m_superType = superType;
      if (isControlCreated()) {
        m_superTypeField.acceptProposal(superType);
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
