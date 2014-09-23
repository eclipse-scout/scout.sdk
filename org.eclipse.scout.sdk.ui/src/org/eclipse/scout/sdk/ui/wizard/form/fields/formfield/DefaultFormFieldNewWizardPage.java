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
package org.eclipse.scout.sdk.ui.wizard.form.fields.formfield;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.form.field.DefaultFormFieldNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
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
 * <h3>DefaultFormFieldNewWizardPage</h3>
 */
public class DefaultFormFieldNewWizardPage extends AbstractWorkspaceWizardPage {

  private INlsEntry m_nlsName;
  private String m_typeName;
  private IType m_superType;
  private String m_genericSignature;
  private SiblingProposal m_sibling;

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_genericTypeField;
  private ProposalTextField m_siblingField;

  // process members
  private final IType m_declaringType;
  private final IType m_abstractFormField;
  private IType m_createdField;

  public DefaultFormFieldNewWizardPage(IType declaringType) {
    super(DefaultFormFieldNewWizardPage.class.getName());
    m_declaringType = declaringType;
    m_abstractFormField = RuntimeClasses.getSuperType(IRuntimeClasses.IFormField, m_declaringType.getJavaProject());
    m_superType = m_abstractFormField;
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  protected void createContent(Composite parent) {
    setTitle(Texts.get("NewDefaultField"));
    setDescription(Texts.get("CreateANewDefaultField"));

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, ScoutTypeUtility.findNlsProject(m_declaringType), Texts.get("PropLabel"));
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
              m_typeNameField.setText(NamingUtility.toJavaCamelCase(m_nlsName.getKey(), false));
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

    IType iFormField = TypeUtility.getType(IRuntimeClasses.IFormField);
    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iFormField, m_declaringType.getJavaProject(), m_abstractFormField));
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_superType = (IType) event.proposal;
          m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
        }
        finally {
          setStateChanging(false);
        }
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

    m_siblingField = getFieldToolkit().createFormFieldSiblingProposalField(parent, m_declaringType);
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_sibling = (SiblingProposal) event.proposal;
        pingStateChanging();
      }
    });
    if (m_sibling == null) {
      m_sibling = (SiblingProposal) m_siblingField.getSelectedProposal();
    }
    else {
      m_siblingField.acceptProposal(m_sibling);
    }

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    DefaultFormFieldNewOperation operation = new DefaultFormFieldNewOperation(getTypeName(), m_declaringType, true);
    // write back members
    if (getNlsName() != null) {
      operation.setNlsEntry(getNlsName());
    }
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
    if (getSibling() == SiblingProposal.SIBLING_END || getSibling() == null) {
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
    multiStatus.add(getStatusNameField());
    multiStatus.add(getStatusSuperType());
    multiStatus.add(getStatusGenericType());
  }

  protected IStatus getStatusNameField() {
    return ScoutUtility.validateFormFieldName(getTypeName(), SdkProperties.SUFFIX_FORM_FIELD, m_declaringType);
  }

  protected IStatus getStatusSuperType() {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusGenericType() {
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
