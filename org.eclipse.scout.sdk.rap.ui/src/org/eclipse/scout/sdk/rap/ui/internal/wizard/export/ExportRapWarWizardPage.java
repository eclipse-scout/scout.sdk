/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.rap.ui.internal.wizard.export;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.export.ExportServerWarOperation;
import org.eclipse.scout.sdk.rap.ui.internal.extensions.UiRapBundleNodeFactory;
import org.eclipse.scout.sdk.ui.fields.FileSelectionField;
import org.eclipse.scout.sdk.ui.fields.IFileSelectionListener;
import org.eclipse.scout.sdk.ui.fields.IProductSelectionListener;
import org.eclipse.scout.sdk.ui.fields.ProductSelectionField;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.DeployableProductFileNodeFilter;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link ExportRapWarWizardPage}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 06.02.2012
 */
public class ExportRapWarWizardPage extends AbstractWorkspaceWizardPage {

  private final static String SETTINGS_WAR_FILE = "warFileSetting";
  private final static String SETTINGS_OVERWRITE_WAR = "warOverwriteSetting";

  private final static String PROP_PRODUCT_FILE_SERVER = "serverProductFile";
  private final static String PROP_WAR_FILE = "warFile";
  private final static String PROP_OVERWRITE_EXISTING_WAR = "overwriteExistingWar";

  private ProductSelectionField m_serverProductField;
  private FileSelectionField m_warFileField;
  private Button m_overwriteButton;

  private IStatus m_serverProductStatus = Status.OK_STATUS;

  private final IScoutProject m_scoutProject;

  public ExportRapWarWizardPage(IScoutProject scoutProject) {
    super(ExportRapWarWizardPage.class.getName());
    m_scoutProject = scoutProject;
    setTitle(Texts.get("ExportRapWebArchive"));
    setDescription(Texts.get("ExportRapWebArchiveMessage"));
  }

  @Override
  protected void createContent(Composite parent) {
    ITreeNode serverProductTreeRoot = TreeUtility.createProductTree(getScoutProject(), new DeployableProductFileNodeFilter(UiRapBundleNodeFactory.BUNDLE_UI_RAP), false);
    m_serverProductField = new ProductSelectionField(parent, serverProductTreeRoot);
    m_serverProductField.setLabelText(Texts.get("ProductFile"));
    m_serverProductField.addProductSelectionListener(new IProductSelectionListener() {
      @Override
      public void productSelected(IFile productFile) {
        setServerProductFileInternal(productFile);
        pingStateChanging();
      }
    });
    ITreeNode[] rapUiProductNodes = TreeUtility.findNodes(serverProductTreeRoot, NodeFilters.getByType(TreeUtility.TYPE_PRODUCT_NODE));
    if (rapUiProductNodes.length == 1) {
      IFile pf = (IFile) rapUiProductNodes[0].getData();
      setServerProductFileInternal(pf);
      m_serverProductField.setProductFile(pf);
    }
    else if (rapUiProductNodes.length == 0) {
      m_serverProductStatus = new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("WarExportNoRapUiFound",
          DeployableProductFileNodeFilter.BUNDLE_ID_HTTP_SERVLETBRIDGE,
          DeployableProductFileNodeFilter.BUNDLE_ID_HTTP_REGISTRY));
    }

    m_warFileField = new FileSelectionField(parent);
    m_warFileField.setLabelText(Texts.get("WarFile"));
    m_warFileField.setFilterExtensions(new String[]{"*.war"});
    m_warFileField.setFileName("web");
    String warFile = getDialogSettings().get(SETTINGS_WAR_FILE);
    if (!StringUtility.isNullOrEmpty(warFile)) {
      File dir = new File(warFile);
      setWarFileInternal(dir);
      m_warFileField.setFile(dir);
    }

    m_warFileField.addProductSelectionListener(new IFileSelectionListener() {
      @Override
      public void fileSelected(File file) {
        String fileName = "";
        if (file != null) {
          fileName = file.getAbsolutePath();
        }
        getDialogSettings().put(SETTINGS_WAR_FILE, fileName);
        setWarFileInternal(file);
        pingStateChanging();
      }
    });

    m_overwriteButton = new Button(parent, SWT.CHECK);
    m_overwriteButton.setText(Texts.get("OverwriteExistingWarFile"));
    boolean initialSelection = getDialogSettings().getBoolean(SETTINGS_OVERWRITE_WAR);
    setOverwriteExistingWarFileInternal(initialSelection);
    m_overwriteButton.setSelection(initialSelection);
    m_overwriteButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getDialogSettings().put(SETTINGS_OVERWRITE_WAR, m_overwriteButton.getSelection());
        setOverwriteExistingWarFileInternal(m_overwriteButton.getSelection());
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_serverProductField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_warFileField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_overwriteButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    ExportServerWarOperation op = new ExportServerWarOperation(getServerProductFile());
    op.setWarFileName(getWarFile().getAbsolutePath());
    OperationJob job = new OperationJob(op);
    job.schedule();
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusServerProductField());
      multiStatus.add(getStatusWarField());
      multiStatus.add(getStatusOverwriteWarField());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate fields.", e);
    }
  }

  protected IStatus getStatusServerProductField() throws JavaModelException {
    if (getServerProductFile() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoRapProductFileSpecified"));
    }
    return m_serverProductStatus;
  }

  protected IStatus getStatusWarField() throws JavaModelException {
    if (getWarFile() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoWARFileSpecified"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusOverwriteWarField() throws JavaModelException {
    if (!isOverwriteExistingWarFile()) {
      if (getWarFile() != null && getWarFile().exists()) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("WARFileAlreadyExists"));
      }
    }
    return Status.OK_STATUS;
  }

  public IScoutProject getScoutProject() {
    return m_scoutProject;
  }

  /**
   * @return the productFile
   */
  public IFile getServerProductFile() {
    return (IFile) getProperty(PROP_PRODUCT_FILE_SERVER);
  }

  /**
   * @param productFile
   *          the productFile to set
   */
  public void setServerProductFile(IFile productFile) {
    try {
      setStateChanging(true);
      setServerProductFileInternal(productFile);
      if (isControlCreated()) {
        m_serverProductField.setProductFile(productFile);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public void setServerProductFileInternal(IFile productFile) {
    setProperty(PROP_PRODUCT_FILE_SERVER, productFile);
  }

  /**
   * @return the warFile
   */
  public File getWarFile() {
    return (File) getProperty(PROP_WAR_FILE);
  }

  /**
   * @param warFile
   *          the warFile to set
   */
  public void setWarFile(File warFile) {
    try {
      setStateChanging(true);
      setWarFileInternal(warFile);
      if (isControlCreated()) {
        m_warFileField.setFile(warFile);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setWarFileInternal(File warFile) {
    setProperty(PROP_WAR_FILE, warFile);
  }

  /**
   * @return the overwriteExistingWarFile
   */
  public boolean isOverwriteExistingWarFile() {
    return getPropertyBool(PROP_OVERWRITE_EXISTING_WAR);
  }

  /**
   * @param overwriteExistingWarFile
   *          the overwriteExistingWarFile to set
   */
  public void setOverwriteExistingWarFile(boolean overwriteExistingWarFile) {
    try {
      setStateChanging(true);
      setOverwriteExistingWarFileInternal(overwriteExistingWarFile);
      if (isControlCreated()) {
        m_overwriteButton.setSelection(overwriteExistingWarFile);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setOverwriteExistingWarFileInternal(boolean overwriteExistingWarFile) {
    setPropertyBool(PROP_OVERWRITE_EXISTING_WAR, overwriteExistingWarFile);
  }
}
