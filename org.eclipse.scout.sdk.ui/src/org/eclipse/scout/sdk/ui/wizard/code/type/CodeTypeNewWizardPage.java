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
package org.eclipse.scout.sdk.ui.wizard.code.type;

import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.codeid.CodeIdExtensionPoint;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.CodeTypeNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureSubTypeProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.fields.code.CodeIdField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.ITypeGenericMapping;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link CodeTypeNewWizardPage}</h3>
 */
public class CodeTypeNewWizardPage extends AbstractWorkspaceWizardPage {

  private final IType iCodeType = TypeUtility.getType(IRuntimeClasses.ICodeType);

  protected String m_nextCodeId;
  protected String m_nextCodeIdSource;
  protected INlsEntry m_nlsName;
  protected String m_typeName;
  protected String m_packageName;
  protected IType m_superType;
  protected IType m_defaultCodeType;
  protected String m_genericSignature;
  protected String m_genericCodeIdSignature;
  protected List<ITypeParameter> m_superTypeParameters;

  protected CodeIdField m_nextCodeIdField;
  protected ProposalTextField m_nlsNameField;
  protected StyledTextField m_typeNameField;
  protected EntityTextField m_entityField;
  protected ProposalTextField m_superTypeField;
  protected ProposalTextField m_genericTypeField;
  protected ProposalTextField m_genericCodeIdField;

  // process members
  private final IScoutBundle m_sharedBundle;

  public CodeTypeNewWizardPage(IScoutBundle sharedBundle) {
    super(CodeTypeNewWizardPage.class.getName());
    m_sharedBundle = sharedBundle;
    setTitle(Texts.get("NewCodeType"));
    setDescription(Texts.get("CreateANewCodeType"));
    if (m_sharedBundle != null) {
      setTargetPackage(DefaultTargetPackage.get(m_sharedBundle, IDefaultTargetPackage.SHARED_SERVICES_CODE));
      m_defaultCodeType = RuntimeClasses.getSuperType(IRuntimeClasses.ICodeType, ScoutUtility.getJavaProject(m_sharedBundle));
    }
    m_superType = m_defaultCodeType;
    m_genericSignature = SignatureCache.createTypeSignature(Long.class.getName());
    m_genericCodeIdSignature = m_genericSignature;
  }

  @Override
  public void postActivate() {
    m_nlsNameField.setFocus();
  }

  protected boolean isPageEnabled() {
    return getSharedBundle() != null;
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;
    final boolean isEnabled = isPageEnabled();
    m_nextCodeIdField = new CodeIdField(parent, getSharedBundle(), labelColWidthPercent);
    m_nextCodeIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_nextCodeId = m_nextCodeIdField.getValue();
        m_nextCodeIdSource = m_nextCodeIdField.getValueSource();
        pingStateChanging();
      }
    });
    m_nextCodeIdField.setEnabled(isEnabled);

    INlsProject nls = null;
    if (getSharedBundle() != null) {
      nls = getSharedBundle().getNlsProject();
    }
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, nls, Texts.get("PropText"), labelColWidthPercent);
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
    m_nlsNameField.setEnabled(isEnabled);

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_CODE_TYPE);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });
    m_typeNameField.setEnabled(isEnabled);

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(parent, Texts.get("EntityTextField"), getSharedBundle(), labelColWidthPercent);
      m_entityField.setText(getTargetPackage());
      m_entityField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setTargetPackageInternal(m_entityField.getText());
          pingStateChanging();
        }
      });
      m_entityField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
      m_entityField.setEnabled(isEnabled);
    }

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"), createSuperTypeContentProvider(), labelColWidthPercent);

    final SignatureSubTypeProposalProvider genericProposalProvider = new SignatureSubTypeProposalProvider(getGenericTypeOfSuperClass(IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_TYPE_ID), ScoutUtility.getJavaProject(getSharedBundle()));
    m_genericTypeField = getFieldToolkit().createProposalField(parent, Texts.get("CodeTypeIdDatatype"), ProposalTextField.STYLE_DEFAULT, labelColWidthPercent);
    m_genericTypeField.setContentProvider(genericProposalProvider);
    m_genericTypeField.setLabelProvider(genericProposalProvider.getLabelProvider());

    final SignatureSubTypeProposalProvider genericCodeIdProposalProvider = new SignatureSubTypeProposalProvider(getGenericTypeOfSuperClass(IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID), ScoutUtility.getJavaProject(getSharedBundle()));
    m_genericCodeIdField = getFieldToolkit().createProposalField(parent, Texts.get("DatatypeOfNestedCodeIds"), ProposalTextField.STYLE_DEFAULT, labelColWidthPercent);
    m_genericCodeIdField.setContentProvider(genericCodeIdProposalProvider);
    m_genericCodeIdField.setLabelProvider(genericCodeIdProposalProvider.getLabelProvider());

    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_superType = (IType) event.proposal;
          m_superTypeParameters = TypeUtility.getTypeParameters(getSuperType());
          handleGenericFieldEnableState();
          genericProposalProvider.setBaseType(getGenericTypeOfSuperClass(IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_TYPE_ID));
          genericCodeIdProposalProvider.setBaseType(getGenericTypeOfSuperClass(IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID));
        }
        finally {
          setStateChanging(false);
        }
      }
    });
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.setEnabled(isEnabled);

    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_genericSignature = (String) event.proposal;
          if (m_genericSignature != null) {
            if (m_nextCodeId == null) {
              setNextCodeId(CodeIdExtensionPoint.getNextCodeId(getSharedBundle(), getGenericSignature()));
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
    m_nextCodeIdField.setGenericTypeField(m_genericTypeField);

    m_genericCodeIdField.acceptProposal(getGenericCodeIdSignature());
    m_genericCodeIdField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_genericCodeIdSignature = (String) event.proposal;
        }
        finally {
          setStateChanging(false);
        }
      }
    });
    m_genericCodeIdField.setEnabled(isEnabled);
    m_genericTypeField.setEnabled(isEnabled);

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nextCodeIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_genericCodeIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  protected AbstractJavaElementContentProvider createSuperTypeContentProvider() {
    return new JavaElementAbstractTypeContentProvider(iCodeType, ScoutUtility.getJavaProject(getSharedBundle()), (ITypeFilter) null, m_defaultCodeType);
  }

  /**
   * Enables/Disables and probably clears the generic type fields depending on which generics that are available on the
   * selected super type class.
   */
  protected void handleGenericFieldEnableState() {
    String codeTypeIdSig = null;
    String codeIdSig = null;

    if (TypeUtility.exists(getSuperType())) {
      try {
        ITypeHierarchy superHierarchy = TypeUtility.getSupertypeHierarchy(getSuperType());
        LinkedHashMap<String, ITypeGenericMapping> collector = new LinkedHashMap<String, ITypeGenericMapping>();
        SignatureUtility.resolveGenericParametersInSuperHierarchy(getSuperType(), superHierarchy, collector);
        ITypeGenericMapping iTypeGenericMapping = collector.get(getSuperType().getFullyQualifiedName());
        codeTypeIdSig = iTypeGenericMapping.getParameterSignature(IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_TYPE_ID);
        codeIdSig = iTypeGenericMapping.getParameterSignature(IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID);
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("Unable to calculate the visible generic signature fields for code types.", e);
      }
    }

    m_genericTypeField.setEnabled(codeTypeIdSig != null && getSharedBundle() != null);
    if (!m_genericTypeField.isEnabled()) {
      m_genericTypeField.acceptProposal(null);
    }
    m_genericCodeIdField.setEnabled(codeIdSig != null && getSharedBundle() != null);
    if (!m_genericCodeIdField.isEnabled()) {
      m_genericCodeIdField.acceptProposal(null);
    }
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    CodeTypeNewOperation op = new CodeTypeNewOperation(getTypeName(), getSharedBundle().getPackageName(getTargetPackage()), ScoutUtility.getJavaProject(getSharedBundle()));
    // write back members
    op.setNlsEntry(getNlsName());

    String sig = null;
    if (getGenericSignature() != null) {
      StringBuilder fqn = new StringBuilder(getSuperType().getFullyQualifiedName());
      if (m_superTypeParameters.size() > 0) {
        fqn.append(Signature.C_GENERIC_START);
        fqn.append(Signature.toString(getGenericSignature()));
        if (m_superTypeParameters.size() > 1) {
          fqn.append(", ");
          fqn.append(Signature.toString(getGenericCodeIdSignature()));
          if (m_superTypeParameters.size() > 2) {
            fqn.append(", ");
            fqn.append(IRuntimeClasses.ICode);
            fqn.append(Signature.C_GENERIC_START);
            fqn.append(Signature.toString(getGenericCodeIdSignature()));
            fqn.append(Signature.C_GENERIC_END);
          }
        }
        fqn.append(Signature.C_GENERIC_END);
        sig = SignatureCache.createTypeSignature(fqn.toString());
      }
      else {
        sig = SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName());
      }
    }
    else {
      sig = SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName());
    }
    op.setSuperTypeSignature(sig);
    op.setNextCodeId(getNextCodeIdSource());
    op.validate();
    op.run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusWorkspace());
    multiStatus.add(getStatusNextCodeIdField());
    multiStatus.add(getStatusNameField());
    multiStatus.add(getStatusSuperType());
    multiStatus.add(getStatusTargetPackge());

    if (m_genericTypeField != null && !m_genericTypeField.isDisposed() && m_genericTypeField.isEnabled() && m_genericTypeField.isVisible()) {
      multiStatus.add(getStatusGenericType());
      multiStatus.add(getStatusGenericTypeToSuperClass(getGenericSignature(), IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_TYPE_ID));
    }

    if (m_genericCodeIdField != null && !m_genericCodeIdField.isDisposed() && m_genericCodeIdField.isEnabled() && m_genericCodeIdField.isVisible()) {
      multiStatus.add(getStatusGenericCodeIdType());
      multiStatus.add(getStatusGenericTypeToSuperClass(getGenericCodeIdSignature(), IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID));
    }
  }

  protected IStatus getStatusWorkspace() {
    if (getSharedBundle() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoNewXWithoutScoutBundle", Texts.get("CodeType")));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
  }

  protected IStatus getStatusTargetPackge() {
    return ScoutUtility.validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusNextCodeIdField() {
    if (isControlCreated() && m_nextCodeIdField.getEnabled()) {
      return m_nextCodeIdField.getStatus();
    }
    else {
      return Status.OK_STATUS;
    }
  }

  protected IStatus getStatusNameField() {
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(getTypeName(), SdkProperties.SUFFIX_CODE_TYPE);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    IStatus existingStatus = ScoutUtility.validateTypeNotExisting(getSharedBundle(), getTargetPackage(), getTypeName());
    if (!existingStatus.isOK()) {
      return existingStatus;
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

  protected IType getGenericTypeOfSuperClass(String typeArgName) {
    if (TypeUtility.exists(getSuperType())) {
      try {
        ITypeHierarchy superHierarchy = TypeUtility.getSupertypeHierarchy(getSuperType());
        String typeParamSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(getSuperType(), superHierarchy, IRuntimeClasses.ICodeType, typeArgName);
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

  protected IStatus getStatusGenericTypeToSuperClass(String genericSignature, String superClassTypeArgName) {
    if (genericSignature != null) {
      IType superType = getGenericTypeOfSuperClass(superClassTypeArgName);
      if (TypeUtility.exists(superType)) {
        IType generic = TypeUtility.getTypeBySignature(genericSignature);
        if (TypeUtility.exists(generic) && !TypeUtility.getSupertypeHierarchy(generic).contains(superType)) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeDoesNotMatchSuperClass"));
        }
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusGenericCodeIdType() {
    if (TypeUtility.isGenericType(getSuperType())) {
      if (getGenericCodeIdSignature() == null) {
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

  public void setGenericCodeIdSignature(String genericCodeIdSignature) {
    try {
      setStateChanging(true);
      m_genericCodeIdSignature = genericCodeIdSignature;
      if (isControlCreated()) {
        m_genericCodeIdField.acceptProposal(genericCodeIdSignature);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getGenericCodeIdSignature() {
    return m_genericCodeIdSignature;
  }

  public String getTargetPackage() {
    return m_packageName;
  }

  public void setTargetPackage(String targetPackage) {
    try {
      setStateChanging(true);
      setTargetPackageInternal(targetPackage);
      if (isControlCreated() && m_entityField != null) {
        m_entityField.setText(targetPackage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTargetPackageInternal(String targetPackage) {
    m_packageName = targetPackage;
  }
}
