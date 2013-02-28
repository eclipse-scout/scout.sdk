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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizardPage;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.validation.JavaElementValidator;
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
import org.eclipse.swt.widgets.Label;

/**
 * <h3>ScoutProjectNewWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 06.03.2010
 */

public class ScoutProjectNewWizardPage extends AbstractProjectNewWizardPage implements IScoutProjectWizardPage {
  private static final String TYPE_BUNDLE = "bundle";

  protected StyledTextField m_projectNameField;
  protected StyledTextField m_postFixField;
  protected CheckableTree m_bundleTree;
  protected ITreeNode m_invisibleRootNode;
  protected StyledTextField m_projectAliasNameField;
  protected Button m_useDefaultScoutPreferences;

  public ScoutProjectNewWizardPage() {
    super(ScoutProjectNewWizardPage.class.getName());
    setTitle(Texts.get("CreateAScoutProject"));
    setDescription(Texts.get("CreateScoutProjectHelpMsg"));
    setUseDefaultJdtPrefsInternal(true);
  }

  @Override
  protected void createContent(Composite parent) {
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
    for (ScoutBundleUiExtension e : ScoutBundleExtensionPoint.getExtensions()) {
      e.getNewScoutBundleHandler().init(getWizard(), e);
    }
    m_bundleTree = new CheckableTree(parent, m_invisibleRootNode);

    m_bundleTree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        setProperty(PROP_SELECTED_BUNDLES, m_bundleTree.getCheckedNodes());
        if (!node.isEnabled()) {
          checkState = false;
        }
        ScoutBundleUiExtension ext = (ScoutBundleUiExtension) node.getData();
        if (ext != null) {
          ext.getNewScoutBundleHandler().bundleSelectionChanged(getWizard(), checkState);
        }

        pingStateChanging();
      }
    });

    m_bundleTree.setChecked(TreeUtility.findNodes(m_invisibleRootNode, new P_InitialCheckNodesFilter()));

    Control aliasGroup = createPropertiesGroup(parent);
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
    for (ScoutBundleUiExtension e : ScoutBundleExtensionPoint.getExtensions()) {
      TreeUtility.createNode(rootNode, TYPE_BUNDLE, e.getBundleName(), e.getIconPath(), e.getOrderNumber(), e);
    }
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
      ScoutBundleUiExtension ext = (ScoutBundleUiExtension) node.getData();
      if (ext != null && node.isEnabled()) {
        node.setText(prefix + ext.getBundleName() + postfix);
      }
    }

    m_bundleTree.getTreeViewer().refresh();
    String alias = "";
    int dotIndex = getProjectName().lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < getProjectName().length() - 1) {
      alias = getProjectName().substring(dotIndex + 1);
    }
    if (pf != null && pf.length() > 1) {
      alias = alias + Character.toUpperCase(pf.charAt(0)) + pf.substring(1);
    }
    m_projectAliasNameField.setText(alias);
  }

  protected Control createPropertiesGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_IN);
    group.setText(Texts.get("ProjectProperties"));

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

    m_useDefaultScoutPreferences = new Button(group, SWT.CHECK);
    m_useDefaultScoutPreferences.setText(Texts.get("UseDefaultScoutJDTPreferences"));
    m_useDefaultScoutPreferences.setSelection(isUseDefaultJdtPrefs());
    m_useDefaultScoutPreferences.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setUseDefaultJdtPrefsInternal(m_useDefaultScoutPreferences.getSelection());
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));

    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_projectAliasNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_useDefaultScoutPreferences.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  @Override
  public void putProperties(PropertyMap properties) {
    // put properties of the text fields of this page (project name, etc)
    String postfix = getProjectNamePostfix();
    if (postfix != null) {
      postfix = postfix.trim();
      if (postfix.length() == 0) {
        postfix = null;
      }
    }
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_NAME, getProjectName().trim());
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_NAME_POSTFIX, postfix);
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_ALIAS, getProjectAlias().trim());
    properties.setProperty(IScoutProjectNewOperation.PROP_TARGET_PLATFORM_VERSION, PlatformVersionUtility.getPlatformVersion());
    properties.setProperty(IScoutProjectNewOperation.PROP_USE_DEFAULT_JDT_PREFS, isUseDefaultJdtPrefs());

    // go through all node extensions and put properties which node has been checked
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getAcceptAll());
    HashSet<String> checkedNodeExtensionIds = new HashSet<String>(nodes.length);
    for (ITreeNode node : nodes) {
      ScoutBundleUiExtension ext = (ScoutBundleUiExtension) node.getData();
      if (ext != null) {
        if (m_bundleTree.isChecked(node) && node.isEnabled() && node.isVisible()) {
          checkedNodeExtensionIds.add(ext.getBundleId());
        }
        ext.getNewScoutBundleHandler().putProperties(getWizard(), properties);
      }
    }
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_CHECKED_NODES, checkedNodeExtensionIds);
  }

  @Override
  public void performHelp() {
    //TODO: remove external link and use eclipse help instead
    ResourceUtility.showUrlInBrowser("http://wiki.eclipse.org/Scout/HowTo/3.9/Create_a_new_project#Step_1");
  }

  @Override
  public ScoutProjectNewWizard getWizard() {
    return (ScoutProjectNewWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusProjectName());
    multiStatus.add(getStatusProjectPostfix());
    multiStatus.add(getStatusProjectAlias());
    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
      if (m_bundleTree.isChecked(node)) {
        ScoutBundleUiExtension ext = (ScoutBundleUiExtension) node.getData();
        if (ext != null) {
          multiStatus.add(ext.getNewScoutBundleHandler().getStatus(getWizard()));
        }
      }
    }
  }

  protected IStatus getStatusProjectPostfix() {
    if (StringUtility.isNullOrEmpty(getProjectNamePostfix())) {
      return Status.OK_STATUS;
    }
    else if (getProjectNamePostfix().matches("[a-zA-Z]{1}[a-zA-Z0-9]*[a-zA-Z]{1}")) {
      return Status.OK_STATUS;
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Project postfix is not valid.");
    }
  }

  protected IStatus getStatusProjectName() {
    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
      if (node.isEnabled() && m_bundleTree.isChecked(node)) {
        IStatus s = JavaElementValidator.validateNewBundleName(node.getText());
        if (!s.isOK()) {
          return s;
        }
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusProjectAlias() {
    if (StringUtility.isNullOrEmpty(getProjectAlias())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ProjectAliasMissing"));
    }
    if (getProjectAlias().matches("[a-zA-Z]{1}[a-zA-Z0-9]*[a-zA-Z]{1}")) {
      return Status.OK_STATUS;
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Project alias is not valid.");
    }
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    super.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    super.removePropertyChangeListener(listener);
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
  public void setBundleNodeAvailable(boolean enabled, boolean visible, String... extensionIds) {
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, new P_NodeByExtensionIdFilter(extensionIds));
    for (ITreeNode n : nodes) {
      n.setEnabled(enabled);
      n.setVisible(visible);
    }
  }

  @Override
  public boolean hasSelectedBundle(String... types) {
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
    return getPropertyString(PROP_PROJECT_NAME);
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
    setPropertyString(PROP_PROJECT_NAME, projectName);
  }

  @Override
  public String getProjectNamePostfix() {
    return getPropertyString(PROP_PROJECT_NAME_POSTFIX);
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
    setPropertyString(PROP_PROJECT_NAME_POSTFIX, projectPostfix);
  }

  @Override
  public String getProjectAlias() {
    return getPropertyString(PROP_PROJECT_ALIAS);
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
    setPropertyString(PROP_PROJECT_ALIAS, alias);
  }

  public boolean isUseDefaultJdtPrefs() {
    return getPropertyBool(PROP_USE_DEFAULT_JDT_PREFS);
  }

  public void setUseDefaultJdtPrefs(boolean useDefaultJdtPrefs) {
    try {
      setStateChanging(true);
      setUseDefaultJdtPrefsInternal(useDefaultJdtPrefs);
      if (isControlCreated()) {
        m_useDefaultScoutPreferences.setSelection(useDefaultJdtPrefs);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setUseDefaultJdtPrefsInternal(boolean useDefaultJdtPrefs) {
    setPropertyBool(PROP_USE_DEFAULT_JDT_PREFS, useDefaultJdtPrefs);
  }

  private class P_InitialCheckNodesFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      return TYPE_BUNDLE.equals(node.getType());
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
      ScoutBundleUiExtension extension = (ScoutBundleUiExtension) node.getData();
      if (extension != null) {
        return m_ids.contains(extension.getBundleId());
      }
      return false;
    }
  }

  private class P_NodeByBundleTypeFilter implements ITreeNodeFilter {

    private final HashSet<String> m_types = new HashSet<String>();

    public P_NodeByBundleTypeFilter(String... types) {
      if (types != null) {
        for (String s : types) {
          m_types.add(s);
        }
      }
    }

    @Override
    public boolean accept(ITreeNode node) {
      ScoutBundleUiExtension extension = (ScoutBundleUiExtension) node.getData();
      if (extension != null) {
        return m_types.contains(extension.getBundleType());
      }
      return false;
    }
  }
}
