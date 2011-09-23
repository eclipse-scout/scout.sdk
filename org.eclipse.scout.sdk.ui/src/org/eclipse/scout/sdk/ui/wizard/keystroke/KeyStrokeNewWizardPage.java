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
package org.eclipse.scout.sdk.ui.wizard.keystroke;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.KeyStrokeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link KeyStrokeNewWizardPage}</h3> ...
 */
public class KeyStrokeNewWizardPage extends AbstractWorkspaceWizardPage {

  final IType iKeyStroke = ScoutSdk.getType(RuntimeClasses.IKeyStroke);
  final IType abstractKeyStroke = ScoutSdk.getType(RuntimeClasses.AbstractKeyStroke);

  private String m_typeName;
  private ITypeProposal m_superType;
  private String m_keyStroke;

  private StyledTextField m_typeNameField;
  private StyledTextField m_keyStrokeField;
  private ProposalTextField m_superTypeField;
  private KeyStrokeNewOperation m_operation;

  // process members
  private final IType m_declaringType;
  private IType m_createdKeystroke;

  public KeyStrokeNewWizardPage(IType declaringType) {
    super(Texts.get("NewKeyStroke"));
    setTitle(Texts.get("NewKeyStroke"));
    setDescription(Texts.get("CreateANewKeyStroke"));
    // default
    m_declaringType = declaringType;
    m_superType = ScoutProposalUtility.getScoutTypeProposalsFor(abstractKeyStroke)[0];
    setOperation(new KeyStrokeNewOperation(m_declaringType));
  }

  @Override
  protected void createContent(Composite parent) {

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_KEY_STROKE);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(abstractKeyStroke);

    ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(SdkTypeUtility.getAbstractTypesOnClasspath(iKeyStroke, m_declaringType.getJavaProject()));

    m_superTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(shotList, proposals), Texts.get("SuperType"));
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (ITypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    m_keyStrokeField = getFieldToolkit().createStyledTextField(parent, Texts.get("KeyStroke"));
    m_keyStrokeField.setText(getKeyStroke());
    m_keyStrokeField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_keyStroke = m_keyStrokeField.getText();
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_keyStrokeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    // write back members
    m_operation.setTypeName(getTypeName());
    ITypeProposal superTypeProp = getSuperType();
    if (superTypeProp != null) {
      m_operation.setSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true));
    }
    m_operation.setKeyStroke(getKeyStroke());
    // sibling
    IStructuredType structuredType = SdkTypeUtility.createStructuredForm(m_declaringType);
    m_operation.setSibling(structuredType.getSiblingTypeKeyStroke(getTypeName()));
    m_operation.run(monitor, workingCopyManager);
    m_createdKeystroke = m_operation.getCreatedKeyStroke();
    return true;
  }

  public KeyStrokeNewOperation getOperation() {
    return m_operation;
  }

  public void setOperation(KeyStrokeNewOperation operation) {
    m_operation = operation;
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
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_KEY_STROKE)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.exists(m_declaringType.getType(getTypeName()))) {
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
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  /**
   * @return the createdKeystroke
   */
  public IType getCreatedKeystroke() {
    return m_createdKeystroke;
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

  public void setKeyStroke(String keyStroke) {
    try {
      setStateChanging(true);
      m_keyStroke = keyStroke;
      if (isControlCreated()) {
        m_keyStrokeField.setText(keyStroke);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getKeyStroke() {
    return m_keyStroke;
  }
}
