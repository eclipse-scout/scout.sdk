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
package org.eclipse.scout.sdk.ui.internal.wizard.newproject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.project.NewBsiCaseGroupStep1Operation;
import org.eclipse.scout.sdk.operation.project.NewBsiCaseGroupStep2Operation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.wizard.AbstractWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>ScoutProjectNewWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 06.03.2010
 */
public class ScoutProjectNewWizardPage extends AbstractWizardPage {
  static final int TYPE_BUNDLE_SWING = 100;
  static final int TYPE_BUNDLE_SWT = 101;
  static final int TYPE_BUNDLE_CLIENT = 102;
  static final int TYPE_BUNDLE_SHARED = 103;
  static final int TYPE_BUNDLE_SERVER = 104;

  private String m_projectName;
  private String m_postFix;
  private boolean m_createUiSwing;
  private boolean m_createUiSwt;
  private boolean m_createClient;
  private boolean m_createShared;
  private boolean m_createServer;
  private String m_projectAlias;

  private StyledTextField m_projectNameField;
  private StyledTextField m_postFixField;
  private CheckableTree m_bundleTree;
  private ITreeNode m_invisibleRootNode;
  private StyledTextField m_projectAliasNameFild;

  public ScoutProjectNewWizardPage() {
    super(ScoutProjectNewWizardPage.class.getName());
    setTitle("Create a Scout Project");
    setDescription("Create a Scout project in the workspace.\nEnter the name and choose the desired tiers.");
  }

  @Override
  protected void createContent(Composite parent) {
    m_projectNameField = getFieldToolkit().createStyledTextField(parent, "Project Name");
    m_projectNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          m_projectName = m_projectNameField.getText();
          updateBundleNames();
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_postFixField = getFieldToolkit().createStyledTextField(parent, "Project Postfix");
    m_postFixField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          m_postFix = m_postFixField.getText();
          updateBundleNames();
        }
        finally {
          setStateChanging(false);
        }
      }
    });
    m_invisibleRootNode = buildBundleTree();
    m_bundleTree = new CheckableTree(parent, m_invisibleRootNode);
    m_bundleTree.setChecked(TreeUtility.findNodes(m_invisibleRootNode, new P_InitialCheckNodesFilter()));
    m_bundleTree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        switch (node.getType()) {
          case TYPE_BUNDLE_CLIENT:
            m_createClient = checkState;
            break;
          case TYPE_BUNDLE_SHARED:
            m_createShared = checkState;
            break;
          case TYPE_BUNDLE_SERVER:
            m_createServer = checkState;
            break;
          case TYPE_BUNDLE_SWING:
            m_createUiSwing = checkState;
            break;
          case TYPE_BUNDLE_SWT:
            m_createUiSwt = checkState;
            break;
        }
        pingStateChanging();
      }
    });

    Control aliasGroup = createAliasGroup(parent);
    // layout
    parent.setLayout(new GridLayout(1, true));

    m_projectNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_postFixField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_bundleTree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    aliasGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private ITreeNode buildBundleTree() {
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);
    TreeUtility.createNode(rootNode, TYPE_BUNDLE_SWING, "ui.swing", ScoutSdkUi.getImage(ScoutSdkUi.IMG_CLIENT), TYPE_BUNDLE_SWING);
    TreeUtility.createNode(rootNode, TYPE_BUNDLE_SWT, "ui.swt", ScoutSdkUi.getImage(ScoutSdkUi.IMG_CLIENT), TYPE_BUNDLE_SWT);
    TreeUtility.createNode(rootNode, TYPE_BUNDLE_CLIENT, "client", ScoutSdkUi.getImage(ScoutSdkUi.IMG_CLIENT), TYPE_BUNDLE_CLIENT);
    TreeUtility.createNode(rootNode, TYPE_BUNDLE_SHARED, "shared", ScoutSdkUi.getImage(ScoutSdkUi.IMG_SHARED), TYPE_BUNDLE_SHARED);
    TreeUtility.createNode(rootNode, TYPE_BUNDLE_SERVER, "server", ScoutSdkUi.getImage(ScoutSdkUi.IMG_SERVER), TYPE_BUNDLE_SERVER);
    return rootNode;
  }

  private void updateBundleNames() {
    String postfix = "";
    String pf = getPostFix();
    if (!StringUtility.isNullOrEmpty(pf)) {
      postfix = "." + pf;
    }
    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_CLIENT)).setText(getProjectName() + ".client" + postfix);
    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_SHARED)).setText(getProjectName() + ".shared" + postfix);
    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_SERVER)).setText(getProjectName() + ".server" + postfix);
    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_SWING)).setText(getProjectName() + ".ui.swing" + postfix);
    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_SWT)).setText(getProjectName() + ".ui.swt" + postfix);
    m_bundleTree.getTreeViewer().refresh();
    String alias = "";
    int dotIndex = getProjectName().lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < getProjectName().length() - 1) {
      alias = getProjectName().substring(dotIndex + 1);
    }
    if (pf != null && pf.length() > 1) {
      alias = alias + Character.toUpperCase(pf.charAt(0)) + pf.substring(1);
    }
    // setProjectAlias(alias);
    m_projectAliasNameFild.setText(alias);
  }

  private Control createAliasGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_IN);
    group.setText("Project Alias");
    Label label = new Label(group, SWT.NONE);
    label.setText("The project alias is used for the servlet name and launcher names.");
    m_projectAliasNameFild = getFieldToolkit().createStyledTextField(group, "Project Alias");
    m_projectAliasNameFild.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_projectAlias = m_projectAliasNameFild.getText();
        pingStateChanging();
      }
    });
    // layout
    group.setLayout(new GridLayout(1, true));

    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_projectAliasNameFild.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  public void performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws IllegalArgumentException, CoreException {
    TemplateVariableSet variables = TemplateVariableSet.createNew(getProjectName(), getPostFix(), getProjectAlias());
    NewBsiCaseGroupStep1Operation op1 = new NewBsiCaseGroupStep1Operation(variables);
    op1.setCreateUiSwing(isCreateUiSwing());
    op1.setCreateUiSwt(isCreateUiSwt());
    op1.setCreateClient(isCreateClient());
    op1.setCreateShared(isCreateShared());
    op1.setCreateServer(isCreateServer());
    op1.setProjectName(getProjectName());
    op1.setProjectNamePostfix(getPostFix());
    op1.setProjectAlias(getProjectAlias());
    op1.validate();
    op1.run(monitor, workingCopyManager);

    NewBsiCaseGroupStep2Operation op2 = new NewBsiCaseGroupStep2Operation(op1, variables);
    op2.validate();
    op2.run(monitor, workingCopyManager);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusProjectName());
    multiStatus.add(getStatusProjectAlias());
    multiStatus.add(getStatusUiBundles());
    multiStatus.add(getStatusClientBundle());
  }

  protected IStatus getStatusProjectName() {
    if (StringUtility.isNullOrEmpty(getProjectName())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Project name is not set.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusProjectAlias() {
    if (StringUtility.isNullOrEmpty(getProjectAlias())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Project alias is not set.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusUiBundles() {
    if (!m_createClient) {
      if (isCreateUiSwing()) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "A swing bundle without a client boundle can not be created.");
      }

      if (isCreateUiSwt()) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "A swt bundle without a client boundle can not be created.");
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusClientBundle() {
    if (isCreateClient()) {
      if (!isCreateShared()) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "A client bundle without a shared bundle can not be created.");
      }

    }
    return Status.OK_STATUS;
  }

  /**
   * @return the projectName
   */
  public String getProjectName() {
    return m_projectName;
  }

  /**
   * @param projectName
   *          the projectName to set
   */
  public void setProjectName(String projectName) {
    m_projectName = projectName;
  }

  /**
   * @return the postFix
   */
  public String getPostFix() {
    return m_postFix;
  }

  /**
   * @param postFix
   *          the postFix to set
   */
  public void setPostFix(String postFix) {
    m_postFix = postFix;
  }

  /**
   * @param projectAlias
   *          the projectAlias to set
   */
  public void setProjectAlias(String projectAlias) {
    m_projectAlias = projectAlias;
  }

  /**
   * @return the projectAlias
   */
  public String getProjectAlias() {
    return m_projectAlias;
  }

  /**
   * @return the createUiSwing
   */
  public boolean isCreateUiSwing() {
    return m_createUiSwing;
  }

  /**
   * @param createUiSwing
   *          the createUiSwing to set
   */
  public void setCreateUiSwing(boolean createUiSwing) {
    m_createUiSwing = createUiSwing;
  }

  /**
   * @return the createUiSwt
   */
  public boolean isCreateUiSwt() {
    return m_createUiSwt;
  }

  /**
   * @param createUiSwt
   *          the createUiSwt to set
   */
  public void setCreateUiSwt(boolean createUiSwt) {
    m_createUiSwt = createUiSwt;
  }

  /**
   * @return the createClient
   */
  public boolean isCreateClient() {
    return m_createClient;
  }

  /**
   * @param createClient
   *          the createClient to set
   */
  public void setCreateClient(boolean createClient) {
    m_createClient = createClient;
  }

  /**
   * @return the createShared
   */
  public boolean isCreateShared() {
    return m_createShared;
  }

  /**
   * @param createShared
   *          the createShared to set
   */
  public void setCreateShared(boolean createShared) {
    m_createShared = createShared;
  }

  /**
   * @return the createServer
   */
  public boolean isCreateServer() {
    return m_createServer;
  }

  /**
   * @param createServer
   *          the createServer to set
   */
  public void setCreateServer(boolean createServer) {
    m_createServer = createServer;
  }

  private class P_InitialCheckNodesFilter implements ITreeNodeFilter {

    @Override
    public boolean accept(ITreeNode node) {
      switch (node.getType()) {
        case TYPE_BUNDLE_CLIENT:
          m_createClient = true;
          return true;
        case TYPE_BUNDLE_SHARED:
          m_createShared = true;
          return true;
        case TYPE_BUNDLE_SERVER:
          m_createServer = true;
          return true;
        case TYPE_BUNDLE_SWING:
          m_createUiSwing = true;
          return true;
        case TYPE_BUNDLE_SWT:
          m_createUiSwt = true;
          return true;
      }
      return false;
    }

  } // end class P_InitialCheckNodesFilter

}
