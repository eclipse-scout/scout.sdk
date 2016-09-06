/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.page;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.Filters;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalListener;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link PageNewWizardPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PageNewWizardPage extends CompilationUnitNewWizardPage {

  public static final String PROP_SHARED_SOURCE_FOLDER = "sharedSourceFolder";
  public static final String PROP_SERVER_SOURCE_FOLDER = "serverSourceFolder";
  public static final String PROP_CREATE_ABSTRACT_PAGE = "createAbstractPage";

  protected ProposalTextField m_sharedSourceFolder;
  protected ProposalTextField m_serverSourceFolder;
  private Button m_createAbstractPageButton;
  private boolean m_isPageWithTable;

  public PageNewWizardPage(PackageContainer packageContainer) {
    super(PageNewWizardPage.class.getName(), packageContainer, ISdkProperties.SUFFIX_PAGE_WITH_TABLE, IScoutRuntimeTypes.IPage, IScoutRuntimeTypes.AbstractPageWithTable, ScoutTier.Client);
    setTitle("Create a new Page");
    setDescription(getTitle());
    setIcuGroupName("New Page Details");
  }

  @Override
  public PageNewWizard getWizard() {
    return (PageNewWizard) super.getWizard();
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    guessSharedFolders();

    createPageServcieGroup(parent);
    createOptionsGroup(parent);

    // remove AbstractPage from the proposal list
    StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) getSuperTypeField().getContentProvider();
    superTypeContentProvider.setTypeProposalFilter(Filters.and(superTypeContentProvider.getTypeProposalFilter(), new IFilter<IType>() {
      @Override
      public boolean evaluate(IType element) {
        return !IScoutRuntimeTypes.AbstractPage.equals(element.getFullyQualifiedName());
      }
    }));

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_PAGE_NEW_WIZARD_PAGE);
  }

  protected void createOptionsGroup(Composite p) {
    Group optionsGroup = getFieldToolkit().createGroupBox(p, "Options");
    GridLayoutFactory
        .swtDefaults()
        .applyTo(optionsGroup);
    GridDataFactory
        .defaultsFor(optionsGroup)
        .align(SWT.FILL, SWT.BEGINNING)
        .applyTo(optionsGroup);

    m_createAbstractPageButton = getFieldToolkit().createCheckBox(optionsGroup, "Create an Abstract Super Page", isCreateAbstractPage());
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
    guessSharedFolders();
  }

  @Override
  protected void handleSuperTypeChanged() {
    super.handleSuperTypeChanged();
    IType superType = getSuperType();
    if (!S2eUtils.exists(superType)) {
      setIsPageWithTable(false);
      return;
    }

    try {
      ITypeHierarchy supertypeHierarchy = superType.newSupertypeHierarchy(null);
      setIsPageWithTable(S2eUtils.hierarchyContains(supertypeHierarchy, IScoutRuntimeTypes.IPageWithTable));
      if (isPageWithTable()) {
        setReadOnlySuffix(ISdkProperties.SUFFIX_PAGE_WITH_TABLE);
      }
      else {
        setReadOnlySuffix(ISdkProperties.SUFFIX_PAGE_WITH_NODES);
      }
      if (m_serverSourceFolder != null) {
        m_serverSourceFolder.setEnabled(isPageWithTable());
      }
    }
    catch (JavaModelException e) {
      SdkLog.warning("Unable to calculate super type hierarchy for type '{}'.", superType.getFullyQualifiedName(), e);
    }
  }

  protected void guessSharedFolders() {
    IPackageFragmentRoot clientSourceFolder = getSourceFolder();
    if (!S2eUtils.exists(clientSourceFolder)) {
      return;
    }

    try {
      setServerSourceFolder(ScoutTier.Client.convert(ScoutTier.Server, clientSourceFolder));
    }
    catch (JavaModelException e) {
      SdkLog.info("Unable to calculate server source folder.", e);
    }

    try {
      setSharedSourceFolder(ScoutTier.Client.convert(ScoutTier.Shared, clientSourceFolder));
    }
    catch (JavaModelException e) {
      SdkLog.info("Unable to calculate shared source folder.", e);
    }
  }

  @Override
  protected int getLabelWidth() {
    return 120;
  }

  protected void createPageServcieGroup(Composite p) {
    Group parent = getFieldToolkit().createGroupBox(p, "PageData and Service Source Folders");

    // shared source folder
    m_sharedSourceFolder = getFieldToolkit().createSourceFolderField(parent, "Shared Source Folder", ScoutTier.Shared, getLabelWidth());
    m_sharedSourceFolder.acceptProposal(getSharedSourceFolder());
    m_sharedSourceFolder.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setSharedSourceFolderInternal((IPackageFragmentRoot) proposal);
        pingStateChanging();
      }
    });

    // server source folder
    m_serverSourceFolder = getFieldToolkit().createSourceFolderField(parent, "Server Source Folder", ScoutTier.Server, getLabelWidth());
    m_serverSourceFolder.acceptProposal(getServerSourceFolder());
    m_serverSourceFolder.setEnabled(isPageWithTable());
    m_serverSourceFolder.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setServerSourceFolderInternal((IPackageFragmentRoot) proposal);
        pingStateChanging();
      }
    });

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

  public IPackageFragmentRoot getSharedSourceFolder() {
    return getProperty(PROP_SHARED_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setSharedSourceFolder(IPackageFragmentRoot sharedSourceFolder) {
    try {
      setStateChanging(true);
      setSharedSourceFolderInternal(sharedSourceFolder);
      if (isControlCreated() && m_sharedSourceFolder != null) {
        m_sharedSourceFolder.acceptProposal(sharedSourceFolder);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setSharedSourceFolderInternal(IPackageFragmentRoot sharedSourceFolder) {
    setProperty(PROP_SHARED_SOURCE_FOLDER, sharedSourceFolder);
  }

  public IPackageFragmentRoot getServerSourceFolder() {
    return getProperty(PROP_SERVER_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setServerSourceFolder(IPackageFragmentRoot serverSourceFolder) {
    try {
      setStateChanging(true);
      setServerSourceFolderInternal(serverSourceFolder);
      if (isControlCreated() && m_serverSourceFolder != null) {
        m_serverSourceFolder.acceptProposal(serverSourceFolder);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setServerSourceFolderInternal(IPackageFragmentRoot serverSourceFolder) {
    setProperty(PROP_SERVER_SOURCE_FOLDER, serverSourceFolder);
  }

  public boolean isCreateAbstractPage() {
    Boolean val = getProperty(PROP_CREATE_ABSTRACT_PAGE, Boolean.class);
    return val != null && val.booleanValue();
  }

  public void setIsCreateAbstractPage(boolean createAbstractPage) {
    try {
      setStateChanging(true);
      setIsCreateAbstractPageInternal(createAbstractPage);
      if (isControlCreated() && m_createAbstractPageButton != null) {
        m_createAbstractPageButton.setSelection(createAbstractPage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setIsCreateAbstractPageInternal(boolean createAbstractPage) {
    setProperty(PROP_CREATE_ABSTRACT_PAGE, createAbstractPage);
  }

  protected boolean isPageWithTable() {
    return m_isPageWithTable;
  }

  protected void setIsPageWithTable(boolean isPageWithTable) {
    m_isPageWithTable = isPageWithTable;
  }
}
