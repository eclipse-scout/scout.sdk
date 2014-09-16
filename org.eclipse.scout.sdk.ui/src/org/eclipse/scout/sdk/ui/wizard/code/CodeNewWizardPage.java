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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.codeid.CodeIdExtensionPoint;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.CodeNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureSubTypeProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.fields.code.CodeIdField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
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

  private final IType iCode = TypeUtility.getType(IRuntimeClasses.ICode);

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
  private final IScoutBundle m_bundle;
  private IType m_createdCode;

  public CodeNewWizardPage(IType declaringType) {
    super(CodeNewWizardPage.class.getName());
    setTitle(Texts.get("NewCode"));
    setDescription(Texts.get("CreateANewCode"));
    m_declaringType = declaringType;
    m_bundle = ScoutTypeUtility.getScoutBundle(m_declaringType.getJavaProject());
    m_genericSignature = null;
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  public void postActivate() {
    m_nlsNameField.setFocus();
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;

    m_nextCodeIdField = new CodeIdField(parent, m_bundle, labelColWidthPercent);
    m_nextCodeIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_nextCodeId = m_nextCodeIdField.getValue();
        m_nextCodeIdSource = m_nextCodeIdField.getValueSource();
        pingStateChanging();
      }
    });
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, ScoutTypeUtility.findNlsProject(m_declaringType), Texts.get("PropText"), labelColWidthPercent);
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

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_CODE);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });
    m_typeNameField.setText(m_typeName);

    ITypeFilter filter = null;
    String codeIdSignatureFromCodeType = null;
    try {
      IType codeType = TypeUtility.getPrimaryType(m_declaringType);
      codeIdSignatureFromCodeType = ScoutTypeUtility.getCodeIdGenericTypeSignature(codeType);

      String codeSignature = ScoutTypeUtility.getCodeSignature(codeType, codeType.newSupertypeHierarchy(null));
      if (codeSignature != null) {
        if (codeIdSignatureFromCodeType != null) {
          filter = TypeFilters.getMultiTypeFilter(TypeFilters.getSubtypeFilter(TypeUtility.getTypeBySignature(codeSignature)), TypeFilters.getTypeParamSubTypeFilter(codeIdSignatureFromCodeType, IRuntimeClasses.ICode, IRuntimeClasses.TYPE_PARAM_CODE__CODE_ID));
        }
        else {
          filter = TypeFilters.getSubtypeFilter(TypeUtility.getTypeBySignature(codeSignature));
        }
      }
    }
    catch (CoreException e1) {
      ScoutSdkUi.logWarning("Cannot resolve CodeType generic type.", e1);
    }

    // default super type
    IType defaultSuperType = RuntimeClasses.getSuperType(IRuntimeClasses.ICode, m_declaringType.getJavaProject());
    if (filter == null || filter.accept(defaultSuperType)) {
      m_superType = defaultSuperType;
      if (codeIdSignatureFromCodeType != null) {
        setGenericSignature(codeIdSignatureFromCodeType);
      }
    }

    final SignatureSubTypeProposalProvider proposalProvider = new SignatureSubTypeProposalProvider(getGenericTypeOfSuperClass(), m_bundle.getJavaProject());

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iCode, m_declaringType.getJavaProject(), filter, m_superType), labelColWidthPercent);
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_superType = (IType) event.proposal;
          if (TypeUtility.isGenericType(getSuperType())) {
            m_genericTypeField.setEnabled(true);
          }
          else {
            m_genericTypeField.setEnabled(false);
          }

          IType genericTypeOfSuperClass = getGenericTypeOfSuperClass();
          try {
            if (TypeUtility.exists(genericTypeOfSuperClass) && (getGenericSignature() == null || !TypeUtility.getTypeBySignature(getGenericSignature()).newSupertypeHierarchy(null).contains(genericTypeOfSuperClass))) {
              m_genericTypeField.acceptProposal(SignatureCache.createTypeSignature(genericTypeOfSuperClass.getFullyQualifiedName()));
            }
            proposalProvider.setBaseType(genericTypeOfSuperClass);
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_genericTypeField = getFieldToolkit().createProposalField(parent, Texts.get("GenericType"), ProposalTextField.STYLE_DEFAULT, labelColWidthPercent);
    m_genericTypeField.setContentProvider(proposalProvider);
    m_genericTypeField.setLabelProvider(proposalProvider.getLabelProvider());
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          setGenericSignature((String) event.proposal);
          if (getGenericSignature() != null) {
            if (getNextCodeId() == null) {
              setNextCodeId(CodeIdExtensionPoint.getNextCodeId(m_bundle, getGenericSignature()));
            }
            else {
              m_nextCodeIdSource = m_nextCodeIdField.getValueSource();
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_siblingField = getFieldToolkit().createSiblingProposalField(parent, m_declaringType, iCode, labelColWidthPercent);
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
    String sig = null;
    if (getGenericSignature() != null && TypeUtility.isGenericType(getSuperType())) {
      sig = SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature()) + ">");
    }
    else {
      sig = SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName());
    }
    op.setSuperTypeSignature(sig);
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredCodeType(m_declaringType);
      op.setSibling(structuredType.getSibling(CATEGORIES.TYPE_CODE));
    }
    else {
      op.setSibling(getSibling().getElement());
    }
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
      multiStatus.add(getStatusGenericTypeToSuperClass());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusGenericTypeToSuperClass() {
    if (getGenericSignature() != null) {
      IType superType = getGenericTypeOfSuperClass();
      if (TypeUtility.exists(superType)) {
        IType generic = TypeUtility.getTypeBySignature(getGenericSignature());
        if (TypeUtility.exists(generic) && !TypeUtility.getSuperTypeHierarchy(generic).contains(superType)) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeDoesNotMatchSuperClass"));
        }
      }
    }
    return Status.OK_STATUS;
  }

  protected IType getGenericTypeOfSuperClass() {
    if (TypeUtility.exists(getSuperType())) {
      try {
        String typeParamSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(getSuperType(), getSuperType().newSupertypeHierarchy(null), IRuntimeClasses.ICode, IRuntimeClasses.TYPE_PARAM_CODE__CODE_ID);
        if (typeParamSig != null) {
          return TypeUtility.getTypeBySignature(typeParamSig);
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError(e);
      }
    }
    return null;
  }

  protected IStatus getStatusCodeIdField() {
    if (isControlCreated() && m_nextCodeIdField.getEnabled()) {
      return m_nextCodeIdField.getStatus();
    }
    else {
      return Status.OK_STATUS;
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(getTypeName(), SdkProperties.SUFFIX_CODE);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }

    for (IType t : m_declaringType.getTypes()) {
      if (t.getElementName().equals(getTypeName())) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }
    return javaFieldNameStatus;
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
      if (isControlCreated()) {
        m_typeNameField.setText(typeName);
      }
      m_typeName = typeName;
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
