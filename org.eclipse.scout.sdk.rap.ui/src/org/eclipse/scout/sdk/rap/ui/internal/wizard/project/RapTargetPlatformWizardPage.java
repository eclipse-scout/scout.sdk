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
package org.eclipse.scout.sdk.rap.ui.internal.wizard.project;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.compatibility.P2Utility;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.rap.RapRuntimeClasses;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.rap.operations.project.FillUiRapPluginOperation;
import org.eclipse.scout.sdk.rap.operations.project.FillUiRapPluginOperation.TARGET_STRATEGY;
import org.eclipse.scout.sdk.rap.ui.internal.ScoutSdkRapUI;
import org.eclipse.scout.sdk.ui.fields.FileSelectionField;
import org.eclipse.scout.sdk.ui.fields.IFileSelectionListener;
import org.eclipse.scout.sdk.ui.internal.wizard.newproject.ScoutProjectNewWizard;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.osgi.framework.Version;

public class RapTargetPlatformWizardPage extends AbstractProjectNewWizardPage {

  private static final String RAP_RUNTIME_FEATURE_PREFIX = "org.eclipse.rap.runtime_1.5";
  private static final String RAP_RWT_PLUGIN_PREFIX = "org.eclipse.rap.rwt_1.5";
  private static final String RAP_TARGET_DEFAULT_SUB_FOLDER = "rap_target";

  private static final String PROP_TARGET_STRATEGY = "propTargetStrategy";
  private static final String PROP_LOCAL_TARGET_FOLDER = "propLocalTargetFolder";
  private static final String PROP_EXTRACT_TARGET_FOLDER = "propExtractTargetFolder";
  private static final String PROP_DOWNLOAD_ECLIPSE_PLATFORM = "propDownloadEclipsePlatform";

  private final boolean m_isRapTargetPluginAvailable;
  private boolean m_messageShown;

  private Button m_extractButton;
  private Button m_remoteButton;
  private Button m_localButton;
  private Button m_laterButton;

  private Control m_extractTargetGroup;
  private Control m_localTargetGroup;
  private Control m_remoteTargetGroup;

  private FileSelectionField m_extractTargetLocationField;
  private FileSelectionField m_localTargetLocationField;
  private Button m_includeRemoteRequirementsButton;

  public RapTargetPlatformWizardPage() {
    super(RapTargetPlatformWizardPage.class.getName());
    m_isRapTargetPluginAvailable = checkIfRapTargetPluginCanBeUsed();
    setTitle(Texts.get("RapTargetDownloadWizardPageTitle"));
    File defRap = getDefaultRapLocation();
    setIsDownloadEclipsePlatformInternal(false);
    m_messageShown = false;
    if (defRap != null && defRap.exists()) {
      setTargetStrategy(TARGET_STRATEGY.STRATEGY_LOCAL_EXISTING);
      setLocalTargetFolder(defRap.getAbsolutePath());
      if (isRapTargetPluginAvailable()) {
        setExtractTargetFolder(getDefaultRapLocation().getAbsolutePath());
      }
    }
    else {
      setLocalTargetFolder(getEclipseInstallLocation().getAbsolutePath());
      if (isRapTargetPluginAvailable()) {
        setTargetStrategy(TARGET_STRATEGY.STRATEGY_LOCAL_EXTRACT);
        setExtractTargetFolder(getDefaultRapLocation().getAbsolutePath());
      }
      else {
        setTargetStrategy(TARGET_STRATEGY.STRATEGY_REMOTE);
      }
    }
  }

  @Override
  protected void createContent(Composite parent) {
    Composite switchBox = new Composite(parent, SWT.NONE);

    if (isRapTargetPluginAvailable()) {
      m_extractButton = new Button(switchBox, SWT.RADIO);
      m_extractButton.setText(Texts.get("CreateNewRAPTarget"));
      m_extractButton.setSelection(TARGET_STRATEGY.STRATEGY_LOCAL_EXTRACT.equals(getTargetStrategy()));
      m_extractButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_LOCAL_EXTRACT));
    }

    m_remoteButton = new Button(switchBox, SWT.RADIO);
    m_remoteButton.setText(Texts.get("DownloadRAPTarget"));
    m_remoteButton.setSelection(TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy()));
    m_remoteButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_REMOTE));

    m_localButton = new Button(switchBox, SWT.RADIO);
    m_localButton.setText(Texts.get("LocalRAPTarget"));
    m_localButton.setSelection(TARGET_STRATEGY.STRATEGY_LOCAL_EXISTING.equals(getTargetStrategy()));
    m_localButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_LOCAL_EXISTING));

    m_laterButton = new Button(switchBox, SWT.RADIO);
    m_laterButton.setText(Texts.get("InstallRapTargetLater"));
    m_laterButton.setSelection(TARGET_STRATEGY.STRATEGY_LATER.equals(getTargetStrategy()));
    m_laterButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_LATER));

    if (isRapTargetPluginAvailable()) {
      m_extractTargetGroup = createExtractTargetGroup(parent);
      m_extractTargetGroup.setVisible(TARGET_STRATEGY.STRATEGY_LOCAL_EXTRACT.equals(getTargetStrategy()));
    }

    m_localTargetGroup = createLocalTargetGroup(parent);
    m_localTargetGroup.setVisible(TARGET_STRATEGY.STRATEGY_LOCAL_EXISTING.equals(getTargetStrategy()));

    m_remoteTargetGroup = createRemoteGroup(parent);
    m_remoteTargetGroup.setVisible(TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy()));

    //layout
    parent.setLayout(new GridLayout(1, true));
    switchBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    if (isRapTargetPluginAvailable()) {
      switchBox.setLayout(new GridLayout(4, true));
    }
    else {
      switchBox.setLayout(new GridLayout(3, true));
    }

    GridData localGroupData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    localGroupData.exclude = !TARGET_STRATEGY.STRATEGY_LOCAL_EXISTING.equals(getTargetStrategy());
    m_localTargetGroup.setLayoutData(localGroupData);

    GridData remoteGroupData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    remoteGroupData.exclude = !TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy());
    m_remoteTargetGroup.setLayoutData(remoteGroupData);

    if (isRapTargetPluginAvailable()) {
      GridData extractGroupData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
      extractGroupData.exclude = !TARGET_STRATEGY.STRATEGY_LOCAL_EXTRACT.equals(getTargetStrategy());
      m_extractTargetGroup.setLayoutData(extractGroupData);
    }
  }

  private File getEclipseInstallLocation() {
    Location l = Platform.getInstallLocation();
    File ret = null;
    if (l != null) {
      ret = new File(l.getURL().getPath());
    }
    return ret;
  }

  private File getDefaultRapLocation() {
    File l = getEclipseInstallLocation();
    File ret = null;
    if (l != null) {
      ret = new File(l, RAP_TARGET_DEFAULT_SUB_FOLDER);
    }
    else {
      File[] roots = File.listRoots();
      if (roots.length > 0) {
        ret = new File(roots[0], RAP_TARGET_DEFAULT_SUB_FOLDER);
      }
    }
    return ret;
  }

  private Control createExtractTargetGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("CreateNewRAPTarget"));
    m_extractTargetLocationField = new FileSelectionField(group);
    m_extractTargetLocationField.setLabelText(Texts.get("RAPTargetLocation"));
    m_extractTargetLocationField.setFolderMode(true);
    m_extractTargetLocationField.setFile(new File(getExtractTargetFolder()));

    m_extractTargetLocationField.addProductSelectionListener(new IFileSelectionListener() {
      @Override
      public void fileSelected(File file) {
        String fileName = "";
        if (file != null) {
          fileName = file.getAbsolutePath();
        }
        setExtractTargetFolderInternal(fileName);
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    m_extractTargetLocationField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  private Control createRemoteGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("DownloadRAPTarget"));
    m_includeRemoteRequirementsButton = new Button(group, SWT.CHECK);
    m_includeRemoteRequirementsButton.setSelection(isDownloadEclipsePlatform());
    m_includeRemoteRequirementsButton.setText(Texts.get("DownloadEclipsePlatformAsWell"));
    m_includeRemoteRequirementsButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (m_includeRemoteRequirementsButton.getSelection() && !m_messageShown && !PlatformVersionUtility.isJuno()) {
          MessageBox msgBox = new MessageBox(m_includeRemoteRequirementsButton.getShell(), SWT.OK | SWT.ICON_WARNING);
          msgBox.setMessage(Texts.get("RapTargetInfoMessage"));
          msgBox.open();
          m_messageShown = true;
        }
        setIsDownloadEclipsePlatformInternal(m_includeRemoteRequirementsButton.getSelection());
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    m_includeRemoteRequirementsButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  private Control createLocalTargetGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("LocalRAPTarget"));
    m_localTargetLocationField = new FileSelectionField(group);
    m_localTargetLocationField.setLabelText(Texts.get("RAPTargetLocation"));
    m_localTargetLocationField.setFolderMode(true);
    m_localTargetLocationField.setFile(new File(getLocalTargetFolder()));

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
  @SuppressWarnings("unchecked")
  public void putProperties(PropertyMap properties) {
    Set<String> checkedNodeIds = properties.getProperty(IScoutProjectNewOperation.PROP_PROJECT_CHECKED_NODES, Set.class);
    if (checkedNodeIds != null && checkedNodeIds.contains(CreateUiRapPluginOperation.BUNDLE_ID)) {
      properties.setProperty(FillUiRapPluginOperation.PROP_LOCAL_TARGET_FOLDER, getLocalTargetFolder());
      properties.setProperty(FillUiRapPluginOperation.PROP_EXTRACT_TARGET_FOLDER, getExtractTargetFolder());
      properties.setProperty(FillUiRapPluginOperation.PROP_TARGET_STRATEGY, getTargetStrategy());
      properties.setProperty(FillUiRapPluginOperation.PROP_DOWNLOAD_ECLIPSE_PLATFORM, isDownloadEclipsePlatform());

      if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_REMOTE && isDownloadEclipsePlatform()) {
        Version v = getRemotePlatformVersion();
        if (v != null) {
          properties.setProperty(IScoutProjectNewOperation.PROP_TARGET_PLATFORM_VERSION, v);
        }
      }
    }
  }

  private Version getRemotePlatformVersion() {
    try {
      String version = P2Utility.getLatestVersion(FillUiRapPluginOperation.ECLIPSE_PLATFORM_FEATURE,
          new URI(FillUiRapPluginOperation.JUNO_UPDATE_SITE_URL), new NullProgressMonitor());
      return new Version(version);
    }
    catch (Exception e) {
      return null;
    }
  }

  public void setTargetStrategy(TARGET_STRATEGY strategy) {
    try {
      if (strategy == null) {
        strategy = TARGET_STRATEGY.STRATEGY_REMOTE;
      }
      setTargetStrategyInternal(strategy);
      if (isControlCreated()) {
        switch (strategy) {
          case STRATEGY_LOCAL_EXISTING:
            m_localButton.setSelection(true);
            break;
          case STRATEGY_LOCAL_EXTRACT:
            m_extractButton.setSelection(true);
            break;
          case STRATEGY_REMOTE:
            m_remoteButton.setSelection(true);
            break;
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

  public Boolean isDownloadEclipsePlatform() {
    return (Boolean) getProperty(PROP_DOWNLOAD_ECLIPSE_PLATFORM);
  }

  public void setIsDownloadEclipsePlatform(Boolean val) {
    try {
      setStateChanging(true);
      setIsDownloadEclipsePlatformInternal(val);
      if (isControlCreated()) {
        m_includeRemoteRequirementsButton.setSelection(val);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setIsDownloadEclipsePlatformInternal(Boolean val) {
    setProperty(PROP_DOWNLOAD_ECLIPSE_PLATFORM, val);
  }

  public String getExtractTargetFolder() {
    return (String) getProperty(PROP_EXTRACT_TARGET_FOLDER);
  }

  public void setExtractTargetFolder(String proposal) {
    try {
      setStateChanging(true);
      setExtractTargetFolderInternal(proposal);
      if (isControlCreated()) {
        m_extractTargetLocationField.setFileName(proposal);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setExtractTargetFolderInternal(String extractTargetLocation) {
    if (extractTargetLocation != null) {
      extractTargetLocation = extractTargetLocation.trim();
    }
    setProperty(PROP_EXTRACT_TARGET_FOLDER, extractTargetLocation);
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

  private void setLocalTargetFolderInternal(String localTargetLocation) {
    if (localTargetLocation != null) {
      localTargetLocation = localTargetLocation.trim();
    }
    setProperty(PROP_LOCAL_TARGET_FOLDER, localTargetLocation);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusStrategy());
  }

  private boolean isRapTargetPluginAvailable() {
    return m_isRapTargetPluginAvailable;
  }

  private static boolean checkIfRapTargetPluginCanBeUsed() {
    return PlatformVersionUtility.isJuno() && Platform.getBundle(RapRuntimeClasses.ScoutRapTargetPlugin) != null;
  }

  private IStatus getStatusStrategy() {
    if (TARGET_STRATEGY.STRATEGY_LOCAL_EXISTING.equals(getTargetStrategy())) {
      if (!StringUtility.hasText(getLocalTargetFolder())) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetLocationDefined"));
      }
      File target = new File(getLocalTargetFolder());
      if (!target.exists() || !new File(target, "plugins").exists()) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetFoundUnder", getLocalTargetFolder()));
      }
      else {
        try {
          File featuresFolder = new File(new File(getLocalTargetFolder()), "features");
          if (featuresFolder.exists()) {
            // try to find e.g. org.eclipse.rap.runtime_1.5.0.20111024-0120 feature
            String[] featureNames = featuresFolder.list(new FilenameFilter() {
              @Override
              public boolean accept(File dir, String name) {
                return name.startsWith(RAP_RUNTIME_FEATURE_PREFIX);
              }
            });
            if (featureNames.length == 1) {
              return Status.OK_STATUS;
            }
          }
          // fallback try to find e.g. 'org.eclipse.rap.rwt_1.5.0.201110241651.jar' plugin
          File pluginsFolder = new File(new File(getLocalTargetFolder()), "plugins");
          if (pluginsFolder.exists()) {
            String[] pluginNames = pluginsFolder.list(new FilenameFilter() {
              @Override
              public boolean accept(File dir, String name) {
                return name.startsWith(RAP_RWT_PLUGIN_PREFIX);
              }
            });
            if (pluginNames.length == 1) {
              return Status.OK_STATUS;
            }
          }
        }
        catch (IllegalArgumentException e) {
          ScoutSdkRapUI.logError("could not parse rap feature version.", e);
        }
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetFoundUnder", getLocalTargetFolder()));
      }
    }
    else if (TARGET_STRATEGY.STRATEGY_LOCAL_EXTRACT.equals(getTargetStrategy())) {
      if (!StringUtility.hasText(getExtractTargetFolder())) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetLocationDefined"));
      }
      File eclipseFolder = getEclipseInstallLocation();
      File targetDir = new File(getExtractTargetFolder());
      if (CompareUtility.equals(eclipseFolder, targetDir)) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("RAPTargetNotInstallableInPlatform"));
      }
      return new Status(IStatus.INFO, ScoutSdkRapUI.PLUGIN_ID, Texts.get("ANewRAPTargetWillBeCreated", getExtractTargetFolder()));
    }
    else if (TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy())) {
      if (isDownloadEclipsePlatform()) {
        int severity = IStatus.INFO;
        if (!PlatformVersionUtility.isJuno()) {
          severity = IStatus.WARNING;
        }
        return new Status(severity, ScoutSdkRapUI.PLUGIN_ID, Texts.get("RapTargetAndEclipseDownload"));
      }
      else {
        return new Status(IStatus.INFO, ScoutSdkRapUI.PLUGIN_ID, Texts.get("TheRAPTargetWillBeDownloaded"));
      }
    }
    else if (TARGET_STRATEGY.STRATEGY_LATER.equals(getTargetStrategy())) {
      return new Status(IStatus.INFO, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetWillBeSet"));
    }
    return Status.OK_STATUS;
  }

  private class P_StrategySelectionListener extends SelectionAdapter {
    private final TARGET_STRATEGY m_strategy;

    public P_StrategySelectionListener(TARGET_STRATEGY strategy) {
      m_strategy = strategy;
    }

    private void setGroupVisible(Control toBeVisible) {
      boolean localVisible = m_localTargetGroup == toBeVisible;
      m_localTargetGroup.setVisible(localVisible);
      ((GridData) m_localTargetGroup.getLayoutData()).exclude = !localVisible;

      boolean remoteVisible = m_remoteTargetGroup == toBeVisible;
      m_remoteTargetGroup.setVisible(remoteVisible);
      ((GridData) m_remoteTargetGroup.getLayoutData()).exclude = !remoteVisible;

      if (isRapTargetPluginAvailable()) {
        boolean extractVisible = m_extractTargetGroup == toBeVisible;
        m_extractTargetGroup.setVisible(extractVisible);
        ((GridData) m_extractTargetGroup.getLayoutData()).exclude = !extractVisible;
      }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      setTargetStrategyInternal(m_strategy);
      switch (m_strategy) {
        case STRATEGY_LOCAL_EXISTING:
          setGroupVisible(m_localTargetGroup);
          break;
        case STRATEGY_LOCAL_EXTRACT:
          setGroupVisible(m_extractTargetGroup);
          break;
        case STRATEGY_REMOTE:
          setGroupVisible(m_remoteTargetGroup);
          break;
        case STRATEGY_LATER:
          setGroupVisible(null);
          break;
      }
      m_localTargetGroup.getParent().layout(true);
      pingStateChanging();
    }
  } // end class P_StrategySelectionListener
}
