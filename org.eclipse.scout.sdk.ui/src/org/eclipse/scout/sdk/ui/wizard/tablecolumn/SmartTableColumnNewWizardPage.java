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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.form.field.table.SmartTableColumnNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.buttongroup.ButtonGroup;
import org.eclipse.scout.sdk.ui.fields.buttongroup.IButtonGroupListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.ScoutWizardDialog;
import org.eclipse.scout.sdk.ui.wizard.tablecolumn.TableColumnNewWizard.CONTINUE_OPERATION;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
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
  private INlsEntry m_nlsName;
  private String m_typeName;
  private String m_genericSignature;
  private IType m_lookupCall;
  private IType m_codeType;
  private CONTINUE_OPERATION m_continueOperation;
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
  private final IScoutBundle m_clientBundle;
  private final IScoutBundle m_sharedBundle;
  private IType m_superType;
  private IType m_createdColumn;

  public SmartTableColumnNewWizardPage(IType declaringType, CONTINUE_OPERATION op) {
    super(SmartTableColumnNewWizardPage.class.getName());
    setTitle(Texts.get("NewSmartTableColumn"));
    setDescription(Texts.get("CreateANewSmartTableColumn"));
    m_declaringType = declaringType;
    m_clientBundle = ScoutTypeUtility.getScoutBundle(m_declaringType.getJavaProject());
    m_sharedBundle = m_clientBundle.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
    // default values
    m_genericSignature = SignatureCache.createTypeSignature(Long.class.getName());
    m_sibling = SiblingProposal.SIBLING_END;
    m_continueOperation = op;
    m_codeTypeDefinesGenericType = false;
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

    final AbstractJavaElementContentProvider codeTypeContentProvider = new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        IType iCodeType = TypeUtility.getType(IRuntimeClasses.ICodeType);
        Set<IType> codeTypes = TypeUtility.getClassesOnClasspath(iCodeType, getSharedBundle().getJavaProject(), TypeFilters.getTypeParamSubTypeFilter(getGenericSignature(), IRuntimeClasses.ICodeType, IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID));
        return new Object[][]{codeTypes.toArray(new IType[codeTypes.size()])};
      }
    };
    final AbstractJavaElementContentProvider lookupCallContentProvider = new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        IType iLookupCall = TypeUtility.getType(IRuntimeClasses.ILookupCall);
        ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getNoGenericTypesFilter(), TypeFilters.getTypeParamSubTypeFilter(getGenericSignature(), IRuntimeClasses.ILookupCall, IRuntimeClasses.TYPE_PARAM_LOOKUPCALL__KEY_TYPE));
        Set<IType> lookupCalls = TypeUtility.getClassesOnClasspath(iLookupCall, getSharedBundle().getJavaProject(), filter);
        return new Object[][]{lookupCalls.toArray(new IType[lookupCalls.size()])};
      }
    };

    m_genericTypeField = getFieldToolkit().createSignatureProposalField(g, Texts.get("GenericType"), ScoutTypeUtility.getScoutBundle(m_declaringType), SignatureProposalProvider.DEFAULT_MOST_USED);
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

    m_codeTypeField = getFieldToolkit().createJavaElementProposalField(g, Texts.get("CodeType"), codeTypeContentProvider);
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

    m_lookupCallField = getFieldToolkit().createJavaElementProposalField(g, Texts.get("LookupCall"), lookupCallContentProvider);
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
        pingStateChanging();
      }
    });

    IType iColumn = TypeUtility.getType(IRuntimeClasses.IColumn);
    m_siblingField = getFieldToolkit().createSiblingProposalField(g, m_declaringType, iColumn);
    m_siblingField.acceptProposal(m_sibling);
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
    nextStepOptions.setValue(m_continueOperation);
    nextStepOptions.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
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
    if (CONTINUE_OPERATION.ADD_MORE_COLUMNS == m_continueOperation) {
      // start another wizard if one additional column should be created.
      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          TableColumnNewWizard wizard = new TableColumnNewWizard(m_continueOperation);
          wizard.initWizard(m_declaringType);
          ScoutWizardDialog wizardDialog = new ScoutWizardDialog(wizard);
          wizardDialog.open();
        }
      });
    }

    SmartTableColumnNewOperation operation = new SmartTableColumnNewOperation(getTypeName(), m_declaringType, true);
    // write back members
    IType superType = getSuperType();
    if (superType != null) {
      String sig = null;
      if (getGenericSignature() != null) {
        sig = SignatureCache.createTypeSignature(superType.getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature()) + ">");
      }
      else {
        sig = SignatureCache.createTypeSignature(superType.getFullyQualifiedName());
      }
      operation.setSuperTypeSignature(sig);
    }
    operation.setNlsEntry(getNlsName());
    operation.setCodeType(getCodeType());
    if (getLookupCall() != null) {
      operation.setLookupCall(getLookupCall());
    }
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredTable(m_declaringType);
      operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_COLUMN));
    }
    else {
      operation.setSibling(getSibling().getElement());
    }
    operation.validate();
    operation.run(monitor, workingCopyManager);
    m_createdColumn = operation.getCreatedColumn();
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusNameField());
    multiStatus.add(getStatusGenericType());
    multiStatus.add(getStatusLookupCallCodeType());
    multiStatus.add(getStatusGenericTypeToLookupCall());
    multiStatus.add(getStatusGenericTypeToCodeType());
    if (isControlCreated()) {
      m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()) && !m_codeTypeDefinesGenericType);
    }
  }

  protected IType getGenericType(IType t, String genericDefiningType, String paramName) {
    if (TypeUtility.exists(t)) {
      try {
        ITypeHierarchy superHierarchy = TypeUtility.getSupertypeHierarchy(t);
        String typeParamSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(t, superHierarchy, genericDefiningType, paramName);
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
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(getTypeName(), SdkProperties.SUFFIX_TABLE_COLUMN);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    if (TypeUtility.exists(m_declaringType.getType(getTypeName()))) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    return javaFieldNameStatus;
  }

  protected IStatus getStatusGenericType() {
    if (TypeUtility.isGenericType(getSuperType())) {
      if (getGenericSignature() == null) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeCanNotBeNull"));
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusLookupCallCodeType() {
    if (getCodeType() != null && getLookupCall() != null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("OnlyOneOfCodeTypeOrLookupCallCanBeProcessed"));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
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

  public IType getCodeType() {
    return m_codeType;
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
