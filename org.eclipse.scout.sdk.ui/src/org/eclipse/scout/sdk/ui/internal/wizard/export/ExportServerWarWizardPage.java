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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.export.ExportServerWarOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.FileSelectionField;
import org.eclipse.scout.sdk.ui.fields.IFileSelectionListener;
import org.eclipse.scout.sdk.ui.fields.IFolderSelectedListener;
import org.eclipse.scout.sdk.ui.fields.IProductSelectionListener;
import org.eclipse.scout.sdk.ui.fields.ProductSelectionField;
import org.eclipse.scout.sdk.ui.fields.ResourceServletFolderSelectionField;
import org.eclipse.scout.sdk.ui.fields.ResourceServletFolderTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>BooleanFieldNewWizardPage</h3> ...
 */
public class ExportServerWarWizardPage extends AbstractWorkspaceWizardPage {
  private final String SETTINGS_WAR_FILE = "warFile";
  private final String SETTINGS_WAR_FILE_NAME = "warFileName";
  private final String SETTINGS_OVERWRITE_WAR = "warOverwrite";
  private final String SETTINGS_INCLUDE_CLIENT = "includeClientExport";

  static final String PROP_PRODUCT_FILE_SERVER = "serverProductFile";
  static final String PROP_PRODUCT_FILE_CLIENT = "clientProductFile";
  static final String PROP_WAR_FILE = "warFile";
  static final String PROP_OVERWRITE_EXISTING_WAR = "overwriteExistingWar";
  static final String PROP_INCLUDE_CLIENT_APPLICATION = "includeClientApplication";
  static final String PROP_CLIENT_EXPORT_FOLDER = "clientExportFolder";
  private final IScoutProject m_scoutProject;

  // process members

  private FileSelectionField m_warFileField;
  private ProductSelectionField m_serverProductField;
  private Button m_overwriteButton;
  private ProductSelectionField m_clientProductField;
  private ResourceServletFolderSelectionField m_resourceFolderField;
  private IStatus m_serverProductStatus = Status.OK_STATUS;
  private IStatus m_clientProductStatus = Status.OK_STATUS;
  private IStatus m_clientExportFolderStatus = Status.OK_STATUS;
  private Button m_includeClientButton;

  public ExportServerWarWizardPage(IScoutProject scoutProject) {
    super(ExportServerWarWizardPage.class.getName());
    m_scoutProject = scoutProject;

    setTitle("Export war file.");
    setDefaultMessage("To export a product as a war file.\n" +
        "The export can be done directly into the webapps folder of a web server.");
  }

  @Override
  protected void createContent(Composite parent) {
    ITreeNode serverProductTreeRoot = TreeUtility.createProductTree(getScoutProject(), new P_ServerProductFilter(), false);
    m_serverProductField = new ProductSelectionField(parent, serverProductTreeRoot);
    m_serverProductField.setLabelText("Product file");
    m_serverProductField.addProductSelectionListener(new IProductSelectionListener() {

      @Override
      public void productSelected(IFile productFile) {
        setServerProductFileInternal(productFile);
        pingStateChanging();
      }
    });
    ITreeNode[] serverProductNodes = TreeUtility.findNodes(serverProductTreeRoot, NodeFilters.getByType(TreeUtility.TYPE_PRODUCT_NODE));
    if (serverProductNodes.length == 1) {
      IFile pf = (IFile) serverProductNodes[0].getData();
      setServerProductFileInternal(pf);
      m_serverProductField.setProductFile(pf);
    }
    else if (serverProductNodes.length == 0) {
      m_serverProductStatus = new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "No server product to deploy on a webserver available.\n" +
          "A deployable server product must contain the bundles:\n" +
          "- " + ScoutSdkUtility.BUNDLE_ID_HTTP_SERVLETBRIDGE +
          "- " + ScoutSdkUtility.BUNDLE_ID_HTTP_REGISTRY);
    }

    m_warFileField = new FileSelectionField(parent);
    m_warFileField.setLabelText("war file");
    m_warFileField.setFilterExtensions(new String[]{"*.war"});
    m_warFileField.setFileName(findBestFitWarName());
    String warFile = getDialogSettings().get(SETTINGS_WAR_FILE);
    if (!StringUtility.isNullOrEmpty(warFile)) {
      setWarFileInternal(new File(warFile));
      m_warFileField.setFile(getWarFile());
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
    m_overwriteButton.setText("Overwrite existing war file");
    boolean initialSelection = getDialogSettings().getBoolean(SETTINGS_OVERWRITE_WAR);
    setOverwriteExistingWarFileInternal(initialSelection);
    m_overwriteButton.setSelection(initialSelection);
    m_overwriteButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        getDialogSettings().put(SETTINGS_OVERWRITE_WAR, m_overwriteButton.getSelection());
        setOverwriteExistingWarFileInternal(m_overwriteButton.getSelection());
        pingStateChanging();
      }
    });

    Control includeClientGroup = createIncludeClientBox(parent);

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_serverProductField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_warFileField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_overwriteButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    includeClientGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  protected Control createIncludeClientBox(Composite parent) {
    ResourceServletFolderTree tree = new ResourceServletFolderTree(getScoutProject());
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    Label description = new Label(group, SWT.WRAP);
    description.setText("If a client application is indluded it will be available for download under the rootUrl/download.");
    m_includeClientButton = new Button(group, SWT.CHECK);
    m_includeClientButton.setText("Include Client application");
    boolean initialSelection = getDialogSettings().getBoolean(SETTINGS_INCLUDE_CLIENT);
    setIncludingClientInternal(initialSelection);
    m_includeClientButton.setSelection(initialSelection);
    m_includeClientButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setIncludingClientInternal(m_includeClientButton.getSelection());
        m_clientProductField.setEnabled(m_includeClientButton.getSelection());
        pingStateChanging();
      }
    });

    ITreeNode clientProductTreeRoot = TreeUtility.createProductTree(getScoutProject(), new P_ClientProductFilter(), false);
    m_clientProductField = new ProductSelectionField(group, clientProductTreeRoot);
    m_clientProductField.setLabelText("Client product to include");
    m_clientProductField.addProductSelectionListener(new IProductSelectionListener() {

      @Override
      public void productSelected(IFile productFile) {
        setClientProductFileInternal(productFile);
        pingStateChanging();
      }
    });
    ITreeNode[] clientProductNodes = TreeUtility.findNodes(clientProductTreeRoot, NodeFilters.getByType(TreeUtility.TYPE_PRODUCT_NODE));
    if (clientProductNodes.length == 1) {
      IFile pf = (IFile) clientProductNodes[0].getData();
      setClientProductFileInternal(pf);
      m_clientProductField.setProductFile(pf);
    }
    else if (clientProductNodes.length == 0) {
      m_clientProductStatus = new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "No client product to add as download available.");
    }

    m_resourceFolderField = new ResourceServletFolderSelectionField(group, getScoutProject());
    m_resourceFolderField.setLabelText("Client download location");
    m_resourceFolderField.addProductSelectionListener(new IFolderSelectedListener() {
      @Override
      public void handleFolderSelection(IFolder folder) {
        setClientExportFolderInternal(folder);
        pingStateChanging();
      }
    });
    ITreeNode[] folderNodes = TreeUtility.findNodes(m_resourceFolderField.getRootNode(), NodeFilters.getByType(ResourceServletFolderTree.NODE_TYPE_FOLDER));
    if (folderNodes.length == 1) {
      IFolder folder = (IFolder) folderNodes[0].getData();
      setClientExportFolderInternal(folder);
      m_resourceFolderField.setFolder(folder);
    }
    else if (folderNodes.length == 0) {
      m_clientExportFolderStatus = new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Could not find a ResourceServlet registration.\n" +
          "Ensure to register a ResourceServlet with the two parameters [bundle-name, bundle-path].");
    }

    group.setLayout(new GridLayout(1, true));
    description.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_includeClientButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_clientProductField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_resourceFolderField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return group;
  }

  /**
   * @return the scoutProject
   */
  public IScoutProject getScoutProject() {
    return m_scoutProject;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    ExportServerWarOperation op = new ExportServerWarOperation(getServerProductFile());
    op.setWarFileName(getWarFile().getAbsolutePath());
    if (getClientProductFile() != null && isIncludingClient()) {
      op.setClientProduct(getClientProductFile());
      op.setHtmlFolder(getClientExportFolder());
    }
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
      multiStatus.add(getStatusClientProductField());
      multiStatus.add(getStatusClientExportFolder());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusServerProductField() throws JavaModelException {
    if (getServerProductFile() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "No server product file specified!");
    }
    return m_serverProductStatus;
  }

  protected IStatus getStatusClientProductField() throws JavaModelException {
    if (isIncludingClient()) {
      if (m_clientProductStatus.isOK()) {
        if (getClientProductFile() == null) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "No client product file specified!");
        }
      }
      return m_clientProductStatus;
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusWarField() throws JavaModelException {
    if (getWarFile() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "No war file specified!");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusOverwriteWarField() throws JavaModelException {
    if (!isOverwriteExistingWarFile()) {
      if (getWarFile() != null && getWarFile().exists()) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "war file already exists!");
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusClientExportFolder() throws JavaModelException {
    if (isIncludingClient()) {
      if (m_clientExportFolderStatus.isOK()) {
        if (getClientExportFolder() == null) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "No client export location specified!");
        }
      }
      return m_clientExportFolderStatus;
    }
    return Status.OK_STATUS;
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
   * @return the productFile
   */
  public IFile getClientProductFile() {
    return (IFile) getProperty(PROP_PRODUCT_FILE_CLIENT);
  }

  /**
   * @param productFile
   *          the productFile to set
   */
  public void setClientProductFile(IFile productFile) {
    try {
      setStateChanging(true);
      setClientProductFileInternal(productFile);
      if (isControlCreated()) {
        m_clientProductField.setProductFile(productFile);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public void setClientProductFileInternal(IFile productFile) {
    setProperty(PROP_PRODUCT_FILE_CLIENT, productFile);
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

  /**
   * @return the overwriteExistingWarFile
   */
  public boolean isIncludingClient() {
    return getPropertyBool(PROP_INCLUDE_CLIENT_APPLICATION);
  }

  /**
   * @param overwriteExistingWarFile
   *          the overwriteExistingWarFile to set
   */
  public void setIncludingClient(boolean includingClient) {
    try {
      setStateChanging(true);
      setIncludingClientInternal(includingClient);
      if (isControlCreated()) {
        m_includeClientButton.setSelection(includingClient);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setIncludingClientInternal(boolean includingClient) {
    setPropertyBool(PROP_INCLUDE_CLIENT_APPLICATION, includingClient);
  }

  /**
   * @return the warFile
   */
  public IFolder getClientExportFolder() {
    return (IFolder) getProperty(PROP_CLIENT_EXPORT_FOLDER);
  }

  public void setClientExportFolder(IFolder folder) {
    try {
      setStateChanging(true);
      setClientExportFolderInternal(folder);
      if (isControlCreated()) {
        m_resourceFolderField.setFolder(folder);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setClientExportFolderInternal(IFolder folder) {
    setProperty(PROP_CLIENT_EXPORT_FOLDER, folder);
  }

  private String findBestFitWarName() {
    String warName = null;
    if (getScoutProject().getUiSwingBundle() != null) {
      warName = findNameInBundle(getScoutProject().getUiSwingBundle());

    }
    if (warName == null && getScoutProject().getUiSwtBundle() != null) {
      warName = findNameInBundle(getScoutProject().getUiSwtBundle());
    }
    return warName;
  }

  private String findNameInBundle(IScoutBundle bundle) {
    P_ConfigIniVisitor visitor = new P_ConfigIniVisitor();
    try {
      bundle.getProject().accept(visitor);
    }
    catch (CoreException e) {
      if (e.getStatus().isOK()) {
        return visitor.getName();
      }
    }
    return null;
  }

  private class P_ConfigIniVisitor implements IResourceVisitor {
    private Pattern m_serverUrlPattern = Pattern.compile("\\/([^\\/]*)\\/process");
    private String m_name;

    @Override
    public boolean visit(IResource resource) throws CoreException {
      if (resource.getType() == IResource.FILE && CompareUtility.equals("config.ini", resource.getName()) && resource.exists()) {
        parseConfigIniFile((IFile) resource);
      }
      return true;
    }

    private void parseConfigIniFile(IFile configIniFile) throws CoreException {
      Properties props = new Properties();
      InputStream is = null;
      try {
        is = (configIniFile).getContents();
        props.load(is);
        // server.url=http://localhost:8080/@@ALIAS@@/process
        String serverUrl = props.getProperty("server.url");
        if (!StringUtility.isNullOrEmpty(serverUrl)) {
          Matcher m = m_serverUrlPattern.matcher(serverUrl);
          if (m.find()) {
            m_name = m.group(1);
          }
          throw new CoreException(Status.OK_STATUS);
        }
      }
      catch (IOException e) {
        ScoutSdkUi.logError("cuold not parse file '" + configIniFile.getFullPath() + "'.", e);
      }
      finally {
        if (is != null) {
          try {
            is.close();
          }
          catch (IOException e) {
            // void
          }
        }
      }
    }

    /**
     * @return the name
     */
    public String getName() {
      return m_name;
    }
  }

  private class P_ServerProductFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      switch (node.getType()) {
        case IScoutBundle.BUNDLE_SERVER:
          return true;
        case TreeUtility.TYPE_PRODUCT_NODE:
          return ScoutSdkUtility.getServletBridgeProductStatus((IFile) node.getData()).isOK();
        default:
          return false;
      }
    }
  }

  private class P_ClientProductFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      switch (node.getType()) {
        case IScoutBundle.BUNDLE_UI_SWING:
        case IScoutBundle.BUNDLE_UI_SWT:
        case TreeUtility.TYPE_PRODUCT_NODE:
          return true;
        default:
          return false;
      }
    }
  }
}
