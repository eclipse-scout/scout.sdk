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
package org.eclipse.scout.sdk.ui.wizard.menu;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.MenuNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>BooleanFieldNewWizardPage</h3> ...
 */
public class MenuNewWizardPage extends AbstractWorkspaceWizardPage {

  static final String PROP_SIBLING = "sibling";
  static final String PROP_FORM_TO_OPEN = "formToOpen";
  static final String PROP_FORM_HANDLER = "formHandler";

  protected final IType abstractMenuType = TypeUtility.getType(RuntimeClasses.AbstractMenu);
  protected final IType iMenuType = TypeUtility.getType(RuntimeClasses.IMenu);
  protected final IType iformType = TypeUtility.getType(RuntimeClasses.IForm);

  private INlsEntry m_nlsName;
  private String m_typeName;
  private IType m_superType;

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_siblingField;
  private ProposalTextField m_formToOpenField;
  private ProposalTextField m_formHandlerField;

  // process members
  private final IType m_declaringType;
  private IType m_createdMenu;

  public MenuNewWizardPage(IType declaringType) {
    super(MenuNewWizardPage.class.getName());
    setTitle(Texts.get("NewMenu"));
    setDescription(Texts.get("CreateANewMenu"));
    m_declaringType = declaringType;
    m_superType = abstractMenuType;
    setSiblingInternal(SiblingProposal.SIBLING_END);

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
          INlsEntry oldEntry = null;
          oldEntry = getNlsName();
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

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_MENU);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"), new IType[]{abstractMenuType},
        ScoutTypeUtility.getAbstractTypesOnClasspath(iMenuType, getDeclaringType().getJavaProject(), abstractMenuType));
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (IType) event.proposal;
        pingStateChanging();
      }
    });

    m_siblingField = getFieldToolkit().createSiblingProposalField(parent, getDeclaringType(), iMenuType);
    m_siblingField.acceptProposal(getSibling());
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSiblingInternal((SiblingProposal) event.proposal);
        pingStateChanging();
      }
    });

    Control formGroup = createFormGroup(parent);

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    formGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  protected Control createFormGroup(Composite parent) {
    Group groupBox = new Group(parent, SWT.SHADOW_ETCHED_IN);
    ITypeHierarchy cachedFormHierarchy = TypeUtility.getPrimaryTypeHierarchy(iformType);
    ITypeFilter formsFilter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getTypesOnClasspath(getDeclaringType().getJavaProject()),
        TypeFilters.getClassFilter());
    IType[] formCandidates = cachedFormHierarchy.getAllSubtypes(iformType, formsFilter, TypeComparators.getTypeNameComparator());

    m_formToOpenField = getFieldToolkit().createJavaElementProposalField(groupBox, Texts.get("FormToStart"), formCandidates);
    m_formToOpenField.acceptProposal(getFormToOpen());
    m_formToOpenField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          setFormToOpenInternal((IType) event.proposal);

          JavaElementContentProvider formHandlerProvider = null;
          IType formHandlerSelection = (IType) m_formHandlerField.getSelectedProposal();
          if (getFormToOpen() != null) {
            IType[] formHandlers = ScoutTypeUtility.getFormHandlers(getFormToOpen());
            if (formHandlers != null) {
              formHandlerProvider = new JavaElementContentProvider((ILabelProvider) m_formHandlerField.getLabelProvider(), formHandlers);
            }
            // assign null selection if the current selected form is not the declaring type of the form hanlder selection.
            if (formHandlerSelection != null && !getFormToOpen().equals(formHandlerSelection.getDeclaringType())) {
              formHandlerSelection = null;
            }
            m_formHandlerField.setEnabled(true);
          }
          else {
            m_formHandlerField.setEnabled(false);
          }
          // backup
          m_formHandlerField.setContentProvider(formHandlerProvider);
          m_formHandlerField.acceptProposal(formHandlerSelection);
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_formHandlerField = getFieldToolkit().createProposalField(groupBox, Texts.get("FormHandler"));
    m_formHandlerField.setLabelProvider(new JavaElementLabelProvider());
    m_formHandlerField.setEnabled(false);
    m_formHandlerField.acceptProposal(getFormToOpen());
    m_formHandlerField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setFormHandlerInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    //layout
    groupBox.setLayout(new GridLayout(1, true));

    m_formToOpenField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_formHandlerField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return groupBox;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    // create menu
    MenuNewOperation operation = new MenuNewOperation(getDeclaringType(), true);
    // write back members
    operation.setNlsEntry(getNlsName());
    operation.setTypeName(getTypeName());
    IType superType = getSuperType();
    if (superType != null) {
      String signature = Signature.createTypeSignature(superType.getFullyQualifiedName(), true);
      operation.setSuperTypeSignature(signature);
    }
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredType(m_declaringType);
      operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_MENU));
    }
    else {
      operation.setSibling(getSibling().getElement());
    }
    operation.setFormToOpen(getFormToOpen());
    operation.setFormHandler(getHandler());
    operation.run(monitor, manager);
    m_createdMenu = operation.getCreatedMenu();
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
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_MENU)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.hasInnerType(m_declaringType, getTypeName())) {
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

  public IType getDeclaringType() {
    return m_declaringType;
  }

  /**
   * @return the createdMenu
   */
  public IType getCreatedMenu() {
    return m_createdMenu;
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

  public SiblingProposal getSibling() {
    return (SiblingProposal) getProperty(PROP_SIBLING);
  }

  public void setSibling(SiblingProposal sibling) {
    try {
      setStateChanging(true);
      setSiblingInternal(sibling);
      if (isControlCreated()) {
        m_siblingField.acceptProposal(sibling);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setSiblingInternal(SiblingProposal sibling) {
    setProperty(PROP_SIBLING, sibling);
  }

  public IType getFormToOpen() {
    return (IType) getProperty(PROP_FORM_TO_OPEN);
  }

  public void setFormToOpen(IType formToOpen) {
    try {
      setStateChanging(true);
      setFormToOpenInternal(formToOpen);
      if (isControlCreated()) {
        m_formToOpenField.acceptProposal(formToOpen);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setFormToOpenInternal(IType formToOpen) {
    setProperty(PROP_FORM_TO_OPEN, formToOpen);
  }

  public IType getHandler() {
    return (IType) getProperty(PROP_FORM_HANDLER);
  }

  public void setFormHandler(IType formHandler) {
    try {
      setStateChanging(true);
      setFormHandlerInternal(formHandler);
      if (isControlCreated()) {
        m_formHandlerField.acceptProposal(formHandler);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setFormHandlerInternal(IType formHandler) {
    setProperty(PROP_FORM_HANDLER, formHandler);
  }

}
