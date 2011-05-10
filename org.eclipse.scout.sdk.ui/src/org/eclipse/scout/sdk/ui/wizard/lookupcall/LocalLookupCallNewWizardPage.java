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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.lookupcall.LocalLookupCallNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
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
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link LocalLookupCallNewWizardPage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 */
public class LocalLookupCallNewWizardPage extends AbstractWorkspaceWizardPage {

  private static final IType localLookupCall = ScoutSdk.getType(RuntimeClasses.LocalLookupCall);

  /** {@link String} **/
  public static final String PROP_TYPE_NAME = "typeName";
  /** {@link ITypeProposal} **/
  public static final String PROP_SUPER_TYPE = "superType";

  // ui fields
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;

  // process members
  private IScoutBundle m_clientBundle;

  public LocalLookupCallNewWizardPage(IScoutBundle client) {
    super(LocalLookupCallNewWizardPage.class.getName());
    m_clientBundle = client;
    setTitle("New Local Lookup Call");
    setDefaultMessage("Create a new local lookup call.");
    setLookupCallSuperTypeInternal(ScoutProposalUtility.getScoutTypeProposalsFor(localLookupCall)[0]);
  }

  @Override
  protected void createContent(Composite parent) {

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, "Type Name");
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_LOOKUP_CALL);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createProposalField(parent, null, "Lookup Call Super Type");
    ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(localLookupCall);
    ICachedTypeHierarchy lookupServiceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(localLookupCall);
    IType[] abstractLookupServices = lookupServiceHierarchy.getAllClasses(TypeFilters.getTypesOnClasspath(getClientBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
    ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(abstractLookupServices);
    m_superTypeField.setContentProposalProvider(new DefaultProposalProvider(shotList, proposals));
    m_superTypeField.acceptProposal(getLookupCallSuperType());

    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        setLookupCallSuperTypeInternal((ITypeProposal) event.proposal);
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public LocalLookupCallNewWizard getWizard() {
    return (LocalLookupCallNewWizard) super.getWizard();
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

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    LocalLookupCallNewOperation op = new LocalLookupCallNewOperation();
    op.setBundle(getClientBundle());
    op.setLookupCallName(getTypeName());
    op.setFormatSource(true);
    ITypeProposal superTypeProp = getLookupCallSuperType();
    if (superTypeProp != null) {
      op.setLookupCallSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true));
    }
    op.validate();
    op.run(monitor, manager);
    return true;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_LOOKUP_CALL)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (ScoutSdk.existsType(getClientBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_LOOKUP) + "." + getTypeName())) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
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
    if (getLookupCallSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "The super type can not be null!");
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

  public ITypeProposal getLookupCallSuperType() {
    return (ITypeProposal) getProperty(PROP_SUPER_TYPE);
  }

  public void setLookupCallSuperType(ITypeProposal superType) {
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

  private void setLookupCallSuperTypeInternal(ITypeProposal superType) {
    setProperty(PROP_SUPER_TYPE, superType);
  }
}
