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
package org.eclipse.scout.sdk.ui.wizard.services;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureSubTypeProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ServiceNewWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 29.07.2009
 */
public class ServiceNewWizardPage extends AbstractWorkspaceWizardPage {

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
  private final IType m_definitionType;
  private final String m_typeNameSuffix;
  private final IScoutBundle m_bundle;

  public ServiceNewWizardPage(String title, String message, IType definitionType, String typeNameSuffix, IScoutBundle b, String defaultPackage) {
    super(ServiceNewWizardPage.class.getName());
    m_typeNameSuffix = typeNameSuffix;
    m_definitionType = definitionType;
    m_bundle = b;
    setTargetPackage(defaultPackage);
    setTitle(title);
    setDescription(message);
  }

  private String getTypeParamName() {
    if (TypeUtility.isGenericType(m_definitionType)) {
      try {
        return m_definitionType.getTypeParameters()[0].getElementName();
      }
      catch (JavaModelException e1) {
        ScoutSdkUi.logError(e1);
      }
    }
    return null;
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;
    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(getTypeNameSuffix());
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    final boolean isGenericDefType = TypeUtility.isGenericType(m_definitionType);

    ITypeFilter filter = null;
    String typeParamName = getTypeParamName();
    if (typeParamName != null) {
      filter = TypeFilters.getTypeParamSubTypeFilter(getGenericTypeSignature(), m_definitionType.getFullyQualifiedName(), typeParamName);
    }

    final JavaElementAbstractTypeContentProvider contentProvider = new JavaElementAbstractTypeContentProvider(m_definitionType, m_bundle.getJavaProject(), filter, getSuperType());
    SignatureSubTypeProposalProvider tmpProposalProvider = null;
    if (isGenericDefType) {
      tmpProposalProvider = new SignatureSubTypeProposalProvider(getGenericTypeOfSuperClass(), m_bundle.getJavaProject());
    }
    final SignatureSubTypeProposalProvider keyTypeProposalProvider = tmpProposalProvider;

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"), contentProvider, labelColWidthPercent);
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((IType) event.proposal);
        if (isGenericDefType) {
          IType genericTypeOfSuperClass = getGenericTypeOfSuperClass();
          if (getGenericTypeSignature() == null && TypeUtility.exists(genericTypeOfSuperClass)) {
            m_genericTypeField.acceptProposal(SignatureCache.createTypeSignature(genericTypeOfSuperClass.getFullyQualifiedName()));
          }
          if (keyTypeProposalProvider != null) {
            keyTypeProposalProvider.setBaseType(genericTypeOfSuperClass);
          }
        }
        pingStateChanging();
      }
    });

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(parent, Texts.get("EntityTextField"), m_bundle, labelColWidthPercent);
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

    if (isGenericDefType && keyTypeProposalProvider != null) {
      m_genericTypeField = getFieldToolkit().createProposalField(parent, Texts.get("KeyType"), ProposalTextField.STYLE_DEFAULT, labelColWidthPercent);
      m_genericTypeField.setContentProvider(keyTypeProposalProvider);
      m_genericTypeField.setLabelProvider(keyTypeProposalProvider.getLabelProvider());
      m_genericTypeField.acceptProposal(getGenericTypeSignature());
      m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
        @Override
        public void proposalAccepted(ContentProposalEvent event) {
          setGenericTypeSignatureInternal((String) event.proposal);
          contentProvider.invalidateCache();
          pingStateChanging();
        }
      });
      m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    }

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  protected IType getGenericTypeOfSuperClass() {
    if (TypeUtility.exists(getSuperType())) {
      List<ITypeParameter> typeParameters = TypeUtility.getTypeParameters(m_definitionType);
      try {
        ITypeHierarchy superHierarchy = TypeUtility.getSupertypeHierarchy(getSuperType());
        String typeParamSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(getSuperType(), superHierarchy, m_definitionType.getFullyQualifiedName(), typeParameters.get(0).getElementName());
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
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusGenericType() {
    if (m_genericTypeField != null) {
      if (getGenericTypeSignature() == null) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeCanNotBeNull"));
      }
      if (TypeUtility.exists(getSuperType())) {
        try {
          ITypeHierarchy superHierarchy = TypeUtility.getSupertypeHierarchy(getSuperType());
          String typeParamSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(getSuperType(), superHierarchy, m_definitionType.getFullyQualifiedName(), getTypeParamName());
          if (typeParamSig != null) {
            IType generic = TypeUtility.getTypeBySignature(getGenericTypeSignature());
            IType superType = TypeUtility.getTypeBySignature(typeParamSig);
            if (TypeUtility.exists(generic) && !TypeUtility.getSupertypeHierarchy(generic).contains(superType)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeDoesNotMatchSuperClass"));
            }
          }
        }
        catch (CoreException e) {
          ScoutSdkUi.logError(e);
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Unknown Error. See Error Log View for Details");
        }
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusTargetPackge() {
    return ScoutUtility.validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(getTypeName(), getTypeNameSuffix());
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    IStatus existingStatus = ScoutUtility.validateTypeNotExisting(m_bundle, getTargetPackage(), getTypeName());
    if (!existingStatus.isOK()) {
      return existingStatus;
    }
    return javaFieldNameStatus;
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  public String getTypeNameSuffix() {
    return m_typeNameSuffix;
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

  protected void setTypeNameInternal(String typeName) {
    setPropertyString(PROP_TYPE_NAME, typeName);
  }

  public IType getSuperType() {
    return (IType) getProperty(PROP_SUPER_TYPE);
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      setSuperTypeInternal(superType);
      if (isControlCreated()) {
        m_superTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setSuperTypeInternal(IType superType) {
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
      if (isControlCreated() && m_genericTypeField != null) {
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
