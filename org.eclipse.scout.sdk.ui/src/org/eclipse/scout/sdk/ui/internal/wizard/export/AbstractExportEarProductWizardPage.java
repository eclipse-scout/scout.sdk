package org.eclipse.scout.sdk.ui.internal.wizard.export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.IProductSelectionListener;
import org.eclipse.scout.sdk.ui.fields.ProductSelectionField;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractExportEarProductWizardPage extends AbstractWorkspaceWizardPage {

  private final static String PROP_PRODUCT_FILE = "productFile";
  private final static String PROP_WAR_FILE_NAME = "warFileName";

  private final IScoutProject m_scoutProject;
  private final int m_nodeTypeFilter;

  private StyledTextField m_warFileName;
  private ProductSelectionField m_productField;
  private IStatus m_productStatus = Status.OK_STATUS;

  public AbstractExportEarProductWizardPage(IScoutProject scoutProject, String pageName, String title, int nodeTypeFilter) {
    super(pageName);
    m_scoutProject = scoutProject;
    m_nodeTypeFilter = nodeTypeFilter;
    setTitle(title);
    setDescription(Texts.get("ExportProductInEar"));
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

    ITreeNode productTreeRoot = TreeUtility.createProductTree(getScoutProject(), new DeployableProductFileNodeFilter(m_nodeTypeFilter), false);
    m_productField = new ProductSelectionField(parent, productTreeRoot);
    m_productField.setLabelText(Texts.get("ProductFile"));
    m_productField.addProductSelectionListener(new IProductSelectionListener() {

      @Override
      public void productSelected(IFile productFile) {
        setProductFileInternal(productFile);
        pingStateChanging();
      }
    });
    ITreeNode[] productNodes = TreeUtility.findNodes(productTreeRoot, NodeFilters.getByType(TreeUtility.TYPE_PRODUCT_NODE));
    if (productNodes.length == 1) {
      IFile pf = (IFile) productNodes[0].getData();
      setProductFileInternal(pf);
      m_productField.setProductFile(pf);
    }
    else if (productNodes.length == 0) {
      m_productStatus = new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("WarExportNoServerFound",
          DeployableProductFileNodeFilter.BUNDLE_ID_HTTP_SERVLETBRIDGE,
          DeployableProductFileNodeFilter.BUNDLE_ID_HTTP_REGISTRY));
    }

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_productField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_warFileName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    parent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusProductField());
    multiStatus.add(getStatusWarName());
  }

  protected IStatus getStatusProductField() {
    if (getProductFile() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoProductFileSpecified"));
    }
    return m_productStatus;
  }

  protected IStatus getStatusWarName() {
    if (getWarName() == null) {
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

  public void setProductFileInternal(IFile productFile) {
    setProperty(PROP_PRODUCT_FILE, productFile);
  }

  public IScoutProject getScoutProject() {
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
  }
}
