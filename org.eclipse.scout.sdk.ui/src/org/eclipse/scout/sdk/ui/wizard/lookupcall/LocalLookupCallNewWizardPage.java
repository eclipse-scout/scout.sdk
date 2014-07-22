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
package org.eclipse.scout.sdk.ui.wizard.lookupcall;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.lookupcall.LocalLookupCallNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureSubTypeProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
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
 * <h3>{@link LocalLookupCallNewWizardPage}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 */
public class LocalLookupCallNewWizardPage extends AbstractWorkspaceWizardPage {

  private final IType localLookupCall = TypeUtility.getType(IRuntimeClasses.LocalLookupCall);

  public static final String PROP_TYPE_NAME = "typeName";
  public static final String PROP_SUPER_TYPE = "superType";
  public static final String PROP_TARGET_PACKAGE = "targetPackage";
  public static final String PROP_GENERIC_TYPE = "genericType";

  // ui fields
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private EntityTextField m_entityField;
  private ProposalTextField m_genericTypeField;

  // process members
  private IScoutBundle m_clientBundle;

  public LocalLookupCallNewWizardPage(IScoutBundle client) {
    super(LocalLookupCallNewWizardPage.class.getName());
    m_clientBundle = client;
    setTitle(Texts.get("NewLocalLookupCallNoPopup"));
    setDescription(Texts.get("CreateANewLocalLookupCall"));
    setLookupCallSuperTypeInternal(localLookupCall);
    setTargetPackage(DefaultTargetPackage.get(client, IDefaultTargetPackage.CLIENT_SERVICES_LOOKUP));
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;
    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_LOOKUP_CALL);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    final SignatureSubTypeProposalProvider proposalProvider = new SignatureSubTypeProposalProvider(getGenericTypeOfSuperClass(), m_clientBundle.getJavaProject());

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("LookupCallSuperType"),
        new AbstractJavaElementContentProvider() {
          @Override
          protected Object[][] computeProposals() {
            Set<IType> superTypes = TypeUtility.getClassesOnClasspath(localLookupCall, ScoutUtility.getJavaProject(getClientBundle()), TypeFilters.getNotInTypes(localLookupCall));
            return new Object[][]{new IType[]{localLookupCall}, superTypes.toArray(new IType[superTypes.size()])};
          }
        }, labelColWidthPercent);
    m_superTypeField.acceptProposal(getLookupCallSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setLookupCallSuperTypeInternal((IType) event.proposal);
        IType genericTypeOfSuperClass = getGenericTypeOfSuperClass();
        if (getGenericTypeSignature() == null && TypeUtility.exists(genericTypeOfSuperClass)) {
          m_genericTypeField.acceptProposal(SignatureCache.createTypeSignature(genericTypeOfSuperClass.getFullyQualifiedName()));
        }
        proposalProvider.setBaseType(genericTypeOfSuperClass);
        pingStateChanging();
      }
    });

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(parent, Texts.get("EntityTextField"), m_clientBundle, labelColWidthPercent);
      m_entityField.setText(getTargetPackage());
      m_entityField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setTargetPackageInternal(m_entityField.getText());
          pingStateChanging();
        }
      });
      m_entityField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    }

    m_genericTypeField = getFieldToolkit().createProposalField(parent, Texts.get("KeyType"), ProposalTextField.STYLE_DEFAULT, labelColWidthPercent);
    m_genericTypeField.setContentProvider(proposalProvider);
    m_genericTypeField.setLabelProvider(proposalProvider.getLabelProvider());
    m_genericTypeField.acceptProposal(getGenericTypeSignature());
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setGenericTypeSignatureInternal((String) event.proposal);
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public LocalLookupCallNewWizard getWizard() {
    return (LocalLookupCallNewWizard) super.getWizard();
  }

  protected IType getGenericTypeOfSuperClass() {
    if (TypeUtility.exists(getLookupCallSuperType())) {
      try {
        ITypeHierarchy superHierarchy = TypeUtility.getSupertypeHierarchy(getLookupCallSuperType());
        String typeParamSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(getLookupCallSuperType(), superHierarchy, IRuntimeClasses.ILookupCall, IRuntimeClasses.TYPE_PARAM_LOOKUPCALL__KEY_TYPE);
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

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
      multiStatus.add(getStatusTargetPackge());
      multiStatus.add(getStatusGenericType());
      multiStatus.add(getStatusGenericTypeToSuperClass());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusGenericTypeToSuperClass() {
    if (getGenericTypeSignature() != null) {
      IType superType = getGenericTypeOfSuperClass();
      if (TypeUtility.exists(superType)) {
        IType generic = TypeUtility.getTypeBySignature(getGenericTypeSignature());
        if (TypeUtility.exists(generic) && !TypeUtility.getSupertypeHierarchy(generic).contains(superType)) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeDoesNotMatchSuperClass"));
        }
      }
    }
    return Status.OK_STATUS;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    LocalLookupCallNewOperation op = new LocalLookupCallNewOperation(getTypeName(), getClientBundle().getPackageName(getTargetPackage()), ScoutUtility.getJavaProject(getClientBundle()));
    op.setFormatSource(true);
    IType superTypeProp = getLookupCallSuperType();
    if (superTypeProp != null) {
      StringBuilder superType = new StringBuilder(superTypeProp.getFullyQualifiedName());
      superType.append('<');
      superType.append(Signature.toString(getGenericTypeSignature()));
      superType.append('>');
      op.setSuperTypeSignature(SignatureCache.createTypeSignature(superType.toString()));
    }
    op.validate();
    op.run(monitor, manager);
    return true;
  }

  protected IStatus getStatusTargetPackge() {
    return ScoutUtility.validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusGenericType() {
    if (getGenericTypeSignature() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(getTypeName(), SdkProperties.SUFFIX_LOOKUP_CALL);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    IStatus existingStatus = ScoutUtility.validateTypeNotExisting(getClientBundle(), getTargetPackage(), getTypeName());
    if (!existingStatus.isOK()) {
      return existingStatus;
    }
    return javaFieldNameStatus;
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getLookupCallSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public String getTypeName() {
    return getPropertyString(PROP_TYPE_NAME);
  }

  public void setTypeName(String typeName) {
    try {
      setStateChanging(true);
      setTypeNameInternal(typeName);
      if (isControlCreated()) {
        m_typeNameField.setText(typeName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTypeNameInternal(String typeName) {
    setPropertyString(PROP_TYPE_NAME, typeName);
  }

  public IType getLookupCallSuperType() {
    return (IType) getProperty(PROP_SUPER_TYPE);
  }

  public void setLookupCallSuperType(IType superType) {
    try {
      setStateChanging(true);
      setLookupCallSuperTypeInternal(superType);
      if (isControlCreated()) {
        m_superTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setLookupCallSuperTypeInternal(IType superType) {
    setProperty(PROP_SUPER_TYPE, superType);
  }

  public String getTargetPackage() {
    return (String) getProperty(PROP_TARGET_PACKAGE);
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
    setProperty(PROP_TARGET_PACKAGE, targetPackage);
  }

  public String getGenericTypeSignature() {
    return (String) getProperty(PROP_GENERIC_TYPE);
  }

  public void setGenericTypeSignature(String genericType) {
    try {
      setStateChanging(true);
      setGenericTypeSignatureInternal(genericType);
      if (isControlCreated()) {
        m_genericTypeField.acceptProposal(genericType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setGenericTypeSignatureInternal(String genericType) {
    setProperty(PROP_GENERIC_TYPE, genericType);
  }
}
