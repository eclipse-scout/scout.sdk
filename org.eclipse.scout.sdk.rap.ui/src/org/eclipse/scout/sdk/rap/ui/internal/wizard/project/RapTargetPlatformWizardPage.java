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
import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.util.InstallTargetPlatformFileOperation;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.rap.operations.project.AppendRapTargetOperation;
import org.eclipse.scout.sdk.rap.operations.project.AppendRapTargetOperation.TargetStrategy;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.rap.ui.internal.ScoutSdkRapUI;
import org.eclipse.scout.sdk.ui.fields.FileSelectionField;
import org.eclipse.scout.sdk.ui.fields.IFileSelectionListener;
import org.eclipse.scout.sdk.ui.internal.wizard.newproject.ScoutProjectNewWizard;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public class RapTargetPlatformWizardPage extends AbstractProjectNewWizardPage {

  private static final String[] RAP_PLUGIN_DETECTION_PREFIXES = new String[]{"org.eclipse.rap.rwt_3", "org.eclipse.scout.rt.ui.rap_4.2", "org.eclipse.scout.rt.ui.rap.mobile_4.2"};
  private static final String RAP_TARGET_DEFAULT_SUB_FOLDER = "rap_target";

  private static final String PROP_TARGET_STRATEGY = "propTargetStrategy";
  private static final String PROP_LOCAL_TARGET_FOLDER = "propLocalTargetFolder";
  private static final String PROP_EXTRACT_TARGET_FOLDER = "propExtractTargetFolder";

  private static final int LABEL_PERCENTAGE = 20;

  private Button m_extractButton;
  private Button m_remoteButton;
  private Button m_localButton;
  private Button m_laterButton;

  private Control m_extractTargetGroup;
  private Control m_localTargetGroup;

  private FileSelectionField m_extractTargetLocationField;
  private FileSelectionField m_localTargetLocationField;

  private TargetStrategy[] m_offeredTargetStrategies;

  public RapTargetPlatformWizardPage() {
    this(TargetStrategy.values());
  }

  public RapTargetPlatformWizardPage(TargetStrategy[] offeredTargetStrategies) {
    super(RapTargetPlatformWizardPage.class.getName());
    setTitle(Texts.get("RapTargetDownloadWizardPageTitle"));
    initOfferedTargetStrategies(offeredTargetStrategies);

    File defRap = getDefaultRapLocation();
    if (defRap != null && defRap.exists() && isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXISTING)) {
      setTargetStrategy(TargetStrategy.STRATEGY_LOCAL_EXISTING);
      setLocalTargetFolder(defRap.getAbsolutePath());
      if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXTRACT)) {
        setExtractTargetFolder(defRap.getAbsolutePath());
      }
    }
    else {
      setLocalTargetFolder(ResourceUtility.getEclipseInstallLocation().getAbsolutePath());
      if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXTRACT)) {
        setTargetStrategy(TargetStrategy.STRATEGY_LOCAL_EXTRACT);
        if (defRap != null) {
          setExtractTargetFolder(defRap.getAbsolutePath());
        }
      }
      else if (isStrategyOffered(TargetStrategy.STRATEGY_REMOTE)) {
        setTargetStrategy(TargetStrategy.STRATEGY_REMOTE);
      }
      else if (getOfferedTargetStrategies().length > 0) {
        setTargetStrategy(getOfferedTargetStrategies()[0]);
      }
    }
  }

  @Override
  protected void createContent(Composite parent) {
    if (getOfferedTargetStrategies().length > 1) {
      Composite switchBox = new Composite(parent, SWT.NONE);

      if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXTRACT)) {
        m_extractButton = new Button(switchBox, SWT.RADIO);
        m_extractButton.setText(Texts.get("CreateNewRAPTarget"));
        m_extractButton.setSelection(TargetStrategy.STRATEGY_LOCAL_EXTRACT.equals(getTargetStrategy()));
        m_extractButton.addSelectionListener(new P_StrategySelectionListener(TargetStrategy.STRATEGY_LOCAL_EXTRACT));
      }

      if (isStrategyOffered(TargetStrategy.STRATEGY_REMOTE)) {
        m_remoteButton = new Button(switchBox, SWT.RADIO);
        m_remoteButton.setText(Texts.get("DownloadRAPTarget"));
        m_remoteButton.setSelection(TargetStrategy.STRATEGY_REMOTE.equals(getTargetStrategy()));
        m_remoteButton.addSelectionListener(new P_StrategySelectionListener(TargetStrategy.STRATEGY_REMOTE));
      }

      if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXISTING)) {
        m_localButton = new Button(switchBox, SWT.RADIO);
        m_localButton.setText(Texts.get("LocalRAPTarget"));
        m_localButton.setSelection(TargetStrategy.STRATEGY_LOCAL_EXISTING.equals(getTargetStrategy()));
        m_localButton.addSelectionListener(new P_StrategySelectionListener(TargetStrategy.STRATEGY_LOCAL_EXISTING));
      }

      if (isStrategyOffered(TargetStrategy.STRATEGY_LATER)) {
        m_laterButton = new Button(switchBox, SWT.RADIO);
        m_laterButton.setText(Texts.get("InstallRapTargetLater"));
        m_laterButton.setSelection(TargetStrategy.STRATEGY_LATER.equals(getTargetStrategy()));
        m_laterButton.addSelectionListener(new P_StrategySelectionListener(TargetStrategy.STRATEGY_LATER));
      }

      switchBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
      switchBox.setLayout(new GridLayout(getOfferedTargetStrategies().length, true));
    }

    if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXTRACT)) {
      m_extractTargetGroup = createExtractTargetGroup(parent);
      m_extractTargetGroup.setVisible(TargetStrategy.STRATEGY_LOCAL_EXTRACT.equals(getTargetStrategy()));

      GridData extractGroupData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
      extractGroupData.exclude = !TargetStrategy.STRATEGY_LOCAL_EXTRACT.equals(getTargetStrategy());
      m_extractTargetGroup.setLayoutData(extractGroupData);
    }

    if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXISTING)) {
      m_localTargetGroup = createLocalTargetGroup(parent);
      m_localTargetGroup.setVisible(TargetStrategy.STRATEGY_LOCAL_EXISTING.equals(getTargetStrategy()));

      GridData localGroupData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
      localGroupData.exclude = !TargetStrategy.STRATEGY_LOCAL_EXISTING.equals(getTargetStrategy());
      m_localTargetGroup.setLayoutData(localGroupData);
    }

    //layout
    parent.setLayout(new GridLayout(1, true));
  }

  private File getDefaultRapLocation() {
    File l = ResourceUtility.getEclipseInstallLocation();
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
    m_extractTargetLocationField = new FileSelectionField(group, LABEL_PERCENTAGE);
    m_extractTargetLocationField.setLabelText(Texts.get("RAPTargetLocation"));
    m_extractTargetLocationField.setFolderMode(true);
    m_extractTargetLocationField.setFile(new File(getExtractTargetFolder()));
    m_extractTargetLocationField.addTraverseListener(new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_ARROW_NEXT) {
          File f = getFileFromInputString(m_extractTargetLocationField.getText());
          File existing = new File(getExtractTargetFolder());
          if (f != null && !f.equals(existing)) {
            setExtractTargetFolderInternal(f.getAbsolutePath());
            pingStateChanging();
          }
        }
      }
    });
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

  private File getFileFromInputString(String input) {
    try {
      if (StringUtility.hasText(input)) {
        if (input.contains(InstallTargetPlatformFileOperation.ECLIPSE_HOME_VAR)) {
          input = input.replace(InstallTargetPlatformFileOperation.ECLIPSE_HOME_VAR, ResourceUtility.getEclipseInstallLocation().getAbsolutePath());
        }
        return new File(input);
      }
    }
    catch (Exception e) {
    }
    return new File("");
  }

  private Control createLocalTargetGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(Texts.get("LocalRAPTarget"));
    m_localTargetLocationField = new FileSelectionField(group, LABEL_PERCENTAGE);
    m_localTargetLocationField.setLabelText(Texts.get("RAPTargetLocation"));
    m_localTargetLocationField.setFolderMode(true);
    m_localTargetLocationField.setFile(new File(getLocalTargetFolder()));
    m_localTargetLocationField.addTraverseListener(new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_ARROW_NEXT) {
          File f = getFileFromInputString(m_localTargetLocationField.getText());
          File existing = new File(getLocalTargetFolder());
          if (f != null && !f.equals(existing)) {
            setLocalTargetFolderInternal(f.getAbsolutePath());
            pingStateChanging();
          }
        }
      }
    });
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

  @SuppressWarnings("unchecked")
  @Override
  public void putProperties(PropertyMap properties) {
    Set<String> checkedNodeIds = properties.getProperty(IScoutProjectNewOperation.PROP_PROJECT_CHECKED_NODES, Set.class);
    if (checkedNodeIds != null && checkedNodeIds.contains(CreateUiRapPluginOperation.BUNDLE_ID)) {
      properties.setProperty(AppendRapTargetOperation.PROP_LOCAL_TARGET_FOLDER, getLocalTargetFolder());
      properties.setProperty(AppendRapTargetOperation.PROP_EXTRACT_TARGET_FOLDER, getExtractTargetFolder());
      properties.setProperty(AppendRapTargetOperation.PROP_TARGET_STRATEGY, getTargetStrategy());
    }
  }

  @Override
  public void performHelp() {
    //TODO: remove external link and use eclipse help instead
    ResourceUtility.showUrlInBrowser("https://wiki.eclipse.org/Scout/HowTo/4.0/Create_a_new_project#Step_3_.28Optional.29");
  }

  public void setTargetStrategy(TargetStrategy strategy) {
    try {
      if (strategy == null) {
        strategy = TargetStrategy.STRATEGY_REMOTE;
      }
      setTargetStrategyInternal(strategy);
      if (isControlCreated()) {
        switch (strategy) {
          case STRATEGY_LOCAL_EXISTING:
            if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXISTING)) {
              m_localButton.setSelection(true);
            }
            break;
          case STRATEGY_LOCAL_EXTRACT:
            if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXTRACT)) {
              m_extractButton.setSelection(true);
            }
            break;
          case STRATEGY_REMOTE:
            if (isStrategyOffered(TargetStrategy.STRATEGY_REMOTE)) {
              m_remoteButton.setSelection(true);
            }
            break;
          case STRATEGY_LATER:
            if (isStrategyOffered(TargetStrategy.STRATEGY_LATER)) {
              m_laterButton.setSelection(true);
            }
            break;
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTargetStrategyInternal(TargetStrategy strategy) {
    if (!isStrategyOffered(strategy)) {
      throw new IllegalArgumentException("unsupported strategy: " + strategy);
    }
    setProperty(PROP_TARGET_STRATEGY, strategy);
  }

  public TargetStrategy getTargetStrategy() {
    return (TargetStrategy) getProperty(PROP_TARGET_STRATEGY);
  }

  public String getExtractTargetFolder() {
    return (String) getProperty(PROP_EXTRACT_TARGET_FOLDER);
  }

  public void setExtractTargetFolder(String proposal) {
    try {
      setStateChanging(true);
      setExtractTargetFolderInternal(proposal);
      if (isControlCreated() && m_extractTargetLocationField != null) {
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
      if (isControlCreated() && m_localTargetLocationField != null) {
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

  private boolean isStrategyOffered(TargetStrategy strategy) {
    for (TargetStrategy item : getOfferedTargetStrategies()) {
      if (item.equals(strategy)) {
        return true;
      }
    }
    return false;
  }

  private IStatus getStatusStrategy() {
    if (TargetStrategy.STRATEGY_LOCAL_EXISTING.equals(getTargetStrategy())) {
      if (!StringUtility.hasText(getLocalTargetFolder())) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetLocationDefined"));
      }
      File target = new File(getLocalTargetFolder());
      if (!target.exists() || !new File(target, "plugins").exists()) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetFoundUnder", getLocalTargetFolder()));
      }
      else {
        try {
          // try to find rap-rt, scout-rap-rt and scout-mobile-rt plugins
          File pluginsFolder = new File(new File(getLocalTargetFolder()), "plugins");
          if (pluginsFolder.exists()) {
            String[] pluginNames = pluginsFolder.list(new FilenameFilter() {
              @Override
              public boolean accept(File dir, String name) {
                String n = name.toLowerCase().trim();
                for (String s : RAP_PLUGIN_DETECTION_PREFIXES) {
                  if (n.startsWith(s) && n.endsWith(".jar")) {
                    return true;
                  }
                }
                return false;
              }
            });
            if (pluginNames.length == RAP_PLUGIN_DETECTION_PREFIXES.length) {
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
    else if (TargetStrategy.STRATEGY_LOCAL_EXTRACT.equals(getTargetStrategy())) {
      if (!StringUtility.hasText(getExtractTargetFolder())) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetLocationDefined"));
      }
      File eclipseFolder = ResourceUtility.getEclipseInstallLocation();
      File targetDir = new File(getExtractTargetFolder());
      if (CompareUtility.equals(eclipseFolder, targetDir)) {
        return new Status(IStatus.ERROR, ScoutSdkRapUI.PLUGIN_ID, Texts.get("RAPTargetNotInstallableInPlatform"));
      }
      if (targetDir.exists()) {
        return new Status(IStatus.WARNING, ScoutSdkRapUI.PLUGIN_ID, Texts.get("RAPTargetAlreadyExists"));
      }
      else {
        return new Status(IStatus.INFO, ScoutSdkRapUI.PLUGIN_ID, Texts.get("ANewRAPTargetWillBeCreated", getExtractTargetFolder()));
      }
    }
    else if (TargetStrategy.STRATEGY_REMOTE.equals(getTargetStrategy())) {
      return new Status(IStatus.INFO, ScoutSdkRapUI.PLUGIN_ID, Texts.get("TheRAPTargetWillBeDownloaded"));
    }
    else if (TargetStrategy.STRATEGY_LATER.equals(getTargetStrategy())) {
      return new Status(IStatus.INFO, ScoutSdkRapUI.PLUGIN_ID, Texts.get("NoRAPTargetWillBeSet"));
    }
    return Status.OK_STATUS;
  }

  public TargetStrategy[] getOfferedTargetStrategies() {
    return m_offeredTargetStrategies;
  }

  private void initOfferedTargetStrategies(TargetStrategy[] offeredTargetStrategies) {
    boolean isRapTargetPluginAvailable = Platform.getBundle(IScoutSdkRapConstants.ScoutRapTargetPlugin) != null;

    if (!isRapTargetPluginAvailable) {
      // the rap target plugin is not installed: filter the strategy out if it is in the list
      ArrayList<TargetStrategy> strategies = new ArrayList<TargetStrategy>(offeredTargetStrategies.length);
      for (TargetStrategy s : offeredTargetStrategies) {
        if (!TargetStrategy.STRATEGY_LOCAL_EXTRACT.equals(s)) {
          strategies.add(s);
        }
      }
      offeredTargetStrategies = strategies.toArray(new TargetStrategy[strategies.size()]);
    }

    m_offeredTargetStrategies = offeredTargetStrategies;
  }

  private class P_StrategySelectionListener extends SelectionAdapter {
    private final TargetStrategy m_strategy;

    public P_StrategySelectionListener(TargetStrategy strategy) {
      m_strategy = strategy;
    }

    private void setGroupVisible(Control toBeVisible) {
      if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXISTING)) {
        boolean localVisible = m_localTargetGroup == toBeVisible;
        m_localTargetGroup.setVisible(localVisible);
        ((GridData) m_localTargetGroup.getLayoutData()).exclude = !localVisible;
      }

      if (isStrategyOffered(TargetStrategy.STRATEGY_LOCAL_EXTRACT)) {
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
          setGroupVisible(null);
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
