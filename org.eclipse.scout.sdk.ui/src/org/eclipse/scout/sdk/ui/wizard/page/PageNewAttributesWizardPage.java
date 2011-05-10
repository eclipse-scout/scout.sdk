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
package org.eclipse.scout.sdk.ui.wizard.page;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.page.PageNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>PageNewWizardPage2</h3> ...
 */
public class PageNewAttributesWizardPage extends AbstractWorkspaceWizardPage {

  private IType iPage = ScoutSdk.getType(RuntimeClasses.IPage);
  private IType iPageWithNodes = ScoutSdk.getType(RuntimeClasses.IPageWithNodes);
  private IType iPageWithTable = ScoutSdk.getType(RuntimeClasses.IPageWithTable);
  private IType iOutline = ScoutSdk.getType(RuntimeClasses.IOutline);

  private NlsProposal m_nlsName;
  private String m_typeName;
  private IType m_superType;
  private ITypeProposal m_holderType;
  private String m_nameSuffix;

  private NlsProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_holderTypeField;

  // process members
  private PageNewOperation m_operation;

  private boolean m_hoderTypeEnabled = true;

  private IScoutBundle m_clientBundle;

  public PageNewAttributesWizardPage() {
    super(PageNewAttributesWizardPage.class.getName());
    setTitle("New Page");
    setDefaultMessage("Create a new page.");
    m_nameSuffix = "";
    setSuperType(ScoutSdk.getType(RuntimeClasses.AbstractPageWithNodes));
    setOperation(new PageNewOperation(true));
  }

  @Override
  protected void createContent(Composite parent) {
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, null, "Name");
    m_nlsNameField.acceptProposal(m_nlsName);
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
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

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, "Type Name");
    m_typeNameField.setReadOnlySuffix(m_nameSuffix);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    Control parentPageControl = createParentPageGroup(parent);
    updateUiField(getClientBundle());
    // layout
    parent.setLayout(new GridLayout(1, true));
    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    parentPageControl.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  protected Control createParentPageGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_OUT);
    group.setText("Add to");

    m_holderTypeField = getFieldToolkit().createProposalField(group, null, "Page/Outline");

    m_holderTypeField.acceptProposal(getHolderType());
    m_holderTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      public void proposalAccepted(ContentProposalEvent event) {
        m_holderType = (ITypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    m_holderTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return group;
  }

  private void updateUiField(IScoutBundle clientBundle) {
    INlsProject nlsProject = null;
    DefaultProposalProvider holderTypePropProvider = null;
    if (clientBundle != null) {
      nlsProject = clientBundle.findBestMatchNlsProject();
      ITypeFilter filter = TypeFilters.getMultiTypeFilter(
          TypeFilters.getInScoutBundles(getClientBundle()),
          TypeFilters.getClassFilter());

      IType[] pages = ScoutSdk.getPrimaryTypeHierarchy(iPage).getAllSubtypes(iPageWithNodes, filter);
      IType[] outlines = ScoutSdk.getPrimaryTypeHierarchy(iOutline).getAllSubtypes(iOutline, filter);
      IType[] propTypes = new IType[pages.length + outlines.length];
      System.arraycopy(pages, 0, propTypes, 0, pages.length);
      System.arraycopy(outlines, 0, propTypes, pages.length, outlines.length);
      Arrays.sort(propTypes, TypeComparators.getTypeNameComparator());
      ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(propTypes);
      holderTypePropProvider = new DefaultProposalProvider(proposals);
    }
    m_nlsNameField.setNlsProject(nlsProject);
    IContentProposalEx selectedProposal = m_holderTypeField.getSelectedProposal();
    m_holderTypeField.setContentProposalProvider(holderTypePropProvider);
    m_holderTypeField.acceptProposal(selectedProposal);
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    // write back members
    getOperation().setClientBundle(getClientBundle());
    if (getNlsName() != null) {
      getOperation().setNlsEntry(getNlsName().getNlsEntry());
    }
    getOperation().setTypeName(getTypeName());
    IType superType = getSuperType();
    if (superType != null) {
      getOperation().setSuperTypeSignature(Signature.createTypeSignature(superType.getFullyQualifiedName(), true));
    }
    if (getHolderType() != null) {
      getOperation().setHolderType(getHolderType().getType());
    }

    getOperation().run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusClientBundle());
      if (!multiStatus.matches(IStatus.ERROR)) {
        multiStatus.add(getStatusNameField());
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusClientBundle() throws JavaModelException {
    if (getClientBundle() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "client bundle missing");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(m_nameSuffix)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (ScoutSdk.existsType(getClientBundle().getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_DESKTOP_OUTLINES_PAGES) + "." + getTypeName())) {
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

  public void setOperation(PageNewOperation operation) {
    m_operation = operation;
  }

  public PageNewOperation getOperation() {
    return m_operation;
  }

  public void setClientBundle(IScoutBundle clientBundle) {
    try {
      setStateChanging(true);
      m_clientBundle = clientBundle;
      if (isControlCreated()) {
        updateUiField(clientBundle);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
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

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      m_superType = superType;
      if (TypeUtility.exists(superType)) {
        try {
          ITypeHierarchy superTypeHierarchy = superType.newSupertypeHierarchy(null);
          if (superTypeHierarchy.contains(iPageWithNodes)) {
            m_nameSuffix = ScoutIdeProperties.SUFFIX_OUTLINE_NODE_PAGE;
          }
          else if (superTypeHierarchy.contains(iPageWithTable)) {
            m_nameSuffix = ScoutIdeProperties.SUFFIX_OUTLINE_TABLE_PAGE;
          }
          else {
            m_nameSuffix = ScoutIdeProperties.SUFFIX_OUTLINE_PAGE;
          }
        }
        catch (JavaModelException e) {
          ScoutSdkUi.logError("could not create superTypeHierarchy of '" + getSuperType().getFullyQualifiedName() + "'.");
        }
      }
      if (isControlCreated()) {
        m_typeNameField.setReadOnlySuffix(m_nameSuffix);
      }

    }
    finally {
      setStateChanging(false);
    }
  }

  public void setHolderType(ITypeProposal holderPage) {
    try {
      setStateChanging(true);
      m_holderType = holderPage;
      if (isControlCreated()) {
        m_holderTypeField.acceptProposal(holderPage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public ITypeProposal getHolderType() {
    return m_holderType;
  }

  public void setHoderTypeEnabled(boolean hoderTypeEnabled) {
    if (isControlCreated()) {
      throw new IllegalStateException("control already created.");
    }
    m_hoderTypeEnabled = hoderTypeEnabled;
  }

  public boolean isHoderTypeEnabled() {
    return m_hoderTypeEnabled;
  }

}
