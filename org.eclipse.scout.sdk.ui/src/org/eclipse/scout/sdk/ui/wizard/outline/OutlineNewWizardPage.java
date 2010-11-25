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
package org.eclipse.scout.sdk.ui.wizard.outline;

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
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>OutlineNewWizardPage</h3> ...
 */
public class OutlineNewWizardPage extends AbstractWorkspaceWizardPage {
  final IType iOutline = ScoutSdk.getType(RuntimeClasses.IOutline);
  final IType abstractOutline = ScoutSdk.getType(RuntimeClasses.AbstractOutline);

  private NlsProposal m_nlsName;
  private String m_typeName;
  private ITypeProposal m_superType;
  private boolean m_addToDesktop;
  private boolean m_addToDesktopEnabled;

  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private Button m_addToDesktopField;

  // process members
  private OutlineNewOperation m_operation;
  private final IScoutBundle m_clientBundle;

  public OutlineNewWizardPage(IScoutBundle clientBundle) {
    super(OutlineNewWizardPage.class.getName());
    m_clientBundle = clientBundle;
    setTitle("New Outline");
    setDefaultMessage("Create a new outline.");
    // default values
    m_superType = ScoutProposalUtility.getScoutTypeProposalsFor(abstractOutline)[0];
    setAddToDesktopEnabled(false);
    setOperation(new OutlineNewOperation());
  }

  @Override
  protected void createContent(Composite parent) {

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, getClientBundle().findBestMatchNlsProject(), "Name");
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
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_OUTLINE);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(ScoutSdk.getType(RuntimeClasses.AbstractOutline));
    ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(SdkTypeUtility.getAbstractTypesOnClasspath(iOutline, getClientBundle().getJavaProject()));

    m_superTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(shotList, proposals), "Super Type");
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (ITypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    m_addToDesktopField = new Button(parent, SWT.CHECK);
    m_addToDesktopField.setSelection(isAddToDesktop());
    m_addToDesktopField.setText("Add to Desktop");
    m_addToDesktopField.setEnabled(isAddToDesktopEnabled());
    m_addToDesktopField.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_addToDesktop = m_addToDesktopField.getSelection();
        pingStateChanging();
      }
    });
    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_addToDesktopField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {

    getOperation().setClientBundle(getClientBundle());

    // write back members
    if (getNlsName() != null) {
      getOperation().setNlsEntry(getNlsName().getNlsEntry());
    }
    getOperation().setTypeName(getTypeName());
    ITypeProposal superTypeProp = getSuperType();
    if (superTypeProp != null) {
      getOperation().setSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true));
    }
    getOperation().setAddToDesktop(isAddToDesktop());

    getOperation().run(monitor, workingCopyManager);
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

  public void setOperation(OutlineNewOperation operation) {
    m_operation = operation;
  }

  public OutlineNewOperation getOperation() {
    return m_operation;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_OUTLINE)) {
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

  public void setAddToDesktop(boolean addToDesktop) {
    try {
      setStateChanging(true);
      m_addToDesktop = addToDesktop;
      if (isControlCreated()) {
        m_addToDesktopField.setSelection(addToDesktop);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public boolean isAddToDesktop() {
    return m_addToDesktop;
  }

  public void setAddToDesktopEnabled(boolean addToDesktopEnabled) {
    try {
      setStateChanging(true);
      m_addToDesktopEnabled = addToDesktopEnabled;
      if (isControlCreated()) {
        if (!addToDesktopEnabled) {
          setAddToDesktop(false);
        }
        m_addToDesktopField.setEnabled(addToDesktopEnabled);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public boolean isAddToDesktopEnabled() {
    return m_addToDesktopEnabled;
  }

}
