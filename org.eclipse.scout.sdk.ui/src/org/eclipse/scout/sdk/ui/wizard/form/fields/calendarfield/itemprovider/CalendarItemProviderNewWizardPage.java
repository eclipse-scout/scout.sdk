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
package org.eclipse.scout.sdk.ui.wizard.form.fields.calendarfield.itemprovider;

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
import org.eclipse.scout.sdk.operation.form.field.calendar.CalendarItemProviderNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>CalendarItemProviderNewWizardPage</h3> ...
 */
public class CalendarItemProviderNewWizardPage extends AbstractWorkspaceWizardPage {

  final IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);
  final IType iCalendarItemProvider = ScoutSdk.getType(RuntimeClasses.ICalendarItemProvider);
  final IType abstractCalendarItemProvider = ScoutSdk.getType(RuntimeClasses.AbstractCalendarItemProvider);

  private String m_typeName;
  private ITypeProposal m_superType;
  private SiblingProposal m_sibling;

  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_siblingField;

  // process members
  private final IType m_declaringType;
  private IType m_createdField;

  public CalendarItemProviderNewWizardPage(IType declaringType) {
    super(Texts.get("NewCalendarItemProvider"));
    setTitle(Texts.get("NewCalendarItemProvider"));
    setDefaultMessage(Texts.get("CreateANewCalendarItemProvider"));
    m_declaringType = declaringType;

    ITypeProposal superType = getSuperType();
    if (superType == null) {
      superType = ScoutProposalUtility.getScoutTypeProposalsFor(abstractCalendarItemProvider)[0];
    }
    m_superType = superType;
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  protected void createContent(Composite parent) {

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_CALENDAR_ITEM_PROVIDER);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    IPrimaryTypeTypeHierarchy formFieldHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iFormField);
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getAbstractOnClasspath(m_declaringType.getJavaProject()),
        TypeFilters.getSubtypeFilter(iCalendarItemProvider, formFieldHierarchy)
        );

    ITypeProposal[] superTypeProposals = ScoutProposalUtility.getScoutTypeProposalsFor(formFieldHierarchy.getAllClasses(filter, TypeComparators.getTypeNameComparator()));
    m_superTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(superTypeProposals), Texts.get("SuperType"));
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (ITypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    SiblingProposal[] availableSiblings = ScoutProposalUtility.getSiblingProposals(SdkTypeUtility.getFormFields(m_declaringType));
    m_siblingField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(availableSiblings), Texts.get("Sibling"));
    m_siblingField.acceptProposal(m_sibling);
    m_siblingField.setEnabled(availableSiblings != null && availableSiblings.length > 0);
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_sibling = (SiblingProposal) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    CalendarItemProviderNewOperation operation = new CalendarItemProviderNewOperation(m_declaringType);
    operation.setFormatSource(true);
    // write back members
    operation.setTypeName(getTypeName());
    ITypeProposal superTypeProp = getSuperType();
    if (superTypeProp != null) {
      operation.setSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true));
    }

    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = SdkTypeUtility.createStructuredCalendar(m_declaringType);
      operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER));
    }
    else {
      operation.setSibling(getSibling().getScoutType());
    }
    operation.run(monitor, workingCopyManager);
    m_createdField = operation.getCreatedField();
    return true;
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
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_CALENDAR_ITEM_PROVIDER)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.exists(m_declaringType.getType(getTypeName()))) {
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
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  /**
   * @return the createdField
   */
  public IType getCreatedField() {
    return m_createdField;
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

  public ITypeProposal getSuperType() {
    return m_superType;
  }

  public void setSuperType(ITypeProposal superType) {
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
