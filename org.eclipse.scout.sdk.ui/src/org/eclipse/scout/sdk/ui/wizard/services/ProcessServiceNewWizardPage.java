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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ProcessServiceNewWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.07.2009
 */
public class ProcessServiceNewWizardPage extends AbstractWorkspaceWizardPage {

  private static final String PROP_TYPE_NAME = "typeName";
  private static final String PROP_SUPER_TYPE = "superType";
  private static final String PROP_FORM_DATA_TYPE = "formDataType";
  private static final String PROP_TARGET_PACKAGE = "targetPackage";

  private final IType iService = TypeUtility.getType(RuntimeClasses.IService);
  private final IType abstractFormData = TypeUtility.getType(RuntimeClasses.AbstractFormData);

  // ui fields
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_formDataTypeField;
  private EntityTextField m_entityField;

  // process members
  private IScoutBundle m_serverBundle;

  public ProcessServiceNewWizardPage(IScoutBundle serverBundle) {
    super(ProcessServiceNewWizardPage.class.getName());
    m_serverBundle = serverBundle;
    setTitle(Texts.get("NewProcessService"));
    setDescription(Texts.get("CreateANewProcessService"));
    setTargetPackage(DefaultTargetPackage.get(serverBundle, IDefaultTargetPackage.SERVER_SERVICES));
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_SERVICE);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    IType abstractService = RuntimeClasses.getSuperType(RuntimeClasses.IService, getServerBundle().getJavaProject());
    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iService, getServerBundle().getJavaProject(), abstractService), labelColWidthPercent);
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    m_formDataTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("FormData"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        ICachedTypeHierarchy formDataHierarchy = TypeUtility.getPrimaryTypeHierarchy(abstractFormData);
        IType[] formDataTypes = formDataHierarchy.getAllSubtypes(abstractFormData, TypeFilters.getTypesOnClasspath(getServerBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
        return new Object[][]{formDataTypes};
      }
    }, labelColWidthPercent);
    m_formDataTypeField.acceptProposal(getFormDataType());
    m_formDataTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setFormDataTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(parent, Texts.get("EntityTextField"), getServerBundle(), labelColWidthPercent);
      m_entityField.setText(getTargetPackage(null));
      m_entityField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setTargetPackageInternal(m_entityField.getText());
          pingStateChanging();
        }
      });
      m_entityField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    }

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_formDataTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  void fillProcessServiceNewOperation(ProcessServiceNewOperation op) {
    op.setFormData(getFormDataType());
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
      multiStatus.add(getStatusTargetPackge());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    IStatus javaFieldNameStatus = ScoutUtility.getJavaNameStatus(getTypeName(), SdkProperties.SUFFIX_SERVICE);
    if (javaFieldNameStatus.isOK()) {
      if (TypeUtility.existsType(getServerBundle().getPackageName(getTargetPackage(IDefaultTargetPackage.SERVER_SERVICES)) + "." + getTypeName())) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }
    return javaFieldNameStatus;
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusTargetPackge() {
    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      return ScoutUtility.validatePackageName(getTargetPackage(null));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getServerBundle() {
    return m_serverBundle;
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

  public void setFormDataType(IType formDataType) {
    try {
      setStateChanging(true);
      setFormDataTypeInternal(formDataType);
      if (isControlCreated()) {
        m_formDataTypeField.acceptProposal(formDataType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setFormDataTypeInternal(IType formDatatype) {
    setProperty(PROP_FORM_DATA_TYPE, formDatatype);
  }

  public IType getFormDataType() {
    return (IType) getProperty(PROP_FORM_DATA_TYPE);
  }

  public String getTargetPackage(String packageId) {
    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      return (String) getProperty(PROP_TARGET_PACKAGE);
    }
    else {
      return DefaultTargetPackage.get(null, packageId);
    }
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
}
