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
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.CodeTypeNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.fields.code.CodeIdField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link CodeTypeNewWizardPage}</h3> ...
 */
public class CodeTypeNewWizardPage extends AbstractWorkspaceWizardPage {

  private final IType iCodeType = TypeUtility.getType(RuntimeClasses.ICodeType);

  private String m_nextCodeId;
  private String m_nextCodeIdSource;
  private INlsEntry m_nlsName;
  private String m_typeName;
  private String m_packageName;
  private IType m_superType;
  private IType m_defaultCodeType;
  private String m_genericSignature;

  private CodeIdField m_nextCodeIdField;
  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private EntityTextField m_entityField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_genericTypeField;

  // process members

  private final IScoutBundle m_sharedBundle;

  public CodeTypeNewWizardPage(IScoutBundle sharedBundle) {
    super(CodeTypeNewWizardPage.class.getName());
    m_sharedBundle = sharedBundle;
    setTargetPackage(DefaultTargetPackage.get(sharedBundle, IDefaultTargetPackage.SHARED_SERVICES_CODE));
    setTitle(Texts.get("NewCodeType"));
    setDescription(Texts.get("CreateANewCodeType"));
    m_defaultCodeType = RuntimeClasses.getSuperType(RuntimeClasses.ICodeType, ScoutUtility.getJavaProject(m_sharedBundle));
    m_superType = m_defaultCodeType;
    m_genericSignature = SignatureCache.createTypeSignature(Long.class.getName());
  }

  @Override
  public void postActivate() {
    m_nlsNameField.setFocus();
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;
    m_nextCodeIdField = new CodeIdField(parent, getSharedBundle(), labelColWidthPercent);
    m_nextCodeIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_nextCodeId = m_nextCodeIdField.getValue();
        m_nextCodeIdSource = m_nextCodeIdField.getValueSource();
        pingStateChanging();
      }
    });

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, getSharedBundle().getNlsProject(), Texts.get("Name"), labelColWidthPercent);
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

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(parent, Texts.get("EntityTextField"), m_sharedBundle, labelColWidthPercent);
      m_entityField.setText(getTargetPackage());
      m_entityField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setTargetPackageInternal((String) m_entityField.getText());
          pingStateChanging();
        }
      });
      m_entityField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    }

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iCodeType, ScoutUtility.getJavaProject(getSharedBundle()), m_defaultCodeType), labelColWidthPercent);
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

    m_genericTypeField = getFieldToolkit().createSignatureProposalField(parent, Texts.get("GenericType"), getSharedBundle(),
        SignatureProposalProvider.DEFAULT_MOST_USED, labelColWidthPercent);
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(getSuperType() != null && TypeUtility.isGenericType(getSuperType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_genericSignature = (String) event.proposal;
        }
        finally {
          setStateChanging(false);
        }
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
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    CodeTypeNewOperation op = new CodeTypeNewOperation();
    // write back members
    op.setSharedBundle(getSharedBundle());
    op.setTypeName(getTypeName());
    op.setPackageName(getSharedBundle().getPackageName(getTargetPackage()));
    op.setNlsEntry(getNlsName());

    IType superTypeProp = getSuperType();
    if (superTypeProp != null) {
      String sig = null;
      if (getGenericSignature() != null) {
        sig = SignatureCache.createTypeSignature(superTypeProp.getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature()) + ">");
      }
      else {
        sig = SignatureCache.createTypeSignature(superTypeProp.getFullyQualifiedName());
      }
      op.setSuperTypeSignature(sig);
    }
    if (getGenericSignature() != null) {
      op.setGenericTypeSignature(getGenericSignature());
    }
    op.setNextCodeId(getNextCodeIdSource());
    op.setFormatSource(true);
    op.validate();
    op.run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNextCodeIdField());
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
      multiStatus.add(getStatusGenericType());
      multiStatus.add(getStatusTargetPackge());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
  }

  protected IStatus getStatusTargetPackge() {
    return ScoutUtility.validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusNextCodeIdField() throws JavaModelException {
    if (isControlCreated()) {
      return m_nextCodeIdField.getStatus();
    }
    else {
      return Status.OK_STATUS;
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_CODE_TYPE)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_className"));
    }
    // check not allowed names
    if (TypeUtility.existsType(getSharedBundle().getPackageName(getTargetPackage()) + "." + getTypeName())) {
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
