package org.eclipse.scout.sdk.ui.internal.wizard.export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.IFolderSelectedListener;
import org.eclipse.scout.sdk.ui.fields.IProductSelectionListener;
import org.eclipse.scout.sdk.ui.fields.ProductSelectionField;
import org.eclipse.scout.sdk.ui.fields.ResourceServletFolderSelectionField;
import org.eclipse.scout.sdk.ui.fields.ResourceServletFolderTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ExportClientWizardPage extends AbstractWorkspaceWizardPage {

  private final static String SETTINGS_CLIENT_EXPORT_FOLDER = "clientExportFolderSetting";
  private final static String SETTINGS_PRODUCT_FILE_CLIENT = "clientProductFileSetting";

  private final static String PROP_CLIENT_EXPORT_FOLDER = "clientExportFolder";
  private final static String PROP_PRODUCT_FILE_CLIENT = "clientProductFile";

  private final IScoutProject m_scoutProject;

  private IStatus m_clientProductStatus = Status.OK_STATUS;
  private IStatus m_clientExportFolderStatus = Status.OK_STATUS;
  private ProductSelectionField m_clientProductField;
  private ResourceServletFolderSelectionField m_resourceFolderField;

  public ExportClientWizardPage(IScoutProject scoutProject) {
    super(ExportClientWizardPage.class.getName());
    m_scoutProject = scoutProject;
    setTitle(Texts.get("ExportWebClientArchive"));
    setDescription(Texts.get("WarExportDownloadClient"));
  }

  @Override
  protected void createContent(Composite parent) {
    ITreeNode clientProductTreeRoot = TreeUtility.createProductTree(getScoutProject(), new P_ClientProductFilter(), false);
    m_clientProductField = new ProductSelectionField(parent, clientProductTreeRoot);
    m_clientProductField.setLabelText(Texts.get("ClientProductToInclude"));
    m_clientProductField.addProductSelectionListener(new IProductSelectionListener() {
      @Override
      public void productSelected(IFile productFile) {
        setClientProductFileInternal(productFile);
        pingStateChanging();
      }
    });
    IFile defaultSelection = getProductFileSetting();
    if (defaultSelection == null) {
      ITreeNode[] clientProductNodes = TreeUtility.findNodes(clientProductTreeRoot, NodeFilters.getByType(TreeUtility.TYPE_PRODUCT_NODE));
      if (clientProductNodes.length == 1) {
        defaultSelection = (IFile) clientProductNodes[0].getData();
      }
      else if (clientProductNodes.length == 0) {
        m_clientProductStatus = new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoClientToAddAvail"));
      }
      else {
        defaultSelection = getDefaultSelectionProductFile(clientProductNodes);
      }
    }
    m_clientProductField.setProductFile(defaultSelection);
    setClientProductFileInternal(defaultSelection);

    m_resourceFolderField = new ResourceServletFolderSelectionField(parent, getScoutProject());
    m_resourceFolderField.setLabelText(Texts.get("ClientDownloadLocation"));
    m_resourceFolderField.addProductSelectionListener(new IFolderSelectedListener() {
      @Override
      public void handleFolderSelection(IFolder folder) {
        setClientExportFolderInternal(folder);
        pingStateChanging();
      }
    });
    IFolder defaultFolder = getResourceFolderSetting();
    if (defaultFolder == null) {
      ITreeNode[] folderNodes = TreeUtility.findNodes(m_resourceFolderField.getRootNode(), NodeFilters.getByType(ResourceServletFolderTree.NODE_TYPE_FOLDER));
      if (folderNodes.length == 1) {
        defaultFolder = (IFolder) folderNodes[0].getData();
      }
      else if (folderNodes.length == 0) {
        m_clientExportFolderStatus = new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoResourceServletFound"));
      }
    }
    m_resourceFolderField.setFolder(defaultFolder);
    setClientExportFolderInternal(defaultFolder);

    parent.setLayout(new GridLayout(1, true));
    m_clientProductField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_resourceFolderField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private IFolder getResourceFolderSetting() {
    String path = getDialogSettings().get(SETTINGS_CLIENT_EXPORT_FOLDER);
    if (StringUtility.hasText(path)) {
      Path p = new Path(path);
      if (p.segmentCount() > 1) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(p.segment(0));
        if (project != null) {
          IFolder productFile = project.getFolder(p.removeFirstSegments(1));
          if (productFile != null && productFile.exists()) {
            return productFile;
          }
        }
      }
    }
    return null;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusClientProductField());
    multiStatus.add(getStatusClientExportFolder());
  }

  protected IStatus getStatusClientProductField() {
    if (m_clientProductStatus.isOK()) {
      if (getClientProductFile() == null || !getClientProductFile().exists()) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoClientProductFileSpecified"));
      }
    }
    return m_clientProductStatus;
  }

  protected IStatus getStatusClientExportFolder() {
    if (m_clientExportFolderStatus.isOK()) {
      if (getClientExportFolder() == null || !getClientExportFolder().exists()) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoClientExportLocationSpecified"));
      }
    }
    return m_clientExportFolderStatus;
  }

  public IScoutProject getScoutProject() {
    return m_scoutProject;
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

  private void setClientProductFileInternal(IFile productFile) {
    setProperty(PROP_PRODUCT_FILE_CLIENT, productFile);
    String setting = null;
    if (productFile != null) {
      setting = productFile.getFullPath().toString();
    }
    getDialogSettings().put(SETTINGS_PRODUCT_FILE_CLIENT, setting);
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
    String setting = null;
    if (folder != null) {
      setting = folder.getFullPath().toString();
    }
    getDialogSettings().put(SETTINGS_CLIENT_EXPORT_FOLDER, setting);
  }

  private IFile getProductFileSetting() {
    String path = getDialogSettings().get(SETTINGS_PRODUCT_FILE_CLIENT);
    if (!StringUtility.isNullOrEmpty(path)) {
      Path p = new Path(path);
      if (p.segmentCount() > 1) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(p.segment(0));
        if (project != null) {
          IFile productFile = project.getFile(p.removeFirstSegments(1));
          return productFile;
        }
      }
    }
    return null;
  }

  private IFile getDefaultSelectionProductFile(ITreeNode[] clientProductNodes) {
    IFile ret = null;
    int lastPrio = -1;
    for (ITreeNode n : clientProductNodes) {
      IFile f = (IFile) n.getData();
      int prio = 0;
      if (!f.getName().toLowerCase().contains("dev")) {
        prio++;
      }
      if (f.getFullPath().toString().toLowerCase().contains("swt")) {
        prio++;
      }

      if (prio > lastPrio) {
        ret = f;
        lastPrio = prio;
      }
    }
    return ret;
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
