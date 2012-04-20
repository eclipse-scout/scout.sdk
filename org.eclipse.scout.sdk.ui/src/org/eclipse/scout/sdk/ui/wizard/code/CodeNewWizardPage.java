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
package org.eclipse.scout.sdk.ui.wizard.code;

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
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.CodeNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.CodeIdExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.fields.code.CodeIdField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>CodeNewWizardPage</h3> ...
 */
public class CodeNewWizardPage extends AbstractWorkspaceWizardPage {

  final IType iCode = TypeUtility.getType(RuntimeClasses.ICode);
  final IType abstractCode = TypeUtility.getType(RuntimeClasses.AbstractCode);

  private String m_nextCodeId;
  private String m_nextCodeIdSource;
  private INlsEntry m_nlsName;
  private String m_typeName;
  private IType m_superType;
  private String m_genericSignature;
  private SiblingProposal m_sibling;

  private CodeIdField m_nextCodeIdField;
  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_genericTypeField;
  private ProposalTextField m_siblingField;

  // process members
  private final IType m_declaringType;
  private IType m_createdCode;

  public CodeNewWizardPage(IType declaringType) {
    super(CodeNewWizardPage.class.getName());
    setTitle(Texts.get("NewCode"));
    setDescription(Texts.get("CreateANewCode"));
    m_declaringType = declaringType;
    m_superType = abstractCode;
    m_genericSignature = Signature.createTypeSignature(Long.class.getName(), true);
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  public void postActivate() {
    m_nlsNameField.setFocus();
  }

  @Override
  protected void createContent(Composite parent) {
    IScoutProject project = ScoutSdkCore.getScoutWorkspace().getScoutBundle(m_declaringType.getJavaProject().getProject()).getScoutProject();
    m_nextCodeIdField = new CodeIdField(parent, project);
    m_nextCodeIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_nextCodeId = m_nextCodeIdField.getValue();
        m_nextCodeIdSource = m_nextCodeIdField.getValueSource();
        pingStateChanging();
      }
    });
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, ScoutTypeUtility.findNlsProject(m_declaringType), Texts.get("Name"));
    m_nlsNameField.acceptProposal(m_nlsName);
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = getNlsName();
          m_nlsName = (INlsEntry) event.proposal;
          if (m_nlsName != null) {
            if (oldEntry == null || oldEntry.getKey().equals(m_typeNameField.getModifiableText()) || StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(m_nlsName.getKey());
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_CODE);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new AbstractJavaElementContentProvider() {
          @Override
          protected Object[][] computeProposals() {
            return new Object[][]{TypeUtility.toArray(m_declaringType), ScoutTypeUtility.getCodes(m_declaringType)};
          }
        });
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_superType = (IType) event.proposal;
          if (TypeUtility.isGenericType(getSuperType())) {
            m_genericTypeField.setEnabled(true);
            if (getGenericSignature() == null) {
              m_genericTypeField.acceptProposal(TypeUtility.getType(Long.class.getName()));
            }
          }
          else {
            m_genericTypeField.setEnabled(false);
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_genericTypeField = getFieldToolkit().createSignatureProposalField(parent, Texts.get("GenericType"), ScoutTypeUtility.getScoutBundle(m_declaringType), SignatureProposalProvider.DEFAULT_PRIMITIV_SIGNATURES);
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_genericSignature = (String) event.proposal;
          if (m_genericSignature != null && getNextCodeId() == null) {
            setNextCodeId(CodeIdExtensionPoint.getNextCodeId(ScoutTypeUtility.getScoutProject(m_declaringType), getGenericSignature()));
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_siblingField = getFieldToolkit().createSiblingProposalField(parent, m_declaringType, iCode);
    m_siblingField.acceptProposal(m_sibling);
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_sibling = (SiblingProposal) event.proposal;
        pingStateChanging();
      }
    });
    m_nextCodeIdField.setGenericTypeField(m_genericTypeField);

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nextCodeIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    CodeNewOperation op = new CodeNewOperation(m_declaringType, true);

    // write back members
    op.setNlsEntry(getNlsName());
    op.setTypeName(getTypeName());
    IType superTypeProp = getSuperType();
    if (superTypeProp != null) {
      String sig = null;
      if (getGenericSignature() != null) {
        sig = Signature.createTypeSignature(superTypeProp.getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature()) + ">", true);
      }
      else {
        sig = Signature.createTypeSignature(superTypeProp.getFullyQualifiedName(), true);
      }
      op.setSuperTypeSignature(sig);
    }
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredCodeType(m_declaringType);
      op.setSibling(structuredType.getSibling(CATEGORIES.TYPE_CODE));
    }
    else {
      op.setSibling(getSibling().getElement());
    }
    op.setGenericTypeSignature(getGenericSignature());
    op.setNextCodeId(getNextCodeIdSource());
    op.run(monitor, workingCopyManager);
    m_createdCode = op.getCreatedCode();
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusCodeIdField());
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
      multiStatus.add(getStatusGenericType());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusCodeIdField() throws JavaModelException {
    if (isControlCreated()) {
      return m_nextCodeIdField.getStatus();
    }
    else {
      return Status.OK_STATUS;
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_CODE)) {
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

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusGenericType() throws JavaModelException {
    if (TypeUtility.isGenericType(getSuperType())) {
      if (getGenericSignature() == null) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeCanNotBeNull"));
      }
    }
    return Status.OK_STATUS;
  }

  public void setNextCodeId(String nextCodeId) {
    try {
      setStateChanging(true);
      m_nextCodeId = nextCodeId;
      if (isControlCreated()) {
        m_nextCodeIdField.setValue(nextCodeId);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getNextCodeId() {
    return m_nextCodeId;
  }

  public String getNextCodeIdSource() {
    return m_nextCodeIdSource;
  }

  /**
   * @return the createdCode
   */
  public IType getCreatedCode() {
    return m_createdCode;
  }

  public INlsEntry getNlsName() {
    return m_nlsName;
  }

  public void setNlsName(INlsEntry nlsName) {
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

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
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

  public void setGenericSignature(String genericType) {
    try {
      setStateChanging(true);
      m_genericSignature = genericType;
      if (isControlCreated()) {
        m_genericTypeField.acceptProposal(genericType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getGenericSignature() {
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
