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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.MenuNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
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
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;
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

  static IType abstractMenuType = ScoutSdk.getType(RuntimeClasses.AbstractMenu);
  static IType iMenuType = ScoutSdk.getType(RuntimeClasses.IMenu);
  static IType iformType = ScoutSdk.getType(RuntimeClasses.IForm);

  private NlsProposal m_nlsName;
  private String m_typeName;
  private ITypeProposal m_superType;

  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_siblingField;
  private ProposalTextField m_formToOpenField;
  private ProposalTextField m_formHandlerField;

  // process members
  private final IType m_declaringType;
  private IType m_createdMenu;

  public MenuNewWizardPage(IType declaringType) {
    super(Texts.get("NewMenu"));
    setTitle(Texts.get("NewMenu"));
    setDescription(Texts.get("CreateANewMenu"));
    m_declaringType = declaringType;
    m_superType = ScoutProposalUtility.getScoutTypeProposalsFor(abstractMenuType)[0];
    setSiblingInternal(SiblingProposal.SIBLING_END);

  }

  @Override
  protected void createContent(Composite parent) {
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, SdkTypeUtility.findNlsProject(m_declaringType), Texts.get("Name"));
    m_nlsNameField.acceptProposal(m_nlsName);
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = null;
          if (getNlsName() != null) {
            oldEntry = getNlsName().getNlsEntry();
          }
          m_nlsName = (NlsProposal) event.proposal;
          if (m_nlsName != null) {
            if (oldEntry == null || oldEntry.getKey().equals(m_typeNameField.getModifiableText()) || StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(m_nlsName.getNlsEntry().getKey());
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_MENU);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });
    ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(abstractMenuType);
    IPrimaryTypeTypeHierarchy menuHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iMenuType);
    IType[] abstractMenus = menuHierarchy.getAllSubtypes(iMenuType, TypeFilters.getAbstractOnClasspath(getDeclaringType().getJavaProject()));
    ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(abstractMenus);

    m_superTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(shotList, proposals), Texts.get("SuperType"));
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (ITypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    ArrayList<SiblingProposal> availableSiblings = new ArrayList<SiblingProposal>();

    IRegion region = JavaCore.newRegion();
    region.add(getDeclaringType());
    ITypeHierarchy combinedTypeHierarchy = menuHierarchy.combinedTypeHierarchy(region);
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(iMenuType, combinedTypeHierarchy));
    IType[] innerTypes = TypeUtility.getInnerTypes(getDeclaringType(), filter, TypeComparators.getOrderAnnotationComparator());
    for (IType menu : innerTypes) {
      availableSiblings.add(new SiblingProposal(menu));
    }
    if (availableSiblings.size() > 0) {
      availableSiblings.add(SiblingProposal.SIBLING_END);
    }
    m_siblingField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(availableSiblings.toArray(new SiblingProposal[availableSiblings.size()])), "Sibling");
    m_siblingField.acceptProposal(getSibling());
    m_siblingField.setEnabled(availableSiblings.size() > 0);
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
    ITypeHierarchy cachedFormHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iformType);
    ITypeFilter formsFilter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getTypesOnClasspath(getDeclaringType().getJavaProject()),
        TypeFilters.getClassFilter());
    IType[] formCandidates = cachedFormHierarchy.getAllSubtypes(iformType, formsFilter, TypeComparators.getTypeNameComparator());
    ITypeProposal[] formProposals = ScoutProposalUtility.getScoutTypeProposalsFor(formCandidates);

    m_formToOpenField = getFieldToolkit().createProposalField(groupBox, new DefaultProposalProvider(formProposals), Texts.get("FormToStart"));
    m_formToOpenField.acceptProposal(getFormToOpen());
    m_formToOpenField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          ITypeProposal formProposal = (ITypeProposal) event.proposal;
          setFormToOpenInternal(formProposal);
          DefaultProposalProvider formHandlerProvider = new DefaultProposalProvider();
          IType form = null;
          ITypeProposal formHandlerSelection = (ITypeProposal) m_formHandlerField.getSelectedProposal();
          if (formProposal != null) {
            m_formHandlerField.setEnabled(true);
            form = formProposal.getType();
            IType[] formHandlers = SdkTypeUtility.getFormHandlers(form);
            if (formHandlers != null) {
              formHandlerProvider = new DefaultProposalProvider(ScoutProposalUtility.getScoutTypeProposalsFor(formHandlers));
            }
            // assign null selection if the current selected form is not the declaring type of the form hanlder selection.
            if (formHandlerSelection != null && !form.equals(formHandlerSelection.getType().getDeclaringType())) {
              formHandlerSelection = null;
            }
          }
          else {
            m_formHandlerField.setEnabled(false);
          }
          // backup
          m_formHandlerField.setContentProposalProvider(formHandlerProvider);
          m_formHandlerField.acceptProposal(formHandlerSelection);
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_formHandlerField = getFieldToolkit().createProposalField(groupBox, new DefaultProposalProvider(), Texts.get("FormHandler"));
    m_formHandlerField.setEnabled(false);
    m_formHandlerField.acceptProposal(getFormToOpen());
    m_formHandlerField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setFormHandlerInternal((ITypeProposal) event.proposal);
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
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    // create menu
    MenuNewOperation operation = new MenuNewOperation(getDeclaringType(), true);
    // write back members
    if (getNlsName() != null) {
      operation.setNlsEntry(getNlsName().getNlsEntry());
    }
    operation.setTypeName(getTypeName());
    ITypeProposal superTypeProp = getSuperType();
    if (superTypeProp != null) {
      String signature = Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true);
      operation.setSuperTypeSignature(signature);
    }
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = SdkTypeUtility.createStructuredType(m_declaringType);
      operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_MENU));
    }
    else {
      operation.setSibling(getSibling().getScoutType());
    }
    if (getFormToOpen() != null) {
      operation.setFormToOpen(getFormToOpen().getType());
    }
    if (getHandler() != null) {
      operation.setFormHandler(getHandler().getType());
    }
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
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_MENU)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (TypeUtility.hasInnerType(m_declaringType, getTypeName())) {
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

  public IType getDeclaringType() {
    return m_declaringType;
  }

  /**
   * @return the createdMenu
   */
  public IType getCreatedMenu() {
    return m_createdMenu;
  }

  public NlsProposal getNlsName() {
    return m_nlsName;
  }

  public void setNlsName(NlsProposal nlsName) {
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

  public ITypeProposal getFormToOpen() {
    return (ITypeProposal) getProperty(PROP_FORM_TO_OPEN);
  }

  public void setFormToOpen(ITypeProposal formToOpen) {
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

  private void setFormToOpenInternal(ITypeProposal formToOpen) {
    setProperty(PROP_FORM_TO_OPEN, formToOpen);
  }

  public ITypeProposal getHandler() {
    return (ITypeProposal) getProperty(PROP_FORM_HANDLER);
  }

  public void setFormHandler(ITypeProposal formHandler) {
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

  private void setFormHandlerInternal(ITypeProposal formHandler) {
    setProperty(PROP_FORM_HANDLER, formHandler);
  }

}
