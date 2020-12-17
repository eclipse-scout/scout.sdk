/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.resource.ResourceTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link ScoutProjectNewWizardPage}</h3>
 *
 * @since 5.1.0
 */
public class ScoutProjectNewWizardPage extends AbstractWizardPage {

  public static final String PROP_GROUP_ID = "groupId";
  public static final String PROP_ARTIFACT_ID = "artifactId";
  public static final String PROP_DISPLAY_NAME = "dispName";
  public static final String PROP_DIR = "dir";
  public static final String PROP_USE_WORKSPACE_LOC = "useWorkspaceLoc";
  public static final String PROP_USE_JS_CLIENT = "useJsClient";
  public static final String SETTINGS_TARGET_DIR = "targetDirSetting";

  protected StyledTextField m_groupIdField;
  protected StyledTextField m_artifactIdField;
  protected StyledTextField m_displayNameField;

  protected Button m_javaScriptButton;
  protected Button m_javaButton;

  protected Button m_useWsLoc;
  protected ResourceTextField m_targetDirectoryField;

  public ScoutProjectNewWizardPage() {
    super(ScoutProjectNewWizardPage.class.getName());
    setTitle("Create a Scout Project");
    setDescription("Create a new Scout Project");
    initDefaultValues();
  }

  @Override
  protected void createContent(Composite parent) {
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);

    var labelWidth = 100;
    createProjectNameGroup(parent, labelWidth);
    createClientLanguageGroup(parent);
    createProjectLocationGroup(parent, labelWidth);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_PROJECT_NEW_WIZARD_PAGE);
  }

  protected void createProjectNameGroup(Composite parent, int labelWidth) {
    var nameGroup = FieldToolkit.createGroupBox(parent, "Project Name");

    // group id
    m_groupIdField = FieldToolkit.createStyledTextField(nameGroup, "Group Id", TextField.TYPE_LABEL, labelWidth);
    m_groupIdField.setText(getGroupId());
    m_groupIdField.setSelection(new Point(0, m_groupIdField.getText().length()));
    m_groupIdField.setFocus();
    m_groupIdField.addModifyListener(e -> {
      setGroupIdInternal(m_groupIdField.getText());
      pingStateChanging();
    });

    // artifact id
    m_artifactIdField = FieldToolkit.createStyledTextField(nameGroup, "Artifact Id", TextField.TYPE_LABEL, labelWidth);
    m_artifactIdField.setText(getArtifactId());
    m_artifactIdField.addModifyListener(e -> {
      setArtifactIdInternal(m_artifactIdField.getText());
      pingStateChanging();
    });

    // display name
    m_displayNameField = FieldToolkit.createStyledTextField(nameGroup, "Display Name", TextField.TYPE_LABEL, labelWidth);
    m_displayNameField.setText(getDisplayName());
    m_displayNameField.addModifyListener(e -> {
      setDisplayNameInternal(m_displayNameField.getText());
      pingStateChanging();
    });

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(nameGroup);
    GridDataFactory
        .defaultsFor(nameGroup)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(nameGroup);
    GridDataFactory
        .defaultsFor(m_groupIdField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_groupIdField);
    GridDataFactory
        .defaultsFor(m_artifactIdField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_artifactIdField);
    GridDataFactory
        .defaultsFor(m_displayNameField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_displayNameField);
  }

  protected void createClientLanguageGroup(Composite parent) {

    var uiLangBox = FieldToolkit.createGroupBox(parent, "Programming language of the user interface");

    m_javaButton = new Button(uiLangBox, SWT.RADIO);
    m_javaButton.setText("Java");
    m_javaButton.setSelection(!isUseJsClient());
    m_javaButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setUseJsClientInternal(!m_javaButton.getSelection());
        pingStateChanging();
      }
    });

    m_javaScriptButton = new Button(uiLangBox, SWT.RADIO);
    m_javaScriptButton.setText("JavaScript");
    m_javaScriptButton.setSelection(isUseJsClient());
    m_javaScriptButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setUseJsClientInternal(m_javaScriptButton.getSelection());
        pingStateChanging();
      }
    });

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(uiLangBox);
    GridDataFactory
        .defaultsFor(uiLangBox)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .indent(0, 10)
        .applyTo(uiLangBox);
    GridDataFactory
        .defaultsFor(m_javaButton)
        .indent(13, 2)
        .applyTo(m_javaButton);
    GridDataFactory
        .defaultsFor(m_javaScriptButton)
        .indent(13, 5)
        .applyTo(m_javaScriptButton);
  }

  protected void createProjectLocationGroup(Composite parent, int labelWidth) {
    var locationGroup = FieldToolkit.createGroupBox(parent, "Project Location");

    // location checkbox
    createLocationCheckbox(locationGroup);

    // target dir
    m_targetDirectoryField = FieldToolkit.createResourceField(locationGroup, "Target Directory", TextField.TYPE_LABEL, labelWidth);
    m_targetDirectoryField.setFile(getTargetDirectory());
    m_targetDirectoryField.setFolderMode(true);
    m_targetDirectoryField.setEnabled(!m_useWsLoc.getSelection());
    m_targetDirectoryField.addResourceChangedListener((newUrl, newFile) -> {
      setTargetDirectoryInternal(newFile);
      pingStateChanging();
    });

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(locationGroup);
    GridDataFactory
        .defaultsFor(locationGroup)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .indent(0, 10)
        .applyTo(locationGroup);
    GridDataFactory
        .defaultsFor(m_targetDirectoryField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_targetDirectoryField);
  }

  protected Composite createLocationCheckbox(Composite p) {
    var parent = new Composite(p, SWT.NONE);
    var lbl = new Label(parent, SWT.NONE);

    m_useWsLoc = FieldToolkit.createCheckBox(parent, "Use default Workspace location", isUseWorkspaceLocation());
    m_useWsLoc.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setUseWorkspaceLocationInternal(m_useWsLoc.getSelection());
        updateTargetDirViewState();
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new FormLayout());
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(parent);

    var labelData = new FormData();
    labelData.top = new FormAttachment(0, 4);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(0, 10);
    labelData.bottom = new FormAttachment(100, 0);
    lbl.setLayoutData(labelData);

    var textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(lbl, 5);
    textData.right = new FormAttachment(100, 0);
    textData.bottom = new FormAttachment(100, 0);
    m_useWsLoc.setLayoutData(textData);

    return parent;
  }

  protected void initDefaultValues() {
    // group id
    setGroupIdInternal("org.eclipse.scout.apps");

    // artifact id
    setArtifactIdInternal("helloworld");

    // display name
    setDisplayNameInternal("My Application");

    // ui language
    setUseJsClientInternal(false);

    // use workspace loc
    setUseWorkspaceLocationInternal(true);

    // target directory
    updateTargetDirViewState();
  }

  protected void updateTargetDirViewState() {
    if (isControlCreated()) {
      m_targetDirectoryField.setEnabled(!isUseWorkspaceLocation());
    }

    Path file = null;
    if (isUseWorkspaceLocation()) {
      file = getWorkspaceLocation();
    }
    else {
      var wsLoc = getWorkspaceLocation();
      if (getTargetDirectory() == null || wsLoc.equals(getTargetDirectory())) {
        var recentTargetDir = getDialogSettings().get(SETTINGS_TARGET_DIR);
        if (recentTargetDir != null) {
          file = Paths.get(recentTargetDir);
        }
        else {
          file = wsLoc;
        }
      }
    }

    if (file != null) {
      setTargetDirectory(file);
    }
  }

  public static Path getWorkspaceLocation() {
    return ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsoluteFile().toPath();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusGroupId());
    multiStatus.add(getStatusArtifactId());
    multiStatus.add(getStatusDisplayName());
    multiStatus.add(getStatusTargetDirectory());
  }

  protected IStatus getStatusGroupId() {
    // check name pattern
    var msg = ScoutProjectNewHelper.getMavenGroupIdErrorMessage(getGroupId());
    if (msg != null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, msg);
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusArtifactId() {
    // check name pattern
    var artifactId = getArtifactId();
    var msg = ScoutProjectNewHelper.getMavenArtifactIdErrorMessage(artifactId);
    if (msg != null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, msg);
    }

    // check folder existence on file system
    Path folder;
    if (isUseWorkspaceLocation()) {
      folder = getWorkspaceLocation();
    }
    else {
      folder = getTargetDirectory();
    }
    if (folder != null && Files.exists(folder.resolve(getArtifactId()))) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "A project with this Artifact Id already exists in this target directory.");
    }

    // check project existence in workspace
    for (var p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      if (p.getName().startsWith(artifactId + '.')) {
        return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "A project with this Artifact Id already exists in the workspace.");
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusDisplayName() {
    var msg = ScoutProjectNewHelper.getDisplayNameErrorMessage(getDisplayName());
    if (msg != null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, msg);
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusTargetDirectory() {
    if (isUseWorkspaceLocation()) {
      return Status.OK_STATUS;
    }
    if (getTargetDirectory() == null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "Please choose a target directory.");
    }
    return Status.OK_STATUS;
  }

  public String getDisplayName() {
    return getPropertyString(PROP_DISPLAY_NAME);
  }

  public void setDisplayName(String s) {
    setPropertyWithChangingControl(m_displayNameField, () -> setDisplayNameInternal(s), field -> field.setText(s));
  }

  protected boolean setDisplayNameInternal(String s) {
    return setPropertyString(PROP_DISPLAY_NAME, s);
  }

  public String getGroupId() {
    return getPropertyString(PROP_GROUP_ID);
  }

  public void setGroupId(String s) {
    setPropertyWithChangingControl(m_groupIdField, () -> setGroupIdInternal(s), field -> field.setText(s));
  }

  protected boolean setGroupIdInternal(String s) {
    return setPropertyString(PROP_GROUP_ID, s);
  }

  public String getArtifactId() {
    return getPropertyString(PROP_ARTIFACT_ID);
  }

  public void setArtifactId(String s) {
    setPropertyWithChangingControl(m_artifactIdField, () -> setArtifactIdInternal(s), field -> field.setText(s));
  }

  protected boolean setArtifactIdInternal(String s) {
    return setPropertyString(PROP_ARTIFACT_ID, s);
  }

  public boolean isUseWorkspaceLocation() {
    return getPropertyBool(PROP_USE_WORKSPACE_LOC);
  }

  public void setUseWorkspaceLocation(boolean f) {
    setPropertyWithChangingControl(m_useWsLoc, () -> setUseWorkspaceLocationInternal(f), field -> field.setSelection(f));
  }

  protected boolean setUseWorkspaceLocationInternal(boolean f) {
    return setPropertyBool(PROP_USE_WORKSPACE_LOC, f);
  }

  public Path getTargetDirectory() {
    return getProperty(PROP_DIR, Path.class);
  }

  public void setTargetDirectory(Path f) {
    setPropertyWithChangingControl(m_targetDirectoryField, () -> setTargetDirectoryInternal(f), field -> field.setFile(f));
  }

  protected boolean setTargetDirectoryInternal(Path f) {
    return setProperty(PROP_DIR, f);
  }

  public boolean isUseJsClient() {
    return getPropertyBool(PROP_USE_JS_CLIENT);
  }

  public void setUseJsClient(boolean isUseJsClient) {
    try {
      setStateChanging(true);
      setUseJsClientInternal(isUseJsClient);
      if (isControlCreated()) {
        m_javaButton.setSelection(!isUseJsClient);
        m_javaScriptButton.setSelection(isUseJsClient);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setUseJsClientInternal(boolean isUseJsClient) {
    setPropertyBool(PROP_USE_JS_CLIENT, isUseJsClient);
  }
}
