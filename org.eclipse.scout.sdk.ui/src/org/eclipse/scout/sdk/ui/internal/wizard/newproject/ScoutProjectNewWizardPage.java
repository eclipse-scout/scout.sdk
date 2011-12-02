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

import java.beans.PropertyChangeListener;
import java.util.HashSet;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ScoutBundleExtension;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizardPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
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
public class ScoutProjectNewWizardPage extends AbstractProjectNewWizardPage implements IScoutProjectWizardPage {
  static final int TYPE_BUNDLE = 99;

  private BasicPropertySupport m_propertySupport;

  private StyledTextField m_projectNameField;
  private StyledTextField m_postFixField;
  private CheckableTree m_bundleTree;
  private ITreeNode m_invisibleRootNode;
  private StyledTextField m_projectAliasNameField;

  private ScoutBundleExtension[] m_scoutBundleExtensions;

  public ScoutProjectNewWizardPage() {
    super(ScoutProjectNewWizardPage.class.getName());
    m_propertySupport = new BasicPropertySupport(this);
    setTitle(Texts.get("CreateAScoutProject"));
    setDescription(Texts.get("CreateScoutProjectHelpMsg"));
    m_scoutBundleExtensions = ScoutBundleExtensionPoint.getExtensions();
  }

  @Override
  protected void createContent(Composite parent) {
    ScoutBundleExtensionPoint.getExtensions();
    m_projectNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("ProjectName"));
    m_projectNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          setProjectNameInternal(m_projectNameField.getText());
          updateBundleNames();
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_postFixField = getFieldToolkit().createStyledTextField(parent, Texts.get("ProjectPostfix"));
    m_postFixField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          setProjectNamePostfixInternal(m_postFixField.getText());
          updateBundleNames();
        }
        finally {
          setStateChanging(false);
        }
      }
    });
    m_invisibleRootNode = buildBundleTree();
    m_bundleTree = new CheckableTree(parent, m_invisibleRootNode);

    m_bundleTree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        m_propertySupport.setProperty(PROP_SELECTED_BUNDLES, m_bundleTree.getCheckedNodes());
        ScoutBundleExtension ext = (ScoutBundleExtension) node.getData();
        if (ext != null) {
          ext.getBundleExtention().bundleSelectionChanged(getWizard(), checkState);
        }

        pingStateChanging();
      }
    });

    m_bundleTree.setChecked(TreeUtility.findNodes(m_invisibleRootNode, new P_InitialCheckNodesFilter()));

    Control aliasGroup = createAliasGroup(parent);
    m_projectNameField.setFocus();
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
    for (ScoutBundleExtension e : ScoutBundleExtensionPoint.getExtensions()) {
      TreeUtility.createNode(rootNode, TYPE_BUNDLE, e.getBundleName(), ScoutSdkUi.getImageDescriptor(e.getIconPath()), e.getOrderNumber(), e);
    }
//    TreeUtility.createNode(rootNode, TYPE_BUNDLE_SWING, "ui.swing", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SwingBundle), TYPE_BUNDLE_SWING);
//    TreeUtility.createNode(rootNode, TYPE_BUNDLE_SWT, "ui.swt", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SwtBundle), TYPE_BUNDLE_SWT);
//    TreeUtility.createNode(rootNode, TYPE_BUNDLE_CLIENT, "client", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ClientBundle), TYPE_BUNDLE_CLIENT);
//    TreeUtility.createNode(rootNode, TYPE_BUNDLE_SHARED, "shared", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SharedBundle), TYPE_BUNDLE_SHARED);
//    TreeUtility.createNode(rootNode, TYPE_BUNDLE_SERVER, "server", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerBundle), TYPE_BUNDLE_SERVER);
    return rootNode;
  }

  private void updateBundleNames() {
    String prefix = "";
    if (!StringUtility.isNullOrEmpty(getProjectName())) {
      prefix = getProjectName() + ".";
    }
    String postfix = "";
    String pf = getProjectNamePostfix();
    if (!StringUtility.isNullOrEmpty(pf)) {
      postfix = "." + pf;
    }
    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
      ScoutBundleExtension ext = (ScoutBundleExtension) node.getData();
      if (ext != null) {
        node.setText(prefix + ext.getBundleName() + postfix);
      }
    }
//    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_CLIENT)).setText(getProjectName() + ".client" + postfix);
//    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_SHARED)).setText(getProjectName() + ".shared" + postfix);
//    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_SERVER)).setText(getProjectName() + ".server" + postfix);
//    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_SWING)).setText(getProjectName() + ".ui.swing" + postfix);
//    TreeUtility.findNode(m_invisibleRootNode, NodeFilters.getByType(TYPE_BUNDLE_SWT)).setText(getProjectName() + ".ui.swt" + postfix);
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
    m_projectAliasNameField.setText(alias);
  }

  private Control createAliasGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_IN);
    group.setText(Texts.get("ProjectAlias"));
    Label label = new Label(group, SWT.NONE);
    label.setText(Texts.get("ProjectAliasHelp"));
    m_projectAliasNameField = getFieldToolkit().createStyledTextField(group, Texts.get("ProjectAlias"));
    m_projectAliasNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setProjectAliasInternal(m_projectAliasNameField.getText());
        pingStateChanging();
      }
    });
    // layout
    group.setLayout(new GridLayout(1, true));

    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_projectAliasNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor) {
    OperationJob job = new OperationJob(new P_PerformFinishOperation(getWizard()));
    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, monitor);
      Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
    }
    catch (Exception e) {
      ScoutSdkUi.logError("error during waiting for auto build and refresh");
    }
    getWizard().setCreatedProject(ScoutSdkCore.getScoutWorkspace().findScoutProject(getWizard().getProjectWizardPage().getProjectName()));
    return true;
  }

//  @Override
//  public void performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws IllegalArgumentException, CoreException {
//    TemplateVariableSet variables = TemplateVariableSet.createNew(getProjectName(), getProjectNamePostfix(), getProjectAlias());
//    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
//      if (m_bundleTree.isChecked(node)) {
//        ScoutBundleExtension ext = (ScoutBundleExtension) node.getData();
//        if (ext != null) {
//          try {
//            ext.getBundleExtention().createBundle(variables, monitor, workingCopyManager);
//          }
//          catch (Exception e) {
//            ScoutSdkUi.logError("could not create bundle of extension '" + ext.getBundleID() + "'.", e);
//          }
//        }
//      }
//    }
//
//    NewBsiCaseGroupStep1Operation op1 = new NewBsiCaseGroupStep1Operation(variables);
//    op1.setCreateUiSwing(isCreateUiSwing());
//    op1.setCreateUiSwt(isCreateUiSwt());
//    op1.setCreateClient(isCreateClient());
//    op1.setCreateShared(isCreateShared());
//    op1.setCreateServer(isCreateServer());
//    op1.setProjectName(getProjectName());
//    op1.setProjectNamePostfix(getPostFix());
//    op1.setProjectAlias(getProjectAlias());
//    op1.validate();
//    op1.run(monitor, workingCopyManager);
//
//    NewScoutProjectStep2Operation op2 = new NewScoutProjectStep2Operation(op1, variables);
//    op2.validate();
//    op2.run(monitor, workingCopyManager);
//
//  }

  @Override
  public ScoutProjectNewWizard getWizard() {
    return (ScoutProjectNewWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusProjectName());
    multiStatus.add(getStatusProjectAlias());
    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
      if (m_bundleTree.isChecked(node)) {
        ScoutBundleExtension ext = (ScoutBundleExtension) node.getData();
        if (ext != null) {
          multiStatus.add(ext.getBundleExtention().getStatus(getWizard()));
        }
      }
    }
  }

  protected IStatus getStatusProjectName() {
    if (StringUtility.isNullOrEmpty(getProjectName())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ProjectNameMissing"));
    }
    if (getProjectName().contains("..")) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Project name is not valid. Valid prject names are similar to 'org.eclipse.testapp'.");
    }
    if (getProjectName().matches("[a-zA-Z]{1}[a-zA-Z0-9\\.]*[a-zA-Z]{1}")) {
      if (getProjectName().matches(".*[A-Z].*")) {
        return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "Project name should contain only lower case characters (e.g. 'org.eclipse.testapp').");
      }
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Project name is not valid. Valid prject names are similar to 'org.eclipse.testapp'.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusProjectAlias() {
    if (StringUtility.isNullOrEmpty(getProjectAlias())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ProjectAliasMissing"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public boolean isBundleNodesSelected(String... extensionIds) {
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, new P_NodeByExtensionIdFilter(extensionIds));
    for (ITreeNode n : nodes) {
      if (!m_bundleTree.isChecked(n)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean hasSelectedBundle(BundleTypes... types) {
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, new P_NodeByBundleTypeFilter(types));
    for (ITreeNode n : nodes) {
      if (m_bundleTree.isChecked(n)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getProjectName() {
    return m_propertySupport.getPropertyString(PROP_PROJECT_NAME);
  }

  public void setProjectName(String projectName) {
    try {
      setStateChanging(true);
      setProjectNameInternal(projectName);
      if (isControlCreated()) {
        m_projectNameField.setText(projectName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setProjectNameInternal(String projectName) {
    m_propertySupport.setPropertyString(PROP_PROJECT_NAME, projectName);
  }

  @Override
  public String getProjectNamePostfix() {
    return m_propertySupport.getPropertyString(PROP_PROJECT_NAME_POSTFIX);
  }

  public void setProjectNamePostfix(String projectPostfix) {
    try {
      setStateChanging(true);
      setProjectNamePostfixInternal(projectPostfix);
      if (isControlCreated()) {
        m_postFixField.setText(projectPostfix);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setProjectNamePostfixInternal(String projectPostfix) {
    m_propertySupport.setPropertyString(PROP_PROJECT_NAME_POSTFIX, projectPostfix);
  }

  @Override
  public String getProjectAlias() {
    return m_propertySupport.getPropertyString(PROP_PROJECT_ALIAS);
  }

  public void setProjectAlias(String projectAlias) {
    try {
      setStateChanging(true);
      setProjectAliasInternal(projectAlias);
      if (isControlCreated()) {
        m_projectAliasNameField.setText(projectAlias);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setProjectAliasInternal(String alias) {
    m_propertySupport.setPropertyString(PROP_PROJECT_ALIAS, alias);
  }

  private class P_PerformFinishOperation implements IOperation {

    private final IScoutProjectWizard m_wizard;

    public P_PerformFinishOperation(IScoutProjectWizard wizard) {
      m_wizard = wizard;

    }

    @Override
    public String getOperationName() {
      return "create bundles...";
    }

    @Override
    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

      for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
        if (m_bundleTree.isChecked(node)) {
          ScoutBundleExtension ext = (ScoutBundleExtension) node.getData();
          if (ext != null) {
            try {
              IJavaProject javaProject = ext.getBundleExtention().createBundle(m_wizard, monitor, workingCopyManager);
              getWizard().addCreatedBundle(javaProject);
            }
            catch (Exception e) {
              ScoutSdkUi.logError("could not create bundle of extension '" + ext.getBundleID() + "'.", e);
            }
          }
        }
      }

    }
  }

  private class P_InitialCheckNodesFilter implements ITreeNodeFilter {

    @Override
    public boolean accept(ITreeNode node) {
      switch (node.getType()) {
        case TYPE_BUNDLE:
          return true;
      }
      return false;
    }

  } // end class P_InitialCheckNodesFilter

  private class P_NodeByExtensionIdFilter implements ITreeNodeFilter {
    private final HashSet<String> m_ids = new HashSet<String>();

    public P_NodeByExtensionIdFilter(String... extensionIds) {
      if (extensionIds != null) {
        for (String s : extensionIds) {
          m_ids.add(s);
        }
      }
    }

    @Override
    public boolean accept(ITreeNode node) {
      ScoutBundleExtension extension = (ScoutBundleExtension) node.getData();
      if (extension != null) {
        return m_ids.contains(extension.getBundleID());
      }
      return false;
    }
  }

  private class P_NodeByBundleTypeFilter implements ITreeNodeFilter {

    private final HashSet<BundleTypes> m_types = new HashSet<IScoutBundleExtension.BundleTypes>();

    public P_NodeByBundleTypeFilter(BundleTypes... types) {
      if (types != null) {
        for (BundleTypes s : types) {
          m_types.add(s);
        }
      }
    }

    @Override
    public boolean accept(ITreeNode node) {
      ScoutBundleExtension extension = (ScoutBundleExtension) node.getData();
      if (extension != null) {
        return m_types.contains(extension.getBundleType());
      }
      return false;
    }
  }

}
