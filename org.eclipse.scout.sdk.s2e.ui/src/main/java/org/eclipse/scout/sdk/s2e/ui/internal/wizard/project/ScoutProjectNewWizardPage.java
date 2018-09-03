/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.project;

import java.io.File;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.resource.IResourceChangedListener;
import org.eclipse.scout.sdk.s2e.ui.fields.resource.ResourceTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link ScoutProjectNewWizardPage}</h3>
 *
 * @author Matthias Villiger
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

    int labelWidth = 100;
    createProjectNameGroup(parent, labelWidth);
    createClientLanguageGroup(parent);
    createProjectLocationGroup(parent, labelWidth);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_PROJECT_NEW_WIZARD_PAGE);
  }

  protected void createProjectNameGroup(Composite parent, int labelWidth) {
    Group nameGroup = getFieldToolkit().createGroupBox(parent, "Project Name");

    // group id
    m_groupIdField = getFieldToolkit().createStyledTextField(nameGroup, "Group Id", TextField.TYPE_LABEL, labelWidth);
    m_groupIdField.setText(getGroupId());
    m_groupIdField.setSelection(new Point(0, m_groupIdField.getText().length()));
    m_groupIdField.setFocus();
    m_groupIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          setGroupIdInternal(m_groupIdField.getText());
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // artifact id
    m_artifactIdField = getFieldToolkit().createStyledTextField(nameGroup, "Artifact Id", TextField.TYPE_LABEL, labelWidth);
    m_artifactIdField.setText(getArtifactId());
    m_artifactIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          setArtifactIdInternal(m_artifactIdField.getText());
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // display name
    m_displayNameField = getFieldToolkit().createStyledTextField(nameGroup, "Display Name", TextField.TYPE_LABEL, labelWidth);
    m_displayNameField.setText(getDisplayName());
    m_displayNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setDisplayNameInternal(m_displayNameField.getText());
        pingStateChanging();
      }
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

    Group uiLangBox = getFieldToolkit().createGroupBox(parent, "Programming language of the user interface");

    m_javaScriptButton = new Button(uiLangBox, SWT.RADIO);
    m_javaScriptButton.setText("JavaScript && JSON");
    m_javaScriptButton.setSelection(isUseJsClient());
    m_javaScriptButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setUseJsClientInternal(m_javaScriptButton.getSelection());
        pingStateChanging();
      }
    });

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
        .defaultsFor(m_javaScriptButton)
        .indent(13, 5)
        .applyTo(m_javaScriptButton);
    GridDataFactory
        .defaultsFor(m_javaButton)
        .indent(13, 2)
        .applyTo(m_javaButton);
  }

  protected void createProjectLocationGroup(Composite parent, int labelWidth) {
    Group locationGroup = getFieldToolkit().createGroupBox(parent, "Project Location");

    // location checkbox
    createLocationCheckbox(locationGroup);

    // target dir
    m_targetDirectoryField = getFieldToolkit().createResourceField(locationGroup, "Target Directory", TextField.TYPE_LABEL, labelWidth);
    m_targetDirectoryField.setFile(getTargetDirectory());
    m_targetDirectoryField.setFolderMode(true);
    m_targetDirectoryField.setEnabled(!m_useWsLoc.getSelection());
    m_targetDirectoryField.addResourceChangedListener(new IResourceChangedListener() {
      @Override
      public void resourceChanged(URL newUrl, File newFile) {
        setTargetDirectoryInternal(newFile);
        pingStateChanging();
      }
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
    Composite parent = new Composite(p, SWT.NONE);
    Label lbl = new Label(parent, SWT.NONE);

    m_useWsLoc = getFieldToolkit().createCheckBox(parent, "Use default Workspace location", isUseWorkspaceLocation());
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

    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 4);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(0, 10);
    labelData.bottom = new FormAttachment(100, 0);
    lbl.setLayoutData(labelData);

    FormData textData = new FormData();
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
    setUseJsClientInternal(true);

    // use workspace loc
    setUseWorkspaceLocationInternal(true);

    // target directory
    updateTargetDirViewState();
  }

  protected void updateTargetDirViewState() {
    if (isControlCreated()) {
      m_targetDirectoryField.setEnabled(!isUseWorkspaceLocation());
    }

    File file = null;
    if (isUseWorkspaceLocation()) {
      file = getWorkspaceLocation();
    }
    else {
      File wsLoc = getWorkspaceLocation();
      if (getTargetDirectory() == null || wsLoc.equals(getTargetDirectory())) {
        String recentTargetDir = getDialogSettings().get(SETTINGS_TARGET_DIR);
        if (recentTargetDir != null) {
          file = new File(recentTargetDir);
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

  public static File getWorkspaceLocation() {
    return ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsoluteFile();
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
    String msg = ScoutProjectNewHelper.getMavenGroupIdErrorMessage(getGroupId());
    if (msg != null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, msg);
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusArtifactId() {
    // check name pattern
    String artifactId = getArtifactId();
    String msg = ScoutProjectNewHelper.getMavenArtifactIdErrorMessage(artifactId);
    if (msg != null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, msg);
    }

    // do not allow a dot in the artifact name (required for ScoutJS archetype)
    if (artifactId.indexOf('.') >= 0) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "The ArtifactId may not contain a dot.");
    }

    // check folder existence on file system
    File folder = null;
    if (isUseWorkspaceLocation()) {
      folder = getWorkspaceLocation();
    }
    else {
      folder = getTargetDirectory();
    }
    if (folder != null && new File(folder, artifactId).exists()) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "A project with this Artifact Id already exists in this target directory.");
    }

    // check project existence in workspace
    for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      if (p.getName().startsWith(artifactId + '.')) {
        return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "A project with this Artifact Id already exists in the workspace.");
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusDisplayName() {
    String msg = ScoutProjectNewHelper.getDisplayNameErrorMessage(getDisplayName());
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
    try {
      setStateChanging(true);
      setDisplayNameInternal(s);
      if (isControlCreated()) {
        m_displayNameField.setText(s);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setDisplayNameInternal(String s) {
    setPropertyString(PROP_DISPLAY_NAME, s);
  }

  public String getGroupId() {
    return getPropertyString(PROP_GROUP_ID);
  }

  public void setGroupId(String s) {
    try {
      setStateChanging(true);
      setGroupIdInternal(s);
      if (isControlCreated()) {
        m_groupIdField.setText(s);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setGroupIdInternal(String s) {
    setPropertyString(PROP_GROUP_ID, s);
  }

  public String getArtifactId() {
    return getPropertyString(PROP_ARTIFACT_ID);
  }

  public void setArtifactId(String s) {
    try {
      setStateChanging(true);
      setArtifactIdInternal(s);
      if (isControlCreated()) {
        m_artifactIdField.setText(s);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setArtifactIdInternal(String s) {
    setPropertyString(PROP_ARTIFACT_ID, s);
  }

  public boolean isUseWorkspaceLocation() {
    return getPropertyBool(PROP_USE_WORKSPACE_LOC);
  }

  public void setUseWorkspaceLocation(boolean f) {
    try {
      setStateChanging(true);
      setUseWorkspaceLocationInternal(f);
      if (isControlCreated()) {
        m_useWsLoc.setSelection(f);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setUseWorkspaceLocationInternal(boolean f) {
    setPropertyBool(PROP_USE_WORKSPACE_LOC, f);
  }

  public File getTargetDirectory() {
    return getProperty(PROP_DIR, File.class);
  }

  public void setTargetDirectory(File f) {
    try {
      setStateChanging(true);
      setTargetDirectoryInternal(f);
      if (isControlCreated()) {
        m_targetDirectoryField.setFile(f);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTargetDirectoryInternal(File f) {
    setProperty(PROP_DIR, f);
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
