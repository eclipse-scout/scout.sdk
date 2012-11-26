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
package org.eclipse.scout.sdk.ui.wizard.form.fields.listbox;

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
import org.eclipse.scout.sdk.operation.form.field.ListBoxFieldNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>DefaultFormFieldNewWizardPage</h3> ...
 */
public class ListBoxFieldNewWizardPage extends AbstractWorkspaceWizardPage {

  private final IType iListBox = TypeUtility.getType(RuntimeClasses.IListBox);
  private final IType iCodeType = TypeUtility.getType(RuntimeClasses.ICodeType);

  private INlsEntry m_nlsName;
  private String m_typeName;
  private IType m_superType;
  private String m_genericSignature;
  private IType m_codeType;
  private SiblingProposal m_sibling;

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_genericTypeField;
  private ProposalTextField m_codeTypeField;
  private ProposalTextField m_siblingField;

  // process members
  private final IType m_declaringType;
  private IType m_createdField;

  public ListBoxFieldNewWizardPage(IType declaringType) {
    super(ListBoxFieldNewWizardPage.class.getName());
    setTitle(Texts.get("NewListboxField"));
    setDescription(Texts.get("CreateANewListBoxField"));
    m_declaringType = declaringType;
    // default
    m_superType = RuntimeClasses.getSuperType(RuntimeClasses.IListBox, m_declaringType.getJavaProject());
    m_genericSignature = SignatureCache.createTypeSignature(Long.class.getName());
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  protected void createContent(Composite parent) {

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
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_FORM_FIELD);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_genericTypeField = getFieldToolkit().createSignatureProposalField(parent, Texts.get("GenericType"), ScoutTypeUtility.getScoutBundle(m_declaringType), SignatureProposalProvider.DEFAULT_MOST_USED);
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_genericSignature = (String) event.proposal;
        pingStateChanging();
      }
    });

    m_codeTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("CodeType"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        return new Object[][]{ScoutTypeUtility.getClassesOnClasspath(iCodeType, m_declaringType.getJavaProject())};
      }
    });
    m_codeTypeField.acceptProposal(getCodeType());
    m_codeTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_codeType = (IType) event.proposal;
        pingStateChanging();
      }
    });

    m_siblingField = getFieldToolkit().createFormFieldSiblingProposalField(parent, m_declaringType);
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_sibling = (SiblingProposal) event.proposal;
        pingStateChanging();
      }
    });
    m_sibling = (SiblingProposal) m_siblingField.getSelectedProposal();

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_codeTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ListBoxFieldNewOperation operation = new ListBoxFieldNewOperation(m_declaringType);
    operation.setFormatSource(true);
    // write back members
    if (getNlsName() != null) {
      operation.setNlsEntry(getNlsName());
    }
    operation.setTypeName(getTypeName());
    if (getSuperType() != null) {
      String sig = null;
      if (getGenericSignature() != null) {
        sig = SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature()) + ">");
      }
      else {
        sig = SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName());
      }
      operation.setSuperTypeSignature(sig);
    }

    operation.setCodeType(getCodeType());
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredCompositeField(m_declaringType);
      operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_FORM_FIELD));
    }
    else {
      operation.setSibling(getSibling().getElement());
    }
    operation.run(monitor, workingCopyManager);
    m_createdField = operation.getCreatedField();
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
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_FORM_FIELD)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (ScoutTypeUtility.getAllTypes(m_declaringType.getCompilationUnit(), TypeFilters.getRegexSimpleNameFilter(getTypeName())).length > 0) {
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

  /**
   * @return the createdField
   */
  public IType getCreatedField() {
    return m_createdField;
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
    m_superType = superType;
    pingStateChanging();
  }

  public void setGenericSignature(String genericSignature) {
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

  public String getGenericSignature() {
    return m_genericSignature;
  }

  public void setCodeType(IType codeType) {
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

  public IType getCodeType() {
    return m_codeType;
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
