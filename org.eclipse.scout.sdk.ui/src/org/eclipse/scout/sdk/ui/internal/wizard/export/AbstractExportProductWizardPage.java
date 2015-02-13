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
package org.eclipse.scout.sdk.ui.internal.wizard.export;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.IProductSelectionListener;
import org.eclipse.scout.sdk.ui.fields.ProductSelectionField;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractExportProductWizardPage extends AbstractWorkspaceWizardPage {

  private static final String PROP_PRODUCT_FILE = "productFile";
  private static final String PROP_WAR_FILE_NAME = "warFileName";

  private final IScoutBundle m_scoutProject;
  private final String m_symbolicNameFilter;
  private final String m_settingsProductFile;
  private final String m_settingsWarFileName;

  protected StyledTextField m_warFileName;
  protected ProductSelectionField m_productField;
  protected IStatus m_productStatus = Status.OK_STATUS;

  public AbstractExportProductWizardPage(IScoutBundle scoutProject, String pageName, String title, String symbolicNameFilter, String prodFileSetting, String warFileNameSetting) {
    super(pageName);
    m_scoutProject = scoutProject;
    m_settingsProductFile = prodFileSetting;
    m_settingsWarFileName = warFileNameSetting;
    m_symbolicNameFilter = symbolicNameFilter;
    setTitle(title);
    setDescription(Texts.get("ExportProductDesc"));
  }

  @Override
  protected void createContent(Composite parent) {
    m_warFileName = getFieldToolkit().createStyledTextField(parent, Texts.get("WarFile"));
    m_warFileName.setReadOnlySuffix(".war");
    m_warFileName.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setWarNameInternal(m_warFileName.getText());
        pingStateChanging();
      }
    });
    m_warFileName.setText(getDialogSettings().get(m_settingsWarFileName));

    ITreeNode productTreeRoot = null;
    try {
      productTreeRoot = TreeUtility.createProductTree(getScoutProject(), new DeployableProductFileNodeFilter(m_symbolicNameFilter), false);
    }
    catch (CoreException e1) {
      ScoutSdkUi.logError("unable to create product file list", e1);
    }
    m_productField = new ProductSelectionField(parent, productTreeRoot);
    m_productField.setLabelText(Texts.get("ProductFile"));
    m_productField.addProductSelectionListener(new IProductSelectionListener() {
      @Override
      public void productSelected(IFile productFile) {
        setProductFileInternal(productFile);
        pingStateChanging();
      }
    });
    IFile defaultSelection = getProductFileSetting();
    if (defaultSelection == null) {
      Set<ITreeNode> productNodes = TreeUtility.findNodes(productTreeRoot, NodeFilters.getByType(TreeUtility.TYPE_PRODUCT_NODE));
      if (productNodes.size() == 1) {
        defaultSelection = (IFile) CollectionUtility.firstElement(productNodes).getData();
      }
      else if (productNodes.size() == 0) {
        m_productStatus = new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("WarExportNoServerFound",
            DeployableProductFileNodeFilter.BUNDLE_ID_HTTP_SERVLETBRIDGE,
            DeployableProductFileNodeFilter.BUNDLE_ID_HTTP_REGISTRY));
      }
    }
    m_productField.setProductFile(defaultSelection);
    setProductFileInternal(defaultSelection);

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_productField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_warFileName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    parent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  private IFile getProductFileSetting() {
    String path = getDialogSettings().get(m_settingsProductFile);
    if (!StringUtility.isNullOrEmpty(path)) {
      Path p = new Path(path);
      if (p.segmentCount() > 1) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(p.segment(0));
        if (project != null) {
          IFile productFile = project.getFile(p.removeFirstSegments(1));
          if (productFile != null && productFile.exists()) {
            return productFile;
          }
        }
      }
    }
    return null;
  }

  @Override
  public IExportScoutProjectWizard getWizard() {
    return (IExportScoutProjectWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusProductField());
    multiStatus.add(getStatusWarName());
  }

  protected IStatus getStatusProductField() {
    if (getProductFile() == null || !getProductFile().exists()) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoProductFileSpecified"));
    }
    return m_productStatus;
  }

  protected IStatus getStatusWarName() {
    if (!StringUtility.hasText(m_warFileName.getModifiableText())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoWARFileSpecified"));
    }
    return Status.OK_STATUS;
  }

  /**
   * @return the productFile
   */
  public IFile getProductFile() {
    return (IFile) getProperty(PROP_PRODUCT_FILE);
  }

  /**
   * @param productFile
   *          the productFile to set
   */
  public void setProductFile(IFile productFile) {
    try {
      setStateChanging(true);
      setProductFileInternal(productFile);
      if (isControlCreated()) {
        m_productField.setProductFile(productFile);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setProductFileInternal(IFile productFile) {
    setProperty(PROP_PRODUCT_FILE, productFile);
    String setting = null;
    if (productFile != null) {
      setting = productFile.getFullPath().toString();
    }
    getDialogSettings().put(m_settingsProductFile, setting);
  }

  public IScoutBundle getScoutProject() {
    return m_scoutProject;
  }

  /**
   * @return the warFile
   */
  public String getWarName() {
    return (String) getProperty(PROP_WAR_FILE_NAME);
  }

  /**
   * @param warFile
   *          the warFile to set
   */
  public void setWarName(String name) {
    try {
      setStateChanging(true);
      setWarNameInternal(name);
      if (isControlCreated()) {
        m_warFileName.setText(name);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setWarNameInternal(String name) {
    setProperty(PROP_WAR_FILE_NAME, name);
    getDialogSettings().put(m_settingsWarFileName, name);
  }
}
