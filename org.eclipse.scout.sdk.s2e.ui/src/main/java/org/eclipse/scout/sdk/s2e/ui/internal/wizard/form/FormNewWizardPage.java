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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.form;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalListener;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link FormNewWizardPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class FormNewWizardPage extends CompilationUnitNewWizardPage {

  public static final String PROP_CREATE_FORM_DATA = "createFormData";
  public static final String PROP_CREATE_SERVICE = "createService";
  public static final String PROP_CREATE_PERMISSIONS = "createPermissions";
  public static final String PROP_SHARED_SOURCE_FOLDER = "sharedSourceFolder";
  public static final String PROP_SERVER_SOURCE_FOLDER = "serverSourceFolder";

  public static final String PREF_CREATE_FORM_DATA = "createFormData";
  public static final String PREF_CREATE_SERVICE = "createService";
  public static final String PREF_CREATE_PERMISSIONS = "createPermissions";

  protected Button m_createFormDataButton;
  protected Button m_createServiceButton;
  protected Button m_createPermissionsButton;
  protected ProposalTextField m_sharedSourceFolder;
  protected ProposalTextField m_serverSourceFolder;

  public FormNewWizardPage(PackageContainer packageContainer) {
    super(FormNewWizardPage.class.getName(), packageContainer, ISdkProperties.SUFFIX_FORM, IScoutRuntimeTypes.IForm, IScoutRuntimeTypes.AbstractForm, ScoutTier.Client);
    setTitle("Create a new Form");
    setDescription(getTitle());
    setIcuGroupName("New Form Details");
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    initDefaults();

    createFormPropertiesGroup(parent);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_FORM_NEW_WIZARD_PAGE);
  }

  protected void initDefaults() {
    IDialogSettings settings = getDialogSettings();

    String prefCreateFormData = settings.get(PREF_CREATE_FORM_DATA);
    setIsCreateFormDataInternal(StringUtils.isBlank(prefCreateFormData) || Boolean.valueOf(prefCreateFormData));

    String prefCreateService = settings.get(PREF_CREATE_SERVICE);
    setIsCreateServiceInternal(StringUtils.isBlank(prefCreateService) || Boolean.valueOf(prefCreateService));

    String prefCreatePermissions = settings.get(PREF_CREATE_PERMISSIONS);
    setIsCreatePermissionsInternal(StringUtils.isBlank(prefCreatePermissions) || Boolean.valueOf(prefCreatePermissions));

    guessSharedAndServerFolders();
  }

  @Override
  public FormNewWizard getWizard() {
    return (FormNewWizard) super.getWizard();
  }

  @Override
  public boolean performFinish() {
    if (!super.performFinish()) {
      return false;
    }

    getDialogSettings().put(PREF_CREATE_FORM_DATA, Boolean.toString(isCreateFormData()));
    getDialogSettings().put(PREF_CREATE_SERVICE, Boolean.toString(isCreateService()));
    getDialogSettings().put(PREF_CREATE_PERMISSIONS, Boolean.toString(isCreatePermissions()));

    return true;
  }

  protected void guessSharedAndServerFolders() {
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
  protected void validatePage(MultiStatus multiStatus) {
    super.validatePage(multiStatus);
    multiStatus.add(getStatusServerSourceFolder());
    multiStatus.add(getStatusSharedSourceFolder());
  }

  protected IStatus getStatusSharedSourceFolder() {
    if (!isCreateFormData() && !isCreatePermissions() && !isCreateService()) {
      return Status.OK_STATUS;
    }

    if (!S2eUtils.exists(getSharedSourceFolder())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a shared source folder.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusServerSourceFolder() {
    if (isCreateService() && !S2eUtils.exists(getServerSourceFolder())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a server source folder.");
    }
    return Status.OK_STATUS;
  }

  protected void createFormPropertiesGroup(Composite p) {
    Group parent = getFieldToolkit().createGroupBox(p, "Additional Components");
    GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    layoutData.verticalIndent = 10;
    parent.setLayoutData(layoutData);
    parent.setLayout(new GridLayout(3, true));

    // create FormData
    m_createFormDataButton = getFieldToolkit().createCheckBox(parent, "Create FormData", isCreateFormData());
    GridData formDataGridData = new GridData();
    formDataGridData.verticalIndent = 8;
    formDataGridData.horizontalIndent = 10;
    m_createFormDataButton.setLayoutData(formDataGridData);
    m_createFormDataButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreateFormDataInternal(m_createFormDataButton.getSelection());
        handleComponentsChanged();
        pingStateChanging();
      }
    });

    // create Service
    m_createServiceButton = getFieldToolkit().createCheckBox(parent, "Create Service", isCreateService());
    m_createServiceButton.setLayoutData(formDataGridData);
    m_createServiceButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreateServiceInternal(m_createServiceButton.getSelection());
        handleComponentsChanged();
        pingStateChanging();
      }
    });

    // create permissions
    m_createPermissionsButton = getFieldToolkit().createCheckBox(parent, "Create Permissions", isCreatePermissions());
    m_createPermissionsButton.setLayoutData(formDataGridData);
    m_createPermissionsButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreatePermissionsInternal(m_createPermissionsButton.getSelection());
        handleComponentsChanged();
        pingStateChanging();
      }
    });

    // shared source folder
    m_sharedSourceFolder = getFieldToolkit().createSourceFolderTextField(parent, "Shared Source Folder", ScoutTier.Shared, 25);
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
    m_serverSourceFolder = getFieldToolkit().createSourceFolderTextField(parent, "Server Source Folder", ScoutTier.Server, 25);
    GridData serverGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    serverGridData.horizontalSpan = 3;
    m_serverSourceFolder.setLayoutData(serverGridData);
    m_serverSourceFolder.acceptProposal(getServerSourceFolder());
    m_serverSourceFolder.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setServerSourceFolderInternal((IPackageFragmentRoot) proposal);
        pingStateChanging();
      }
    });

    handleComponentsChanged();
  }

  @Override
  protected void handleJavaProjectChanged() {
    super.handleJavaProjectChanged();
    guessSharedAndServerFolders();
  }

  protected void handleComponentsChanged() {
    m_sharedSourceFolder.setEnabled(isCreateFormData() || isCreatePermissions() || isCreateService());
    m_serverSourceFolder.setEnabled(isCreateService());
  }

  public boolean isCreateFormData() {
    Boolean val = getProperty(PROP_CREATE_FORM_DATA, Boolean.class);
    return val != null && val.booleanValue();
  }

  public void setIsCreateFormData(boolean createFormData) {
    try {
      setStateChanging(true);
      setIsCreateFormDataInternal(createFormData);
      if (isControlCreated() && m_createFormDataButton != null) {
        m_createFormDataButton.setSelection(createFormData);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setIsCreateFormDataInternal(boolean createFormData) {
    setProperty(PROP_CREATE_FORM_DATA, createFormData);
  }

  public boolean isCreateService() {
    Boolean val = getProperty(PROP_CREATE_SERVICE, Boolean.class);
    return val != null && val.booleanValue();
  }

  public void setIsCreateService(boolean createService) {
    try {
      setStateChanging(true);
      setIsCreateServiceInternal(createService);
      if (isControlCreated() && m_createServiceButton != null) {
        m_createServiceButton.setSelection(createService);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setIsCreateServiceInternal(boolean createService) {
    setProperty(PROP_CREATE_SERVICE, createService);
  }

  public boolean isCreatePermissions() {
    Boolean val = getProperty(PROP_CREATE_PERMISSIONS, Boolean.class);
    return val != null && val.booleanValue();
  }

  public void setIsCreatePermissions(boolean createPermissions) {
    try {
      setStateChanging(true);
      setIsCreatePermissionsInternal(createPermissions);
      if (isControlCreated() && m_createPermissionsButton != null) {
        m_createPermissionsButton.setSelection(createPermissions);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setIsCreatePermissionsInternal(boolean createPermissions) {
    setProperty(PROP_CREATE_PERMISSIONS, createPermissions);
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
