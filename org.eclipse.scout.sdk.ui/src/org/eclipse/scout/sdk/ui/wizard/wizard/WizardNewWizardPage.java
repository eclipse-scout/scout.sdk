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
package org.eclipse.scout.sdk.ui.wizard.wizard;

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
import org.eclipse.scout.sdk.operation.WizardNewOperation;
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
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>WizardNewWizardPage</h3> ...
 */
public class WizardNewWizardPage extends AbstractWorkspaceWizardPage {

  final IType iWizard = ScoutSdk.getType(RuntimeClasses.IWizard);
  final IType abstractWizard = ScoutSdk.getType(RuntimeClasses.AbstractWizard);

  private NlsProposal m_nlsName;
  private String m_typeName;
  private ITypeProposal m_superType;

  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;

  // process members
  private final IScoutBundle m_clientBundle;

  public WizardNewWizardPage(IScoutBundle clientBundle) {
    super("New Wizard");
    setTitle("New Wizard");
    setDefaultMessage("Create a new wizard.");
    m_clientBundle = clientBundle;
    m_superType = ScoutProposalUtility.getScoutTypeProposalsFor(abstractWizard)[0];
  }

  @Override
  protected void createContent(Composite parent) {
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, null, "Name");
    INlsProject nlsProject = getClientBundle().findBestMatchNlsProject();
    if (nlsProject != null) {
      m_nlsNameField.setNlsProject(nlsProject);
    }
    else {
      m_nlsNameField.setEnabled(false);
    }
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
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_WIZARD);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(abstractWizard);
    ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(SdkTypeUtility.getAbstractTypesOnClasspath(iWizard, getClientBundle().getJavaProject()));

    m_superTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(shotList, proposals), "Super Type");
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (ITypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    GridData tablePageData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    m_nlsNameField.setLayoutData(tablePageData);
    m_typeNameField.setLayoutData(tablePageData);
    m_superTypeField.setLayoutData(tablePageData);
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    WizardNewOperation m_operation = new WizardNewOperation();
    m_operation.setClientBundle(getClientBundle());
    // write back members
    if (getNlsName() != null) {
      m_operation.setNlsEntry(getNlsName().getNlsEntry());
    }
    m_operation.setTypeName(getTypeName());
    ITypeProposal superTypeProp = getSuperType();
    if (superTypeProp != null) {
      m_operation.setSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true));
    }

    m_operation.run(monitor, workingCopyManager);
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

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_WIZARD)) {
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
}
