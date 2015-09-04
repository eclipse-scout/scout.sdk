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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.newproject;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.s2e.ui.fields.file.FileSelectionField;
import org.eclipse.scout.sdk.s2e.ui.fields.file.IFileSelectionListener;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>{@link ScoutProjectNewWizardPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class ScoutProjectNewWizardPage extends AbstractWizardPage {

  public static final String PROP_SYMBOLIC_NAME = "symbName";
  public static final String PROP_DISPLAY_NAME = "dispName";
  public static final String PROP_DIR = "dir";
  public static final String PROP_USE_WORKSPACE_LOC = "useWorkspaceLoc";
  public static final String SETTINGS_TARGET_DIR = "targetDirSetting";

  protected StyledTextField m_symbolicNameField;
  protected StyledTextField m_displayNameField;
  protected Button m_useWsLoc;
  protected FileSelectionField m_targetDirectoryField;

  public ScoutProjectNewWizardPage() {
    super(ScoutProjectNewWizardPage.class.getName());
    setTitle("Create a Scout Project");
    setDescription("Create a new Scout Project");
  }

  @Override
  protected void createContent(Composite parent) {
    initDefaultValues();

    parent.setLayout(new GridLayout(1, true));

    createProjectNameGroup(parent);
    createProjectLocationGroup(parent);
  }

  protected void createProjectNameGroup(Composite parent) {
    Group nameGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
    nameGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    nameGroup.setLayout(new GridLayout(1, true));
    nameGroup.setText("Project Name");

    // symbolic name
    m_symbolicNameField = getFieldToolkit().createStyledTextField(nameGroup, "Symbolic Name");
    m_symbolicNameField.setText(getSymbolicName());
    m_symbolicNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_symbolicNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          setSymbolicNameInternal(m_symbolicNameField.getText());
        }
        finally {
          setStateChanging(false);
        }
      }
    });
    m_symbolicNameField.setSelection(new Point(0, m_symbolicNameField.getText().length()));
    m_symbolicNameField.setFocus();

    // display name
    m_displayNameField = getFieldToolkit().createStyledTextField(nameGroup, "Display Name");
    m_displayNameField.setText(getDisplayName());
    m_displayNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_displayNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setDisplayNameInternal(m_displayNameField.getText());
        pingStateChanging();
      }
    });
  }

  protected void createProjectLocationGroup(Composite parent) {
    Group locationGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
    GridData locatoinGroupData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    locatoinGroupData.verticalIndent = 10;
    locationGroup.setLayoutData(locatoinGroupData);
    locationGroup.setLayout(new GridLayout(1, true));
    locationGroup.setText("Project Location");

    // location checkbox
    createLocationCheckbox(locationGroup);

    // target dir
    m_targetDirectoryField = new FileSelectionField(locationGroup);
    m_targetDirectoryField.setLabelText("Target Directory");
    m_targetDirectoryField.setFile(getTargetDirectory());
    m_targetDirectoryField.setFolderMode(true);
    m_targetDirectoryField.setEnabled(!m_useWsLoc.getSelection());
    m_targetDirectoryField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_targetDirectoryField.addFileSelectionListener(new IFileSelectionListener() {
      @Override
      public void fileSelected(File file) {
        setTargetDirectoryInternal(file);
        pingStateChanging();
      }
    });
  }

  protected Composite createLocationCheckbox(Composite p) {
    Composite parent = new Composite(p, SWT.NONE);
    Label lbl = new Label(parent, SWT.NONE);

    // layout
    parent.setLayout(new FormLayout());
    parent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 4);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(TextField.DEFAULT_LABEL_PERCENTAGE, 0);
    labelData.bottom = new FormAttachment(100, 0);
    lbl.setLayoutData(labelData);

    m_useWsLoc = new Button(parent, SWT.CHECK);
    m_useWsLoc.setText("Use default Workspace location");
    m_useWsLoc.setSelection(isUseWorkspaceLocation());
    m_useWsLoc.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_targetDirectoryField.setEnabled(!m_useWsLoc.getSelection());
        setUseWorkspaceLocationInternal(m_useWsLoc.getSelection());
        pingStateChanging();
      }
    });

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(lbl, 5);
    textData.right = new FormAttachment(100, 0);
    textData.bottom = new FormAttachment(100, 0);
    m_useWsLoc.setLayoutData(textData);
    return parent;
  }

  protected void initDefaultValues() {
    // symbolic name
    setSymbolicNameInternal("org.eclipse.scout.myapp");

    // display name
    setDisplayNameInternal("My Application");

    // use workspace loc
    setUseWorkspaceLocationInternal(true);

    // target directory
    File file = null;
    String recentTargetDir = getDialogSettings().get(SETTINGS_TARGET_DIR);
    if (recentTargetDir != null) {
      file = new File(recentTargetDir);
    }
    else {
      file = getWorkspaceLocation();
    }
    setTargetDirectoryInternal(file);
  }

  public static File getWorkspaceLocation() {
    return ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsoluteFile();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusSymbolicName());
    multiStatus.add(getStatusDisplayName());
    multiStatus.add(getStatusTargetDirectory());
  }

  protected IStatus getStatusSymbolicName() {
    String msg = ScoutProjectNewHelper.getSymbolicNameErrorMessage(getSymbolicName());
    if (msg != null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, msg);
    }
    for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      if (p.getName().startsWith(getSymbolicName() + ".")) {
        return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "Projects with this Symbolic Name already exist.");
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusDisplayName() {
    String msg = ScoutProjectNewHelper.getDisplayNameErrorMEssage(getDisplayName());
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

  public String getSymbolicName() {
    return getPropertyString(PROP_SYMBOLIC_NAME);
  }

  public void setSymbolicName(String s) {
    try {
      setStateChanging(true);
      setSymbolicNameInternal(s);
      if (isControlCreated()) {
        m_symbolicNameField.setText(s);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setSymbolicNameInternal(String s) {
    setPropertyString(PROP_SYMBOLIC_NAME, s);
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
}
