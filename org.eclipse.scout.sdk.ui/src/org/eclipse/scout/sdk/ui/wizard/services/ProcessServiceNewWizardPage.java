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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
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

  /** {@link String} **/
  public static final String PROP_TYPE_NAME = "typeName";
  /** {@link ITypeProposal} **/
  public static final String PROP_SUPER_TYPE = "superType";
  /** {@link ITypeProposal} **/
  public static final String PROP_FORM_DATA_TYPE = "formDataType";

  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);
  final IType abstractFormData = ScoutSdk.getType(RuntimeClasses.AbstractFormData);

  // ui fields
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_formDataTypeField;

  // process members
  private IScoutBundle m_serverBundle;

  public ProcessServiceNewWizardPage() {
    super(ProcessServiceNewWizardPage.class.getName());
    setTitle(Texts.get("NewProcessService"));
    setDefaultMessage(Texts.get("CreateANewProcessService"));
  }

  @Override
  protected void createContent(Composite parent) {
    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_PROCESS_SERVICE);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createProposalField(parent, null, Texts.get("SuperType"));
    if (getServerBundle() != null) {
      ICachedTypeHierarchy serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
      ITypeFilter filter = TypeFilters.getAbstractOnClasspath(getServerBundle().getJavaProject());
      IType[] abstractServices = serviceHierarchy.getAllSubtypes(iService, filter, TypeComparators.getTypeNameComparator());
      ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(abstractServices);
      m_superTypeField.setContentProposalProvider(new DefaultProposalProvider(proposals));
    }
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((ITypeProposal) event.proposal);
        pingStateChanging();
      }
    });

    m_formDataTypeField = getFieldToolkit().createProposalField(parent, null, Texts.get("FormData"));
    if (getServerBundle() != null) {
      ICachedTypeHierarchy formDataHierarchy = ScoutSdk.getPrimaryTypeHierarchy(abstractFormData);
      ITypeFilter filter = TypeFilters.getTypesOnClasspath(getServerBundle().getJavaProject());
      IType[] abstractFormDatas = formDataHierarchy.getAllSubtypes(abstractFormData, filter, TypeComparators.getTypeNameComparator());
      ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(abstractFormDatas);
      m_formDataTypeField.setContentProposalProvider(new DefaultProposalProvider(proposals));
    }
    m_formDataTypeField.acceptProposal(getFormDataType());
    m_formDataTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setFormDataTypeInternal((ITypeProposal) event.proposal);
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    GridData formDataTypeFieldData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    m_formDataTypeField.setLayoutData(formDataTypeFieldData);
  }

  void fillProcessServiceNewOperation(ProcessServiceNewOperation op) {
    op.setServiceImplementationName(getTypeName());
    op.setServiceInterfaceName("I" + getTypeName());
    if (getFormDataType() != null) {
      op.setFormData(getFormDataType().getType());
    }
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_PROCESS_SERVICE)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    if (getTypeName().matches(Regex.REGEX_WELLFORMD_JAVAFIELD)) {
      return Status.OK_STATUS;
    }
    else if (getTypeName().matches(Regex.REGEX_JAVAFIELD)) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_invalidFieldX", getTypeName()));
    }
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getServerBundle() {
    return m_serverBundle;
  }

  public void setServerBundle(IScoutBundle serverBundle) {
    m_serverBundle = serverBundle;
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

  public ITypeProposal getSuperType() {
    return (ITypeProposal) getProperty(PROP_SUPER_TYPE);
  }

  public void setSuperType(ITypeProposal superType) {
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

  protected void setSuperTypeInternal(ITypeProposal superType) {
    setProperty(PROP_SUPER_TYPE, superType);
  }

  public void setFormDataType(ITypeProposal formDataType) {
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

  protected void setFormDataTypeInternal(ITypeProposal formDatatype) {
    setProperty(PROP_FORM_DATA_TYPE, formDatatype);
  }

  public ITypeProposal getFormDataType() {
    return (ITypeProposal) getProperty(PROP_FORM_DATA_TYPE);
  }

}
