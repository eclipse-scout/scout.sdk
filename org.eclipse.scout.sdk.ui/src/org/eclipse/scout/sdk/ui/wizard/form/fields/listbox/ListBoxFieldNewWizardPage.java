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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
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
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
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

  private INlsEntry m_nlsName;
  private String m_typeName;
  private IType m_superType;
  private String m_genericSignature;
  private IType m_codeType;
  private IType m_lookupCall;
  private SiblingProposal m_sibling;
  private boolean m_codeTypeDefinesGenericType;

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_genericTypeField;
  private ProposalTextField m_codeTypeField;
  private ProposalTextField m_lookupCallField;
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
    m_superType = RuntimeClasses.getSuperType(IRuntimeClasses.IListBox, m_declaringType.getJavaProject());
    m_genericSignature = SignatureCache.createTypeSignature(Long.class.getName());
    m_sibling = SiblingProposal.SIBLING_END;
    m_codeTypeDefinesGenericType = false;
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

    final AbstractJavaElementContentProvider codeTypeContentProvider = new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        IType iCodeType = TypeUtility.getType(IRuntimeClasses.ICodeType);
        Set<IType> codeTypes = TypeUtility.getClassesOnClasspath(iCodeType, m_declaringType.getJavaProject(), TypeFilters.getTypeParamSubTypeFilter(getGenericSignature(), IRuntimeClasses.ICodeType, IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID));
        return new Object[][]{codeTypes.toArray(new IType[codeTypes.size()])};
      }
    };
    final AbstractJavaElementContentProvider lookupCallContentProvider = new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        IType iLookupCall = TypeUtility.getType(IRuntimeClasses.ILookupCall);
        ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getNoGenericTypesFilter(), TypeFilters.getTypeParamSubTypeFilter(getGenericSignature(), IRuntimeClasses.ILookupCall, IRuntimeClasses.TYPE_PARAM_LOOKUPCALL__KEY_TYPE));
        Set<IType> codeTypes = TypeUtility.getClassesOnClasspath(iLookupCall, m_declaringType.getJavaProject(), filter);
        return new Object[][]{codeTypes.toArray(new IType[codeTypes.size()])};
      }
    };

    m_genericTypeField = getFieldToolkit().createSignatureProposalField(parent, Texts.get("GenericType"), ScoutTypeUtility.getScoutBundle(m_declaringType), SignatureProposalProvider.DEFAULT_MOST_USED);
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_genericSignature = (String) event.proposal;
        codeTypeContentProvider.invalidateCache();
        lookupCallContentProvider.invalidateCache();
        pingStateChanging();
      }
    });

    m_codeTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("CodeType"), codeTypeContentProvider);
    m_codeTypeField.acceptProposal(getCodeType());
    m_codeTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_codeType = (IType) event.proposal;
          m_lookupCallField.acceptProposal(null);
          m_lookupCallField.setEnabled(m_codeType == null);
          readGenericType(m_codeType, getCodeTypeGenericTypeSignature());
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_lookupCallField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("LookupCall"), lookupCallContentProvider);
    m_lookupCallField.acceptProposal(getLookupCall());
    m_lookupCallField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_lookupCall = (IType) event.proposal;
          m_codeTypeField.acceptProposal(null);
          m_codeTypeField.setEnabled(m_lookupCall == null);
          readGenericType(m_lookupCall, getLookupCallGenericTypeSignature());
        }
        finally {
          setStateChanging(false);
        }
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
    m_lookupCallField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private String getCodeTypeGenericTypeSignature() {
    try {
      return ScoutTypeUtility.getCodeIdGenericTypeSignature(m_codeType);
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("Could not compute generic type of code type '" + m_codeType.getFullyQualifiedName() + "'.", e);
      return null;
    }
  }

  private String getLookupCallGenericTypeSignature() {
    if (m_lookupCall == null) {
      return null;
    }
    try {
      return SignatureUtility.resolveGenericParameterInSuperHierarchy(m_lookupCall, TypeUtility.getSupertypeHierarchy(m_lookupCall), IRuntimeClasses.ILookupCall, IRuntimeClasses.TYPE_PARAM_LOOKUPCALL__KEY_TYPE);
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("Could not compute generic type of lookup call '" + m_lookupCall.getFullyQualifiedName() + "'.", e);
      return null;
    }
  }

  private void readGenericType(IType genericDefiningType, String signature) {
    m_codeTypeDefinesGenericType = false;
    if (TypeUtility.exists(genericDefiningType)) {
      if (signature != null) {
        m_codeTypeDefinesGenericType = true;
        m_genericSignature = signature;
        m_genericTypeField.acceptProposal(getGenericSignature());
      }
    }
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ListBoxFieldNewOperation operation = new ListBoxFieldNewOperation(getTypeName(), m_declaringType);
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

    operation.setCodeType(getCodeType());
    operation.setLookupCall(getLookupCall());

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
    multiStatus.add(getStatusNameField());
    multiStatus.add(getStatusGenericType());
    multiStatus.add(getStatusGenericTypeToLookupCall());
    multiStatus.add(getStatusGenericTypeToCodeType());
    if (isControlCreated()) {
      m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()) && !m_codeTypeDefinesGenericType);
    }
  }

  protected IType getGenericType(IType t, String genericDefiningType, String paramName) {
    if (TypeUtility.exists(t)) {
      try {
        String typeParamSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(t, ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(t), genericDefiningType, paramName);
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

  protected IStatus getStatusGenericTypeToLookupCall() {
    if (getLookupCall() != null) {
      IType lookupCallKeyType = getGenericType(getLookupCall(), IRuntimeClasses.ILookupCall, IRuntimeClasses.TYPE_PARAM_LOOKUPCALL__KEY_TYPE);
      if (TypeUtility.exists(lookupCallKeyType)) {
        IType generic = TypeUtility.getTypeBySignature(getGenericSignature());
        if (TypeUtility.exists(generic) && !TypeUtility.getSupertypeHierarchy(generic).contains(lookupCallKeyType)) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("LookupCallDoesNotMatchGeneric"));
        }
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusGenericTypeToCodeType() {
    if (getCodeType() != null) {
      IType codeTypeKeyType = getGenericType(getCodeType(), IRuntimeClasses.ICodeType, IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID);
      if (TypeUtility.exists(codeTypeKeyType)) {
        IType generic = TypeUtility.getTypeBySignature(getGenericSignature());
        if (TypeUtility.exists(generic) && !TypeUtility.getSupertypeHierarchy(generic).contains(codeTypeKeyType)) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("CodeTypeDoesNotMatchGeneric"));
        }
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() {
    return ScoutUtility.validateFormFieldName(getTypeName(), SdkProperties.SUFFIX_FORM_FIELD, m_declaringType);
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

  public void setLookupCall(IType lookupCall) {
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

  public IType getLookupCall() {
    return m_lookupCall;
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
