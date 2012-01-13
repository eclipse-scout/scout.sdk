/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.rap.internal.wizard.project;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateAjaxServletOperation;
import org.eclipse.scout.sdk.rap.operations.project.InstallTargetPlatformFileOperation;
import org.eclipse.scout.sdk.ui.fields.FileSelectionField;
import org.eclipse.scout.sdk.ui.fields.IFileSelectionListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.newproject.ScoutProjectNewWizard;
import org.eclipse.scout.sdk.ui.rap.ScoutSdkRapUI;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizardPage;
import org.eclipse.scout.sdk.util.PlatformUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.osgi.framework.Version;

public class RapTargetPlatformWizardPage extends AbstractProjectNewWizardPage {

  public enum TARGET_STRATEGY {
    STRATEGY_REMOTE,
    STRATEGY_LOCAL,
    STRATEGY_LATER
  }

  public static final String PROP_TARGET_STRATEGY = "propTargetStrategy";
  public static final String PROP_REMOTE_TARGET_URL = "propRemtoeTargetURL";
  public static final String PROP_LOCAL_TARGET_FOLDER = "propLocalTargetForlder";

  private final static String TARGET_REPOSITORY_LATEST_BUILD = "http://download.eclipse.org/rt/rap/latest-stable/runtime"; //$NON-NLS-1$
  private final static String TARGET_REPOSITORY_LATEST_RELEASE = "http://download.eclipse.org/rt/rap/latest-release/runtime"; //$NON-NLS-1$
  DefaultProposal remoteUrlProposalV_1_4;
  DefaultProposal remoteUrlProposalVLatest;

  private boolean m_visited = false;
  private IFile m_targetFile;
  private Version m_localTargetVersion = null;

  private Button m_remoteButton;
  private Button m_localButton;
  private Control m_remoteTargetGroup;
  private Control m_localTargetGroup;
  private ProposalTextField m_remoteRapVersionField;
  private Button m_laterButton;
  private FileSelectionField m_localTargetLocationField;

  public RapTargetPlatformWizardPage() {
    super(RapTargetPlatformWizardPage.class.getName());
    // init proposals
    remoteUrlProposalV_1_4 = new DefaultProposal("Version 1.4 (recomanded)", ScoutSdkUi.getImage(ScoutSdkUi.Default));
    remoteUrlProposalV_1_4.setData(PROP_REMOTE_TARGET_URL, "http://download.eclipse.org/rt/rap/1.4/runtime");
    remoteUrlProposalVLatest = new DefaultProposal("Latest release (experimental)", ScoutSdkUi.getImage(ScoutSdkUi.Default));
    remoteUrlProposalVLatest.setData(PROP_REMOTE_TARGET_URL, "http://download.eclipse.org/rt/rap/latest-release/runtime");
    setTargetStrategy(TARGET_STRATEGY.STRATEGY_REMOTE);
    setRemoteTargetUrl(remoteUrlProposalV_1_4);
    setLocalTargetFolder("D:/RAP/rapTarget");
    setTargetStrategy(TARGET_STRATEGY.STRATEGY_LOCAL);
  }

  @Override
  protected void createContent(Composite parent) {
    Composite switchBox = new Composite(parent, SWT.NONE);
    m_remoteButton = new Button(switchBox, SWT.RADIO);
    m_remoteButton.setText("Download RAP Target");
    m_remoteButton.setSelection(TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy()));
    m_remoteButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_REMOTE));
    m_localButton = new Button(switchBox, SWT.RADIO);
    m_localButton.setText("Local RAP Target");
    m_localButton.setSelection(TARGET_STRATEGY.STRATEGY_LOCAL.equals(getTargetStrategy()));
    m_localButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_LOCAL));
    m_laterButton = new Button(switchBox, SWT.RADIO);
    m_laterButton.setText("I'll do it later...");
    m_laterButton.setSelection(TARGET_STRATEGY.STRATEGY_LATER.equals(getTargetStrategy()));
    m_laterButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_LATER));

    m_remoteTargetGroup = createRemoteTargetGroup(parent);
    m_remoteTargetGroup.setVisible(TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy()));
    m_localTargetGroup = createLocalTargetGroup(parent);
    m_localTargetGroup.setVisible(TARGET_STRATEGY.STRATEGY_LOCAL.equals(getTargetStrategy()));

    //layout
    parent.setLayout(new GridLayout(1, true));
    switchBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    switchBox.setLayout(new GridLayout(3, true));
    GridData remoteData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    remoteData.exclude = TARGET_STRATEGY.STRATEGY_LOCAL.equals(getTargetStrategy());
    m_remoteTargetGroup.setLayoutData(remoteData);
    GridData localGroupData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    localGroupData.exclude = TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy());
    m_localTargetGroup.setLayoutData(localGroupData);

  }

  private Control createRemoteTargetGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText("Remote RAP Target");

    DefaultProposalProvider provider = new DefaultProposalProvider(new IContentProposalEx[]{remoteUrlProposalV_1_4, remoteUrlProposalVLatest});
    m_remoteRapVersionField = getFieldToolkit().createProposalField(group, provider, "RAP Version");
    m_remoteRapVersionField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setRemoteTargetUrlInternal((DefaultProposal) event.proposal);
        pingStateChanging();
      }
    });
    m_remoteRapVersionField.acceptProposal(getRemoteTargetUrl());

    // layout
    group.setLayout(new GridLayout(1, true));
    m_remoteRapVersionField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return group;
  }

  private File getEclipseInstallLocation() {
    Location l = Platform.getInstallLocation();
    File ret = null;
    if (l != null) {
      ret = new File(l.getURL().getPath());
    }
    else {
      ret = new File(getLocalTargetFolder());
    }
    return ret;
  }

  private Control createLocalTargetGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText("Local RAP Target");
    m_localTargetLocationField = new FileSelectionField(group);
    m_localTargetLocationField.setLabelText("RAP Target location");
    m_localTargetLocationField.setFolderMode(true);
    m_localTargetLocationField.setFile(getEclipseInstallLocation());

    m_localTargetLocationField.addProductSelectionListener(new IFileSelectionListener() {
      @Override
      public void fileSelected(File file) {
        String fileName = "";
        if (file != null) {
          fileName = file.getAbsolutePath();
        }
        setLocalTargetFolderInternal(fileName);
        pingStateChanging();

      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    m_localTargetLocationField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  @Override
  public ScoutProjectNewWizard getWizard() {
    return (ScoutProjectNewWizard) super.getWizard();
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      m_visited = true;
    }
    super.setVisible(visible);
  }

  @Override
  public boolean isPageComplete() {
    if (m_visited) {
      return super.isPageComplete();
    }
    return false;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor) {
    OperationJob job = new OperationJob(new P_PerformFinishOperation());
    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      ScoutSdkRapUI.logInfo("", e);
    }
    try {
      Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, monitor);
      Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
    }
    catch (Exception e) {
      ScoutSdkUi.logError("error during waiting for auto build and refresh");
    }
    if (m_targetFile != null) {
      try {
        PlatformUtility.resolveTargetPlatform(m_targetFile, true, monitor);
      }
      catch (CoreException e) {
        ScoutSdkRapUI.logError("could not set target to file '" + m_targetFile.getProjectRelativePath().toString() + "'.", e);
      }
    }

    return true;
  }

  public void setTargetStrategy(TARGET_STRATEGY strategy) {
    try {
      if (strategy == null) {
        strategy = TARGET_STRATEGY.STRATEGY_REMOTE;
      }
      setTargetStrategyInternal(strategy);
      if (isControlCreated()) {
        switch (strategy) {
          case STRATEGY_LOCAL:
            m_localButton.setSelection(true);
            break;
          case STRATEGY_REMOTE:
            m_remoteButton.setSelection(true);
          case STRATEGY_LATER:
            m_laterButton.setSelection(true);
            break;
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTargetStrategyInternal(TARGET_STRATEGY strategy) {
    setProperty(PROP_TARGET_STRATEGY, strategy);
  }

  public TARGET_STRATEGY getTargetStrategy() {
    return (TARGET_STRATEGY) getProperty(PROP_TARGET_STRATEGY);
  }

  public void setRemoteTargetUrl(DefaultProposal proposal) {
    try {
      setStateChanging(true);
      setRemoteTargetUrlInternal(proposal);
      if (isControlCreated()) {
        m_remoteRapVersionField.acceptProposal(proposal);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setRemoteTargetUrlInternal(DefaultProposal proposal) {
    setProperty(PROP_REMOTE_TARGET_URL, proposal);
  }

  public String getLocalTargetFolder() {
    return (String) getProperty(PROP_LOCAL_TARGET_FOLDER);
  }

  public void setLocalTargetFolder(String proposal) {
    try {
      setStateChanging(true);
      setLocalTargetFolderInternal(proposal);
      if (isControlCreated()) {
        m_localTargetLocationField.setFileName(proposal);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private static final String RAP_RUNTIME_FEATURE_PREFIX = "org.eclipse.rap.runtime_";
  private static final String RAP_RWT_PLUGIN_PREFIX = "org.eclipse.rap.rwt_";

  private void setLocalTargetFolderInternal(String localTargetLocation) {
    setProperty(PROP_LOCAL_TARGET_FOLDER, localTargetLocation);
    // find local version
    m_localTargetVersion = null;
    try {
      File featuresFolder = new File(new File(localTargetLocation), "features");
      if (featuresFolder.exists()) {
        // feature org.eclipse.rap.runtime_1.5.0.20111024-0120
        String[] featureNames = featuresFolder.list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.startsWith(RAP_RUNTIME_FEATURE_PREFIX);
          }
        });
        if (featureNames.length == 1) {
          m_localTargetVersion = Version.parseVersion(featureNames[0].substring(RAP_RUNTIME_FEATURE_PREFIX.length()));
          return;
        }
      }
      // fallback try to find 'org.eclipse.rap.rwt_1.5.0.201110241651.jar' plugin
      File pluginsFolder = new File(new File(localTargetLocation), "plugins");
      if (pluginsFolder.exists()) {
        String[] pluginNames = pluginsFolder.list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.startsWith(RAP_RWT_PLUGIN_PREFIX);
          }
        });
        if (pluginNames.length == 1) {
          String version = pluginNames[0].substring(RAP_RWT_PLUGIN_PREFIX.length());
          if (version.endsWith(".jar")) {
            version = version.substring(0, version.length() - 4);
          }
          m_localTargetVersion = Version.parseVersion(version);
          return;
        }
      }
    }
    catch (IllegalArgumentException e) {
      ScoutSdkRapUI.logError("could not parse rap feature version.", e);
    }
  }

  public DefaultProposal getRemoteTargetUrl() {
    return (DefaultProposal) getProperty(PROP_REMOTE_TARGET_URL);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusRemote());
    multiStatus.add(getStatusLocal());
  }

  private IStatus getStatusRemote() {
    if (TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy())) {
      if (getRemoteTargetUrl() == null) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, "no RAP target URL selected.");
      }
    }
    return Status.OK_STATUS;
  }

  private IStatus getStatusLocal() {
    if (TARGET_STRATEGY.STRATEGY_LOCAL.equals(getTargetStrategy())) {
      if (StringUtility.isNullOrEmpty(getLocalTargetFolder())) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, "No RAP target location defined.");
      }
      File target = new File(getLocalTargetFolder());
      if (!target.exists() || !new File(target, "plugins").exists()) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, "No RAP target found under '" + getLocalTargetFolder() + "'.");
      }

    }
    return Status.OK_STATUS;
  }

  private class P_StrategySelectionListener extends SelectionAdapter {
    private final TARGET_STRATEGY m_strategy;

    public P_StrategySelectionListener(TARGET_STRATEGY strategy) {
      m_strategy = strategy;

    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      setTargetStrategyInternal(m_strategy);
      switch (m_strategy) {
        case STRATEGY_LOCAL:
          m_localTargetGroup.setVisible(true);
          ((GridData) m_localTargetGroup.getLayoutData()).exclude = false;
          m_remoteTargetGroup.setVisible(false);
          ((GridData) m_remoteTargetGroup.getLayoutData()).exclude = true;
          break;
        case STRATEGY_REMOTE:
          m_localTargetGroup.setVisible(false);
          ((GridData) m_localTargetGroup.getLayoutData()).exclude = true;
          m_remoteTargetGroup.setVisible(true);
          ((GridData) m_remoteTargetGroup.getLayoutData()).exclude = false;
          break;
        case STRATEGY_LATER:
          m_localTargetGroup.setVisible(false);
          ((GridData) m_localTargetGroup.getLayoutData()).exclude = true;
          m_remoteTargetGroup.setVisible(false);
          ((GridData) m_remoteTargetGroup.getLayoutData()).exclude = true;
          break;
      }
      m_localTargetGroup.getParent().layout(true);
      pingStateChanging();
    }
  } // end class P_StrategySelectionListener

  private class P_PerformFinishOperation implements IOperation {
    @Override
    public String getOperationName() {
      return "create RAP bundle";
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      IScoutProjectWizardPage projectWizardPage = getWizard().getProjectWizardPage();
      String serverBundleName = projectWizardPage.getProjectName() + ".server" + ((projectWizardPage.getProjectNamePostfix() != null) ? (projectWizardPage.getProjectNamePostfix()) : (""));
      IJavaProject serverProject = getWizard().getCreatedBundle(serverBundleName);
      if (serverProject != null) {
        CreateAjaxServletOperation createAjaxServletOperation = new CreateAjaxServletOperation(serverProject);
        createAjaxServletOperation.run(monitor, workingCopyManager);
      }

      String rapBundleName = projectWizardPage.getProjectName() + ".ui.rap" + ((projectWizardPage.getProjectNamePostfix() != null) ? (projectWizardPage.getProjectNamePostfix()) : (""));
      IJavaProject rapBundle = getWizard().getCreatedBundle(rapBundleName);
      if (rapBundle != null) {
        InstallTargetPlatformFileOperation op = new InstallTargetPlatformFileOperation(rapBundle.getProject());
        switch (getTargetStrategy()) {
          case STRATEGY_LOCAL:
            op.setRapTargetLocalFolder(getLocalTargetFolder());
            op.setRapVersion(m_localTargetVersion);
            break;
          case STRATEGY_REMOTE:
            op.setRapTargetRemoteURL((String) getRemoteTargetUrl().getData(PROP_REMOTE_TARGET_URL));
            break;
          case STRATEGY_LATER:
            return;
        }
        op.run(monitor, workingCopyManager);
        m_targetFile = op.getCreatedFile();
      }
      else {
        ScoutSdkRapUI.logWarning("no rap bundle '" + rapBundleName + "' found.");
      }
    }
  }
}
