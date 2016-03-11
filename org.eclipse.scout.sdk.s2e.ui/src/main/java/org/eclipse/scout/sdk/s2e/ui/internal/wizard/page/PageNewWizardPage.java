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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalListener;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

  protected ProposalTextField m_sharedSourceFolder;
  protected ProposalTextField m_serverSourceFolder;

  public PageNewWizardPage(PackageContainer packageContainer) {
    super(PageNewWizardPage.class.getName(), packageContainer, ISdkProperties.SUFFIX_PAGE, IScoutRuntimeTypes.IPage, IScoutRuntimeTypes.AbstractPageWithTable, ScoutTier.Client);
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

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_PAGE_NEW_WIZARD_PAGE);
  }

  @Override
  protected void handleJavaProjectChanged() {
    super.handleJavaProjectChanged();
    guessSharedFolders();
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

  protected void createPageServcieGroup(Composite p) {
    Group parent = getFieldToolkit().createGroupBox(p, "PageData & Service Source Folders");
    parent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    parent.setLayout(new GridLayout(1, true));

    // shared source folder
    m_sharedSourceFolder = getFieldToolkit().createSourceFolderTextField(parent, "Shared Source Folder", ScoutTier.Shared, 20);
    GridData sharedGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    sharedGridData.horizontalSpan = 3;
    sharedGridData.verticalIndent = 8;
    m_sharedSourceFolder.setLayoutData(sharedGridData);
    m_sharedSourceFolder.acceptProposal(getSharedSourceFolder());
    m_sharedSourceFolder.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setSharedSourceFolderInternal((IPackageFragmentRoot) proposal);
        pingStateChanging();
      }
    });

    // server source folder
    m_serverSourceFolder = getFieldToolkit().createSourceFolderTextField(parent, "Server Source Folder", ScoutTier.Server, 20);
    m_serverSourceFolder.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_serverSourceFolder.acceptProposal(getServerSourceFolder());
    m_serverSourceFolder.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setServerSourceFolderInternal((IPackageFragmentRoot) proposal);
        pingStateChanging();
      }
    });
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
}
