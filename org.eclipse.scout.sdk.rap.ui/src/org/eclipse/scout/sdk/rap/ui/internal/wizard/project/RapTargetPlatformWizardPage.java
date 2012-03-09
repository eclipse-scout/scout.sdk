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
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
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

public class RapTargetPlatformWizardPage extends AbstractProjectNewWizardPage {

  private static final String RAP_RUNTIME_FEATURE_PREFIX = "org.eclipse.rap.runtime_1.5";
  private static final String RAP_RWT_PLUGIN_PREFIX = "org.eclipse.rap.rwt_1.5";

  private static final String PROP_TARGET_STRATEGY = "propTargetStrategy";
  private static final String PROP_LOCAL_TARGET_FOLDER = "propLocalTargetForlder";

  private boolean m_visited = false;
  private IFile m_targetFile;

  private Button m_remoteButton;
  private Button m_localButton;
  private Button m_laterButton;

  private Control m_localTargetGroup;

  private FileSelectionField m_localTargetLocationField;

  public RapTargetPlatformWizardPage() {
    super(RapTargetPlatformWizardPage.class.getName());
    setTitle(Texts.get("RapTargetDownloadWizardPageTitle"));
    setTargetStrategy(TARGET_STRATEGY.STRATEGY_REMOTE);
    setLocalTargetFolder(getEclipseInstallLocation().getAbsolutePath());
  }

  @Override
  protected void createContent(Composite parent) {
    Composite switchBox = new Composite(parent, SWT.NONE);
    m_remoteButton = new Button(switchBox, SWT.RADIO);
    m_remoteButton.setText(Texts.get("DownloadRAPTarget"));
    m_remoteButton.setSelection(TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy()));
    m_remoteButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_REMOTE));

    m_localButton = new Button(switchBox, SWT.RADIO);
    m_localButton.setText(Texts.get("LocalRAPTarget"));
    m_localButton.setSelection(TARGET_STRATEGY.STRATEGY_LOCAL.equals(getTargetStrategy()));
    m_localButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_LOCAL));

    m_laterButton = new Button(switchBox, SWT.RADIO);
    m_laterButton.setText(Texts.get("InstallRapTargetLater"));
    m_laterButton.setSelection(TARGET_STRATEGY.STRATEGY_LATER.equals(getTargetStrategy()));
    m_laterButton.addSelectionListener(new P_StrategySelectionListener(TARGET_STRATEGY.STRATEGY_LATER));

    m_localTargetGroup = createLocalTargetGroup(parent);
    m_localTargetGroup.setVisible(TARGET_STRATEGY.STRATEGY_LOCAL.equals(getTargetStrategy()));

    //layout
    parent.setLayout(new GridLayout(1, true));
    switchBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    switchBox.setLayout(new GridLayout(3, true));

    GridData localGroupData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    localGroupData.exclude = TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy());
    m_localTargetGroup.setLayoutData(localGroupData);
  }

  private File getEclipseInstallLocation() {
    Location l = Platform.getInstallLocation();
    File ret = null;
    if (l != null) {
      ret = new File(l.getURL().getPath());
    }
    else {
      ret = File.listRoots()[0];
    }
    return ret;
  }

  private Control createLocalTargetGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText("Local RAP Target");
    m_localTargetLocationField = new FileSelectionField(group);
    m_localTargetLocationField.setLabelText("RAP Target location");
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
  @SuppressWarnings("unchecked")
  public void putProperties(PropertyMap properties) {

    Set<String> checkedNodeIds = properties.getProperty(IScoutProjectNewOperation.PROP_PROJECT_CHECKED_NODES, Set.class);
    if (checkedNodeIds != null && checkedNodeIds.contains(CreateUiRapPluginOperation.BUNDLE_ID)) {
      properties.setProperty(FillUiRapPluginOperation.PROP_LOCAL_TARGET_FOLDER, getLocalTargetFolder());
      properties.setProperty(FillUiRapPluginOperation.PROP_TARGET_STRATEGY, getTargetStrategy());
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
    setProperty(PROP_LOCAL_TARGET_FOLDER, localTargetLocation);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusStrategy());
  }

  private IStatus getStatusStrategy() {
    if (TARGET_STRATEGY.STRATEGY_LOCAL.equals(getTargetStrategy())) {
      if (StringUtility.isNullOrEmpty(getLocalTargetFolder())) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, "No RAP target location defined.");
      }
      File target = new File(getLocalTargetFolder());
      if (!target.exists() || !new File(target, "plugins").exists()) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, "No RAP target found under '" + getLocalTargetFolder() + "'.");
      }
      else {
        try {
          File featuresFolder = new File(new File(getLocalTargetFolder()), "features");
          if (featuresFolder.exists()) {
            // feature org.eclipse.rap.runtime_1.5.0.20111024-0120
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
          // fallback try to find 'org.eclipse.rap.rwt_1.5.0.201110241651.jar' plugin
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
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, "No RAP target found under '" + getLocalTargetFolder() + "'.");
      }
    }
    else if (TARGET_STRATEGY.STRATEGY_REMOTE.equals(getTargetStrategy())) {
      return new Status(IStatus.INFO, ScoutSdkRapUI.PLUGIN_ID, "The RAP target will be downloaded. This can take some minutes.");
    }
    else {
      return Status.OK_STATUS;
    }
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
          break;
        case STRATEGY_REMOTE:
          m_localTargetGroup.setVisible(false);
          ((GridData) m_localTargetGroup.getLayoutData()).exclude = true;
          break;
        case STRATEGY_LATER:
          m_localTargetGroup.setVisible(false);
          ((GridData) m_localTargetGroup.getLayoutData()).exclude = true;
          break;
      }
      m_localTargetGroup.getParent().layout(true);
      pingStateChanging();
    }
  } // end class P_StrategySelectionListener
}
