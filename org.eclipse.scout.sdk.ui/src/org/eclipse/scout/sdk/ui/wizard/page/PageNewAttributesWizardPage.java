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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
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

  public static final String PROP_TYPE_NAME = "typeName";

  private IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);
  private IType iPageWithNodes = TypeUtility.getType(IRuntimeClasses.IPageWithNodes);
  private IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
  private IType iOutline = TypeUtility.getType(IRuntimeClasses.IOutline);

  private INlsEntry m_nlsName;
  private IType m_superType;
  private IType m_holderType;
  private String m_nameSuffix;
  private String m_packageName;

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_holderTypeField;
  private EntityTextField m_entityField;

  // process members

  private boolean m_hoderTypeEnabled = true;

  private final IScoutBundle m_clientBundle;

  public PageNewAttributesWizardPage(IScoutBundle clientBundle) {
    super(PageNewAttributesWizardPage.class.getName());
    m_clientBundle = clientBundle;
    setTitle(Texts.get("NewPage"));
    setDescription(Texts.get("CreateANewPage"));
    setTargetPackage(DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_PAGES));
    m_nameSuffix = "";
    setSuperType(RuntimeClasses.getSuperType(IRuntimeClasses.IPageWithNodes, m_clientBundle));
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;
    Group group = new Group(parent, SWT.SHADOW_ETCHED_OUT);
    group.setText(Texts.get("Page"));

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(group, getClientBundle().getNlsProject(), Texts.get("Name"), labelColWidthPercent);
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

    m_typeNameField = getFieldToolkit().createStyledTextField(group, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(m_nameSuffix);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(group, Texts.get("EntityTextField"), m_clientBundle, labelColWidthPercent);
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

    Control parentPageControl = createParentPageGroup(parent);

    // layout
    parent.setLayout(new GridLayout(1, true));
    group.setLayout(new GridLayout(1, true));
    group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    parentPageControl.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    m_nlsNameField.setFocus();
  }

  protected Control createParentPageGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_OUT);
    group.setText(Texts.get("AddTo"));

    m_holderTypeField = getFieldToolkit().createJavaElementProposalField(group, Texts.get("PageOutline"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        ITypeFilter filter = TypeFilters.getMultiTypeFilter(
            ScoutTypeFilters.getInScoutBundles(getClientBundle()),
            TypeFilters.getClassFilter());

        IType[] pages = TypeUtility.getPrimaryTypeHierarchy(iPage).getAllSubtypes(iPageWithNodes, filter);
        IType[] outlines = TypeUtility.getPrimaryTypeHierarchy(iOutline).getAllSubtypes(iOutline, filter);
        IType[] propTypes = new IType[pages.length + outlines.length];
        System.arraycopy(pages, 0, propTypes, 0, pages.length);
        System.arraycopy(outlines, 0, propTypes, pages.length, outlines.length);
        Arrays.sort(propTypes, TypeComparators.getTypeNameComparator());
        return new Object[][]{propTypes};
      }
    });
    m_holderTypeField.acceptProposal(getHolderType());
    m_holderTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_holderType = (IType) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    m_holderTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return group;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusClientBundle());
      if (!multiStatus.matches(IStatus.ERROR)) {
        multiStatus.add(getStatusNameField());
        multiStatus.add(getStatusTargetPackge());
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusTargetPackge() {
    return ScoutUtility.validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusClientBundle() throws JavaModelException {
    if (getClientBundle() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ClientBundleMissing"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(getTypeName(), m_nameSuffix);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    IStatus existingStatus = ScoutUtility.validateTypeNotExisting(getClientBundle(), getTargetPackage(), getTypeName());
    if (!existingStatus.isOK()) {
      return existingStatus;
    }
    return javaFieldNameStatus;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
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
            m_nameSuffix = SdkProperties.SUFFIX_OUTLINE_NODE_PAGE;
          }
          else if (superTypeHierarchy.contains(iPageWithTable)) {
            m_nameSuffix = SdkProperties.SUFFIX_OUTLINE_TABLE_PAGE;
          }
          else {
            m_nameSuffix = SdkProperties.SUFFIX_OUTLINE_PAGE;
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

  public void setHolderType(IType holderPage) {
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

  public IType getHolderType() {
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
