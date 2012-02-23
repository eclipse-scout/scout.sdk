package org.eclipse.scout.sdk.ui.internal.wizard.export;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.FileSelectionField;
import org.eclipse.scout.sdk.ui.fields.IFileSelectionListener;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.export.ExportScoutProjectEntry;
import org.eclipse.scout.sdk.ui.internal.extensions.export.ExportScoutProjectEntryExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizard;
import org.eclipse.scout.sdk.ui.wizard.export.IExportScoutProjectWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public class ExportScoutProjectWizardPage extends AbstractWorkspaceWizardPage implements IExportScoutProjectWizardPage {

  private static final String PROP_SELECTED_ENTRIES = "selectedEntries";
  private static final String PROP_TARGET_DIR = "targetDir";
  private static final String PROP_EXPORT_EAR = "exportEAR";
  private static final String PROP_EAR_FILE_NAME = "earFileName";

  private static final String SETTINGS_TARGET_DIR = "targetDirSetting";
  private static final String SETTINGS_EXPORT_EAR = "exportEARSetting";
  private static final String SETTINGS_EAR_FILE_NAME = "earFileNameSetting";

  private static final int TYPE_EXPORT_ENTRY = 101;

  private final IScoutProject m_scoutProject;
  private final BasicPropertySupport m_propertySupport;

  private FileSelectionField m_destDirFileField;
  private Button m_exportAsEarButton;
  private StyledTextField m_earFileName;
  private CheckableTree m_entryTree;
  private ITreeNode m_invisibleRootNode;

  public ExportScoutProjectWizardPage(IScoutProject scoutProject) {
    super(ExportScoutProjectWizardPage.class.getName());
    m_scoutProject = scoutProject;
    setTitle(Texts.get("ExportScoutProject"));
    setDescription(Texts.get("ExportScoutProjectMessage"));
    m_propertySupport = new BasicPropertySupport(this);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusTargetDirectoryField());
    multiStatus.add(getStatusEarFileName());
    multiStatus.add(getStatusEntryTree());

    if (m_invisibleRootNode != null) {
      for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
        if (m_entryTree.isChecked(node)) {
          ExportScoutProjectEntry entry = (ExportScoutProjectEntry) node.getData();
          if (entry != null) {
            IStatus status = entry.getHandler().getStatus(getWizard());
            if (status != null) {
              multiStatus.add(status);
            }
          }
        }
      }
    }
  }

  @Override
  protected void createContent(Composite parent) {
    m_destDirFileField = new FileSelectionField(parent);
    m_destDirFileField.setLabelText(Texts.get("TargetDirectory"));
    m_destDirFileField.setFolderMode(true);
    m_destDirFileField.addProductSelectionListener(new IFileSelectionListener() {
      @Override
      public void fileSelected(File file) {
        setTargetDirectoryInternal(file);
        pingStateChanging();
      }
    });
    String defaultSelection = getDialogSettings().get(SETTINGS_TARGET_DIR);
    if (defaultSelection != null) {
      File file = new File(defaultSelection);
      m_destDirFileField.setFile(file);
      setTargetDirectoryInternal(file);
      pingStateChanging();
    }

    Control exportAsEarGroup = createExportAsEarBox(parent);

    m_invisibleRootNode = buildBundleTree();
    m_entryTree = new CheckableTree(parent, m_invisibleRootNode);
    m_entryTree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        m_propertySupport.setProperty(PROP_SELECTED_ENTRIES, m_entryTree.getCheckedNodes());
        ExportScoutProjectEntry ext = (ExportScoutProjectEntry) node.getData();
        if (ext != null) {
          ext.getHandler().selectionChanged(getWizard(), checkState);
        }
        pingStateChanging();
      }
    });
    m_entryTree.setChecked(m_invisibleRootNode.getChildren().toArray(new ITreeNode[]{}));

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_destDirFileField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    exportAsEarGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    m_entryTree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  protected Control createExportAsEarBox(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);

    m_exportAsEarButton = new Button(group, SWT.CHECK);
    m_exportAsEarButton.setText(Texts.get("ExportAsEar"));
    m_exportAsEarButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setExportEarInternal(m_exportAsEarButton.getSelection());
        m_earFileName.setEnabled(m_exportAsEarButton.getSelection());
        pingStateChanging();
      }
    });
    m_exportAsEarButton.setSelection(getDialogSettings().getBoolean(SETTINGS_EXPORT_EAR));
    setExportEarInternal(m_exportAsEarButton.getSelection());

    m_earFileName = getFieldToolkit().createStyledTextField(group, Texts.get("EarFileName"));
    m_earFileName.setReadOnlySuffix(".ear");
    m_earFileName.setEnabled(m_exportAsEarButton.getSelection());
    m_earFileName.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setEarNameInternal(m_earFileName.getText());
        pingStateChanging();
      }
    });
    String defaultValue = getDialogSettings().get(SETTINGS_EAR_FILE_NAME);
    if (StringUtility.hasText(defaultValue) && !m_earFileName.getReadOnlySuffix().equals(defaultValue)) {
      m_earFileName.setText(defaultValue);
    }
    else {
      m_earFileName.setText(getWizard().getProjectAlias());
    }

    group.setLayout(new GridLayout(1, true));
    m_exportAsEarButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_earFileName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return group;
  }

  @Override
  public IExportScoutProjectWizard getWizard() {
    return (IExportScoutProjectWizard) super.getWizard();
  }

  @Override
  public boolean isNodesSelected(String... entryIds) {
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, new P_NodeByIdFilter(entryIds));
    for (ITreeNode n : nodes) {
      if (m_entryTree.isChecked(n)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ExportScoutProjectEntry[] getSelectedEntries() {
    if (m_invisibleRootNode == null) {
      return new ExportScoutProjectEntry[]{};
    }
    ArrayList<ExportScoutProjectEntry> selectedEntries = new ArrayList<ExportScoutProjectEntry>();
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible());
    for (ITreeNode n : nodes) {
      if (m_entryTree.isChecked(n)) {
        ExportScoutProjectEntry entry = (ExportScoutProjectEntry) n.getData();
        selectedEntries.add(entry);
      }
    }
    return selectedEntries.toArray(new ExportScoutProjectEntry[selectedEntries.size()]);
  }

  public File getTargetDirectory() {
    return (File) getProperty(PROP_TARGET_DIR);
  }

  public void setTargetDirectory(File dir) {
    try {
      setStateChanging(true);
      setTargetDirectoryInternal(dir);
      if (isControlCreated()) {
        m_destDirFileField.setFile(dir);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTargetDirectoryInternal(File f) {
    setProperty(PROP_TARGET_DIR, f);
    String path = null;
    if (f != null) {
      path = f.getAbsolutePath();
    }
    getDialogSettings().put(SETTINGS_TARGET_DIR, path);
  }

  public boolean isExportEar() {
    return getPropertyBool(PROP_EXPORT_EAR);
  }

  public void setExportEar(boolean exportEar) {
    try {
      setStateChanging(true);
      setExportEarInternal(exportEar);
      if (isControlCreated()) {
        m_exportAsEarButton.setSelection(exportEar);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setExportEarInternal(boolean exportEar) {
    setPropertyBool(PROP_EXPORT_EAR, exportEar);
    getDialogSettings().put(SETTINGS_EXPORT_EAR, exportEar);
  }

  public String getEarName() {
    return (String) getProperty(PROP_EAR_FILE_NAME);
  }

  public void setEarName(String name) {
    try {
      setStateChanging(true);
      setEarNameInternal(name);
      if (isControlCreated()) {
        m_earFileName.setText(name);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setEarNameInternal(String name) {
    setProperty(PROP_EAR_FILE_NAME, name);
    getDialogSettings().put(SETTINGS_EAR_FILE_NAME, name);
  }

  protected IStatus getStatusTargetDirectoryField() {
    if (getTargetDirectory() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoTargetSpecified"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusEarFileName() {
    if (isExportEar()) {
      if (!StringUtility.hasText(m_earFileName.getModifiableText())) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("SelectEarFileName"));
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusEntryTree() {
    ExportScoutProjectEntry[] entries = getSelectedEntries();
    if (entries == null || entries.length < 1) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("PleaseSelectEntry"));
    }
    return Status.OK_STATUS;
  }

  private ITreeNode buildBundleTree() {
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);
    for (ExportScoutProjectEntry e : ExportScoutProjectEntryExtensionPoint.getEntries()) {
      if (e.getHandler().isAvailable(getWizard())) {
        e.getHandler().selectionChanged(getWizard(), true);
        TreeUtility.createNode(rootNode, TYPE_EXPORT_ENTRY, e.getName(), ScoutSdkUi.getImageDescriptor(e.getIcon()), e.getOrder(), e);
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
      ExportScoutProjectEntry entry = (ExportScoutProjectEntry) node.getData();
      if (entry != null) {
        return m_ids.contains(entry.getId());
      }
      return false;
    }
  }
}
