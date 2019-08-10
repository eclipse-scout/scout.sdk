/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.form;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eScoutTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link FormNewWizardPage}</h3>
 *
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
    setIsCreateFormDataInternal(Strings.isBlank(prefCreateFormData) || Boolean.parseBoolean(prefCreateFormData));

    String prefCreateService = settings.get(PREF_CREATE_SERVICE);
    setIsCreateServiceInternal(Strings.isBlank(prefCreateService) || Boolean.parseBoolean(prefCreateService));

    String prefCreatePermissions = settings.get(PREF_CREATE_PERMISSIONS);
    setIsCreatePermissionsInternal(Strings.isBlank(prefCreatePermissions) || Boolean.parseBoolean(prefCreatePermissions));

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
    if (!JdtUtils.exists(clientSourceFolder)) {
      return;
    }

    setServerSourceFolder(S2eScoutTier.wrap(ScoutTier.Client).convert(ScoutTier.Server, clientSourceFolder).orElse(null));
    setSharedSourceFolder(S2eScoutTier.wrap(ScoutTier.Client).convert(ScoutTier.Shared, clientSourceFolder).orElse(null));
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    super.validatePage(multiStatus);
    multiStatus.add(getStatusServerSourceFolder());
    multiStatus.add(getStatusSharedSourceFolder());
    multiStatus.add(getStatusVisibility());
  }

  protected IStatus getStatusVisibility() {
    if (!JdtUtils.exists(getSharedSourceFolder())) {
      return Status.OK_STATUS;
    }

    if (JdtUtils.exists(getServerSourceFolder()) && !getServerSourceFolder().getJavaProject().isOnClasspath(getSharedSourceFolder().getJavaProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The Shared Source Folder is not accessible from the selected Server Source Folder.");
    }
    if (JdtUtils.exists(getSourceFolder()) && !getSourceFolder().getJavaProject().isOnClasspath(getSharedSourceFolder().getJavaProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The Shared Source Folder is not accessible from the selected Form Source Folder.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusSharedSourceFolder() {
    if (!isCreateFormData() && !isCreatePermissions() && !isCreateService()) {
      return Status.OK_STATUS;
    }

    if (!JdtUtils.exists(getSharedSourceFolder())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a shared source folder.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusServerSourceFolder() {
    if (isCreateService() && !JdtUtils.exists(getServerSourceFolder())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a server source folder.");
    }
    return Status.OK_STATUS;
  }

  @Override
  protected int getLabelWidth() {
    return 130;
  }

  protected void createFormPropertiesGroup(Composite p) {
    Group parent = FieldToolkit.createGroupBox(p, "Additional Components");

    // create FormData
    m_createFormDataButton = FieldToolkit.createCheckBox(parent, "Create FormData", isCreateFormData());
    m_createFormDataButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreateFormDataInternal(m_createFormDataButton.getSelection());
        handleComponentsChanged();
        pingStateChanging();
      }
    });

    // create Service
    m_createServiceButton = FieldToolkit.createCheckBox(parent, "Create Service", isCreateService());
    m_createServiceButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreateServiceInternal(m_createServiceButton.getSelection());
        handleComponentsChanged();
        pingStateChanging();
      }
    });

    // create permissions
    m_createPermissionsButton = FieldToolkit.createCheckBox(parent, "Create Permissions", isCreatePermissions());
    m_createPermissionsButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreatePermissionsInternal(m_createPermissionsButton.getSelection());
        handleComponentsChanged();
        pingStateChanging();
      }
    });

    // shared source folder
    m_sharedSourceFolder = FieldToolkit.createSourceFolderField(parent, "Shared Source Folder", ScoutTier.Shared, getLabelWidth());
    m_sharedSourceFolder.acceptProposal(getSharedSourceFolder());
    m_sharedSourceFolder.addProposalListener(proposal -> {
      setSharedSourceFolderInternal((IPackageFragmentRoot) proposal);
      pingStateChanging();
    });

    // server source folder
    m_serverSourceFolder = FieldToolkit.createSourceFolderField(parent, "Server Source Folder", ScoutTier.Server, getLabelWidth());
    m_serverSourceFolder.acceptProposal(getServerSourceFolder());
    m_serverSourceFolder.addProposalListener(proposal -> {
      setServerSourceFolderInternal((IPackageFragmentRoot) proposal);
      pingStateChanging();
    });

    // layout
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .indent(0, 10)
        .applyTo(parent);
    GridLayoutFactory
        .swtDefaults()
        .numColumns(3)
        .equalWidth(true)
        .applyTo(parent);
    GridDataFactory optionsButtonGridDataFactory = GridDataFactory
        .defaultsFor(parent)
        .indent(10, 8);
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
        .span(3, 0)
        .applyTo(m_serverSourceFolder);
    optionsButtonGridDataFactory.applyTo(m_createFormDataButton);
    optionsButtonGridDataFactory.applyTo(m_createServiceButton);
    optionsButtonGridDataFactory.applyTo(m_createPermissionsButton);

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
    return getPropertyBool(PROP_CREATE_FORM_DATA, true);
  }

  public void setIsCreateFormData(boolean createFormData) {
    setPropertyWithChangingControl(m_createFormDataButton, () -> setIsCreateFormDataInternal(createFormData), field -> field.setSelection(createFormData));
  }

  protected boolean setIsCreateFormDataInternal(boolean createFormData) {
    return setProperty(PROP_CREATE_FORM_DATA, createFormData);
  }

  public boolean isCreateService() {
    return getPropertyBool(PROP_CREATE_SERVICE, true);
  }

  public void setIsCreateService(boolean createService) {
    setPropertyWithChangingControl(m_createServiceButton, () -> setIsCreateServiceInternal(createService), field -> field.setSelection(createService));
  }

  protected boolean setIsCreateServiceInternal(boolean createService) {
    return setProperty(PROP_CREATE_SERVICE, createService);
  }

  public boolean isCreatePermissions() {
    return getPropertyBool(PROP_CREATE_PERMISSIONS, true);
  }

  public void setIsCreatePermissions(boolean createPermissions) {
    setPropertyWithChangingControl(m_createPermissionsButton, () -> setIsCreatePermissionsInternal(createPermissions), field -> field.setSelection(createPermissions));
  }

  protected boolean setIsCreatePermissionsInternal(boolean createPermissions) {
    return setProperty(PROP_CREATE_PERMISSIONS, createPermissions);
  }

  public IPackageFragmentRoot getSharedSourceFolder() {
    return getProperty(PROP_SHARED_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setSharedSourceFolder(IPackageFragmentRoot sharedSourceFolder) {
    setPropertyWithChangingControl(m_sharedSourceFolder, () -> setSharedSourceFolderInternal(sharedSourceFolder), field -> field.acceptProposal(sharedSourceFolder));
  }

  protected boolean setSharedSourceFolderInternal(IPackageFragmentRoot sharedSourceFolder) {
    return setProperty(PROP_SHARED_SOURCE_FOLDER, sharedSourceFolder);
  }

  public IPackageFragmentRoot getServerSourceFolder() {
    return getProperty(PROP_SERVER_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setServerSourceFolder(IPackageFragmentRoot serverSourceFolder) {
    setPropertyWithChangingControl(m_serverSourceFolder, () -> setServerSourceFolderInternal(serverSourceFolder), field -> field.acceptProposal(serverSourceFolder));
  }

  protected boolean setServerSourceFolderInternal(IPackageFragmentRoot serverSourceFolder) {
    return setProperty(PROP_SERVER_SOURCE_FOLDER, serverSourceFolder);
  }
}
