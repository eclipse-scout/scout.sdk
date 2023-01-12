/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.page;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractCompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link PageNewWizardPage}</h3>
 *
 * @since 5.2.0
 */
public class PageNewWizardPage extends AbstractCompilationUnitNewWizardPage {

  public static final String PROP_SHARED_SOURCE_FOLDER = "sharedSourceFolder";
  public static final String PROP_SERVER_SOURCE_FOLDER = "serverSourceFolder";
  public static final String PROP_CREATE_ABSTRACT_PAGE = "createAbstractPage";

  protected ProposalTextField m_sharedSourceFolder;
  protected ProposalTextField m_serverSourceFolder;
  private Button m_createAbstractPageButton;
  private boolean m_isPageWithTable;

  public PageNewWizardPage(PackageContainer packageContainer) {
    super(PageNewWizardPage.class.getName(), packageContainer, ISdkConstants.SUFFIX_PAGE_WITH_TABLE, ScoutTier.Client);
    setTitle("Create a new Page");
    setDescription(getTitle());
    setIcuGroupName("New Page Details");
  }

  @Override
  protected Optional<ITypeNameSupplier> calcSuperTypeDefaultFqn() {
    return scoutApi().map(IScoutApi::AbstractPageWithTable);
  }

  @Override
  protected Optional<ITypeNameSupplier> calcSuperTypeDefaultBaseFqn() {
    return scoutApi().map(IScoutApi::IPage);
  }

  @Override
  public PageNewWizard getWizard() {
    return (PageNewWizard) super.getWizard();
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    guessSharedAndServerFolders();

    createPageServiceGroup(parent);
    createOptionsGroup(parent);

    // remove AbstractPage from the proposal list
    var superTypeContentProvider = (StrictHierarchyTypeContentProvider) getSuperTypeField().getContentProvider();
    superTypeContentProvider.setTypeProposalFilter(superTypeContentProvider.getTypeProposalFilter()
        .and(element -> !scoutApi().orElseThrow().AbstractPage().fqn().equals(element.getFullyQualifiedName())));

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_PAGE_NEW_WIZARD_PAGE);
  }

  protected void createOptionsGroup(Composite p) {
    var optionsGroup = FieldToolkit.createGroupBox(p, "Options");
    GridLayoutFactory
        .swtDefaults()
        .applyTo(optionsGroup);
    GridDataFactory
        .defaultsFor(optionsGroup)
        .align(SWT.FILL, SWT.BEGINNING)
        .applyTo(optionsGroup);

    m_createAbstractPageButton = FieldToolkit.createCheckBox(optionsGroup, "Create an Abstract Super Page", isCreateAbstractPage());
    m_createAbstractPageButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreateAbstractPageInternal(m_createAbstractPageButton.getSelection());
        pingStateChanging();
      }
    });
    GridDataFactory
        .defaultsFor(m_createAbstractPageButton)
        .indent(5, 2)
        .applyTo(m_createAbstractPageButton);
  }

  @Override
  protected void handleJavaProjectChanged() {
    super.handleJavaProjectChanged();
    guessSharedAndServerFolders();
  }

  @Override
  protected void handleSuperTypeChanged() {
    super.handleSuperTypeChanged();
    var superType = getSuperType();
    if (!JdtUtils.exists(superType)) {
      setIsPageWithTable(false);
      return;
    }

    try {
      var supertypeHierarchy = superType.newSupertypeHierarchy(null);
      setIsPageWithTable(JdtUtils.hierarchyContains(supertypeHierarchy, scoutApi().orElseThrow().IPageWithTable().fqn()));
      if (isPageWithTable()) {
        setReadOnlySuffix(ISdkConstants.SUFFIX_PAGE_WITH_TABLE);
      }
      else {
        setReadOnlySuffix(ISdkConstants.SUFFIX_PAGE_WITH_NODES);
      }
      setViewSharedSourceFolder();
      setViewServerSourceFolder();
    }
    catch (JavaModelException e) {
      SdkLog.warning("Unable to calculate super type hierarchy for type '{}'.", superType.getFullyQualifiedName(), e);
    }
  }

  protected void setViewServerSourceFolder() {
    if (m_serverSourceFolder == null) {
      return;
    }
    m_serverSourceFolder.setEnabled(isPageWithTable());
  }

  protected void setViewSharedSourceFolder() {
    if (m_sharedSourceFolder == null) {
      return;
    }
    m_sharedSourceFolder.setEnabled(isPageWithTable());
  }

  protected void guessSharedAndServerFolders() {
    var clientSourceFolder = getSourceFolder();
    if (!JdtUtils.exists(clientSourceFolder)) {
      return;
    }

    setServerSourceFolder(S2eTier.wrap(ScoutTier.Client).convert(ScoutTier.Server, clientSourceFolder).orElse(null));
    setSharedSourceFolder(S2eTier.wrap(ScoutTier.Client).convert(ScoutTier.Shared, clientSourceFolder).orElse(null));
  }

  @Override
  protected int getLabelWidth() {
    return 120;
  }

  protected void createPageServiceGroup(Composite p) {
    var parent = FieldToolkit.createGroupBox(p, "PageData and Service Source Folders");

    // shared source folder
    m_sharedSourceFolder = FieldToolkit.createSourceFolderField(parent, "Shared Source Folder", ScoutTier.Shared, getLabelWidth());
    m_sharedSourceFolder.acceptProposal(getSharedSourceFolder());
    m_sharedSourceFolder.addProposalListener(proposal -> {
      setSharedSourceFolderInternal((IPackageFragmentRoot) proposal);
      pingStateChanging();
    });
    setViewSharedSourceFolder();

    // server source folder
    m_serverSourceFolder = FieldToolkit.createSourceFolderField(parent, "Server Source Folder", ScoutTier.Server, getLabelWidth());
    m_serverSourceFolder.acceptProposal(getServerSourceFolder());
    m_serverSourceFolder.setEnabled(isPageWithTable());
    m_serverSourceFolder.addProposalListener(proposal -> {
      setServerSourceFolderInternal((IPackageFragmentRoot) proposal);
      pingStateChanging();
    });
    setViewServerSourceFolder();

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(parent);
    GridDataFactory
        .defaultsFor(m_sharedSourceFolder)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .span(3, 0)
        .indent(0, 8)
        .applyTo(m_sharedSourceFolder);
    GridDataFactory
        .defaultsFor(m_serverSourceFolder)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_serverSourceFolder);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    super.validatePage(multiStatus);
    multiStatus.add(getStatusVisibility());
  }

  protected IStatus getStatusVisibility() {
    if (!JdtUtils.exists(getSharedSourceFolder())) {
      return Status.OK_STATUS;
    }

    if (JdtUtils.exists(getServerSourceFolder()) && !getServerSourceFolder().getJavaProject().isOnClasspath(getSharedSourceFolder().getJavaProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The Shared Source Folder is not accessible from the selected Server Source Folder.");
    }
    if (JdtUtils.exists(getSourceFolder()) && !getSourceFolder().getJavaProject().isOnClasspath(getSharedSourceFolder().getJavaProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The Shared Source Folder is not accessible from the selected Page Source Folder.");
    }
    return Status.OK_STATUS;
  }

  public IPackageFragmentRoot getSharedSourceFolder() {
    return getProperty(PROP_SHARED_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setSharedSourceFolder(IPackageFragmentRoot sharedSourceFolder) {
    setPropertyWithChangingControl(m_sharedSourceFolder, () -> setSharedSourceFolderInternal(sharedSourceFolder), field -> field.acceptProposal(sharedSourceFolder));
  }

  protected boolean setSharedSourceFolderInternal(IPackageFragmentRoot sharedSourceFolder) {
    return setProperty(PROP_SHARED_SOURCE_FOLDER, sharedSourceFolder);
  }

  public IPackageFragmentRoot getServerSourceFolder() {
    return getProperty(PROP_SERVER_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setServerSourceFolder(IPackageFragmentRoot serverSourceFolder) {
    setPropertyWithChangingControl(m_serverSourceFolder, () -> setServerSourceFolderInternal(serverSourceFolder), field -> field.acceptProposal(serverSourceFolder));
  }

  protected boolean setServerSourceFolderInternal(IPackageFragmentRoot serverSourceFolder) {
    return setProperty(PROP_SERVER_SOURCE_FOLDER, serverSourceFolder);
  }

  public boolean isCreateAbstractPage() {
    var val = getProperty(PROP_CREATE_ABSTRACT_PAGE, Boolean.class);
    return val != null && val;
  }

  public void setIsCreateAbstractPage(boolean createAbstractPage) {
    setPropertyWithChangingControl(m_createAbstractPageButton, () -> setIsCreateAbstractPageInternal(createAbstractPage), field -> field.setSelection(createAbstractPage));
  }

  protected boolean setIsCreateAbstractPageInternal(boolean createAbstractPage) {
    return setProperty(PROP_CREATE_ABSTRACT_PAGE, createAbstractPage);
  }

  protected boolean isPageWithTable() {
    return m_isPageWithTable;
  }

  protected void setIsPageWithTable(boolean isPageWithTable) {
    m_isPageWithTable = isPageWithTable;
  }
}
