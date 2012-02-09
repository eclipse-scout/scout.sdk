package org.eclipse.scout.sdk.ui.internal.wizard.export;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.FileSelectionField;
import org.eclipse.scout.sdk.ui.fields.IFileSelectionListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.ear.EarEntry;
import org.eclipse.scout.sdk.ui.internal.extensions.ear.EarEntryExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.ear.IScoutEarExportWizard;
import org.eclipse.scout.sdk.ui.wizard.ear.IScoutEarExportWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ExportEarWizardPage extends AbstractWorkspaceWizardPage implements IScoutEarExportWizardPage {

  private static final String PROP_SELECTED_ENTRIES = "selectedEntries";
  private final static String PROP_EAR_FILE = "earFile";

  private static final int TYPE_EAR_ENTRY = 101;

  private final IScoutProject m_scoutProject;
  private final BasicPropertySupport m_propertySupport;

  private CheckableTree m_entryTree;
  private FileSelectionField m_earFileField;
  private ITreeNode m_invisibleRootNode;

  public ExportEarWizardPage(IScoutProject scoutProject) {
    super(ExportEarWizardPage.class.getName());
    m_scoutProject = scoutProject;
    setTitle(Texts.get("ExportEnterpriseArchive"));
    setDescription(Texts.get("ExportEnterpriseArchiveMessage"));
    m_propertySupport = new BasicPropertySupport(this);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusEarField());
    multiStatus.add(getStatusEntryTree());
    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
      if (m_entryTree.isChecked(node)) {
        EarEntry entry = (EarEntry) node.getData();
        if (entry != null) {
          multiStatus.add(entry.getHandler().getStatus(getWizard()));
        }
      }
    }
  }

  @Override
  protected void createContent(Composite parent) {
    m_earFileField = new FileSelectionField(parent);
    m_earFileField.setLabelText(Texts.get("EarFile"));
    m_earFileField.setFilterExtensions(new String[]{"*.ear"});
    m_earFileField.addProductSelectionListener(new IFileSelectionListener() {
      @Override
      public void fileSelected(File file) {
        setEarFileInternal(file);
        pingStateChanging();
      }
    });

    m_invisibleRootNode = buildBundleTree();
    m_entryTree = new CheckableTree(parent, m_invisibleRootNode);
    m_entryTree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        m_propertySupport.setProperty(PROP_SELECTED_ENTRIES, m_entryTree.getCheckedNodes());
        EarEntry ext = (EarEntry) node.getData();
        if (ext != null) {
          ext.getHandler().selectionChanged(getWizard(), checkState);
        }
        pingStateChanging();
      }
    });
    m_entryTree.setChecked(m_invisibleRootNode.getChildren().toArray(new ITreeNode[]{}));

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_entryTree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    m_earFileField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public IScoutEarExportWizard getWizard() {
    return (IScoutEarExportWizard) super.getWizard();
  }

  @Override
  public boolean isNodesSelected(String... entryIds) {
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, new P_NodeByIdFilter(entryIds));
    for (ITreeNode n : nodes) {
      if (!m_entryTree.isChecked(n)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public EarEntry[] getSelectedEntries() {
    ArrayList<EarEntry> selectedEntries = new ArrayList<EarEntry>();
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible());
    for (ITreeNode n : nodes) {
      if (m_entryTree.isChecked(n)) {
        EarEntry entry = (EarEntry) n.getData();
        selectedEntries.add(entry);
      }
    }
    return selectedEntries.toArray(new EarEntry[selectedEntries.size()]);
  }

  /**
   * @return the warFile
   */
  public File getEarFile() {
    return (File) getProperty(PROP_EAR_FILE);
  }

  /**
   * @param warFile
   *          the warFile to set
   */
  public void setEarFile(File warFile) {
    try {
      setStateChanging(true);
      setEarFileInternal(warFile);
      if (isControlCreated()) {
        m_earFileField.setFile(warFile);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected IStatus getStatusEarField() {
    if (getEarFile() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoEARFileSpecified"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusEntryTree() {
    EarEntry[] entries = getSelectedEntries();
    if (entries == null || entries.length < 1) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("PleaseSelectEntry"));
    }
    return Status.OK_STATUS;
  }

  private void setEarFileInternal(File warFile) {
    setProperty(PROP_EAR_FILE, warFile);
  }

  private ITreeNode buildBundleTree() {
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);
    for (EarEntry e : EarEntryExtensionPoint.getEarEntries()) {
      if (e.getHandler().isAvailable(getWizard())) {
        e.getHandler().selectionChanged(getWizard(), true);
        TreeUtility.createNode(rootNode, TYPE_EAR_ENTRY, e.getName(), ScoutSdkUi.getImageDescriptor(e.getIcon()), e.getOrder(), e);
      }
    }
    return rootNode;
  }

  private class P_NodeByIdFilter implements ITreeNodeFilter {
    private final HashSet<String> m_ids = new HashSet<String>();

    public P_NodeByIdFilter(String... ids) {
      if (ids != null) {
        for (String s : ids) {
          m_ids.add(s);
        }
      }
    }

    @Override
    public boolean accept(ITreeNode node) {
      EarEntry entry = (EarEntry) node.getData();
      if (entry != null) {
        return m_ids.contains(entry.getId());
      }
      return false;
    }
  }
}
