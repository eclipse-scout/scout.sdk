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
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateTargetProjectOperation;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.TextField;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizardPage;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Version;

/**
 * <h3>ScoutProjectNewWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 06.03.2010
 */

public class ScoutProjectNewWizardPage extends AbstractProjectNewWizardPage implements IScoutProjectWizardPage {
  private static final String TYPE_BUNDLE = "bundle";

  private static final String PROP_CURR_TARGET = "curr";
  private static final String PROP_RECOMMENDED_TARGET = "recomm";
  private static final String PROP_PLATFORM_VERSION = "vers";

  protected StyledTextField m_projectNameField;
  protected StyledTextField m_postFixField;
  protected CheckableTree m_bundleTree;
  protected ITreeNode m_invisibleRootNode;

  protected ProposalTextField m_eclipseTargetPlatform;
  protected Button m_useDefaultScoutPreferences;
  protected StyledTextField m_projectAliasNameField;

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

    for (ScoutBundleUiExtension e : ScoutBundleExtensionPoint.getExtensions()) {
      e.getNewScoutBundleHandler().init(getWizard(), e);
    }

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
      TreeUtility.createNode(rootNode, TYPE_BUNDLE, e.getBundleName(), e.getIcon(), e.getOrderNumber(), e, false);
    }
    return rootNode;
  }

  private void updateBundleNames() {
    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
      ScoutBundleUiExtension ext = (ScoutBundleUiExtension) node.getData();
      if (ext != null && node.isEnabled()) {
        node.setText(AbstractScoutProjectNewOperation.getPluginName(getProjectName(), getProjectNamePostfix(), ext.getBundleName()));
      }
    }

    m_bundleTree.getTreeViewer().refresh();
    String alias = "";
    if (getProjectName() != null) {
      int dotIndex = getProjectName().lastIndexOf('.');
      if (dotIndex > 0 && dotIndex < getProjectName().length() - 1) {
        alias = getProjectName().substring(dotIndex + 1);
      }
    }
    String pf = getProjectNamePostfix();
    if (pf != null) {
      pf = pf.trim();
      if (pf.length() > 1) {
        alias += NamingUtility.ensureStartWithUpperCase(pf);
      }
    }
    m_projectAliasNameField.setText(alias);
  }

  protected SimpleProposal[] getTargetPlatformProposals() {
    final String RECOMMENDED_VERSION = "3.8";
    final String[][] supportedPlatforms = new String[][]{{"Indigo", "3.7"}, {"Juno", "3.8"}, {"Luna", "4.4"}};
    ArrayList<SimpleProposal> ret = new ArrayList<SimpleProposal>(supportedPlatforms.length);
    for (String[] platform : supportedPlatforms) {
      String codeName = platform[0];
      String ver = platform[1];
      boolean isCurrent = isCurrentPlatform(ver);
      boolean isRecommended = Boolean.valueOf(RECOMMENDED_VERSION.equals(ver));

      StringBuilder txt = new StringBuilder("Eclipse ");
      txt.append(codeName).append(" (").append(ver);
      if (isCurrent) {
        txt.append(", ").append(Texts.get("currrent"));
      }
      if (isRecommended) {
        txt.append(", ").append(Texts.get("recommended"));
      }
      txt.append(")");
      SimpleProposal prop = new SimpleProposal(txt.toString(), null);
      prop.setData(PROP_CURR_TARGET, Boolean.valueOf(isCurrent));
      prop.setData(PROP_RECOMMENDED_TARGET, isRecommended);
      prop.setData(PROP_PLATFORM_VERSION, ver);
      ret.add(prop);
    }
    return ret.toArray(new SimpleProposal[ret.size()]);
  }

  protected boolean isCurrentPlatform(String ver) {
    String curPlatform = PlatformVersionUtility.getPlatformVersion().toString();
    return curPlatform.startsWith(ver);
  }

  protected void setTargetPlatformDefaultSelection(SimpleProposal[] targetPlatformProposals) {
    SimpleProposal recommended = null;
    SimpleProposal current = null;
    for (SimpleProposal p : targetPlatformProposals) {
      Boolean reco = (Boolean) p.getData(PROP_RECOMMENDED_TARGET);
      if (reco.booleanValue()) {
        recommended = p;
      }
      Boolean curr = (Boolean) p.getData(PROP_CURR_TARGET);
      if (curr.booleanValue()) {
        current = p;
      }
    }
    if (current != null) {
      m_eclipseTargetPlatform.acceptProposal(current);
      setTargetPlatformVersionInternal((String) current.getData(PROP_PLATFORM_VERSION));
    }
    else if (recommended != null) {
      m_eclipseTargetPlatform.acceptProposal(recommended);
      setTargetPlatformVersionInternal((String) recommended.getData(PROP_PLATFORM_VERSION));
    }
  }

  protected Control createPropertiesGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_IN);
    group.setText(Texts.get("ProjectProperties"));

    SimpleProposal[] targetPlatformProposals = getTargetPlatformProposals();
    SimpleProposalProvider provider = new SimpleProposalProvider(targetPlatformProposals);
    m_eclipseTargetPlatform = getFieldToolkit().createProposalField(group, Texts.get("EclipsePlatform"), SWT.NONE);
    m_eclipseTargetPlatform.setContentProvider(provider);
    m_eclipseTargetPlatform.setLabelProvider(new SimpleLabelProvider());
    m_eclipseTargetPlatform.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        SimpleProposal proposal = (SimpleProposal) event.proposal;
        if (proposal != null) {
          setTargetPlatformVersionInternal((String) proposal.getData(PROP_PLATFORM_VERSION));
        }
        else {
          setTargetPlatformVersionInternal(null);
        }
        pingStateChanging();
      }
    });
    setTargetPlatformDefaultSelection(targetPlatformProposals);

    Composite prefButton = createPreferencesButton(group);

    m_projectAliasNameField = getFieldToolkit().createStyledTextField(group, Texts.get("ProjectAlias"));
    m_projectAliasNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setProjectAliasInternal(m_projectAliasNameField.getText());
        pingStateChanging();
      }
    });
    Composite infoLabel = createInfoLabel(group);

    // layout
    group.setLayout(new GridLayout(1, true));

    infoLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_eclipseTargetPlatform.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_projectAliasNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    prefButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return group;
  }

  protected Composite createPreferencesButton(Composite p) {
    Composite parent = new Composite(p, SWT.NONE);
    Label lbl = new Label(parent, SWT.NONE);

    // layout
    parent.setLayout(new FormLayout());
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 4);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(TextField.DEFAULT_LABEL_PERCENTAGE, 0);
    labelData.bottom = new FormAttachment(100, 0);
    lbl.setLayoutData(labelData);

    m_useDefaultScoutPreferences = new Button(parent, SWT.CHECK);
    m_useDefaultScoutPreferences.setText(Texts.get("UseDefaultScoutJDTPreferences"));
    m_useDefaultScoutPreferences.setSelection(isUseDefaultJdtPrefs());
    m_useDefaultScoutPreferences.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setUseDefaultJdtPrefsInternal(m_useDefaultScoutPreferences.getSelection());
        pingStateChanging();
      }
    });

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(lbl, 5);
    textData.right = new FormAttachment(100, 0);
    textData.bottom = new FormAttachment(100, 0);
    m_useDefaultScoutPreferences.setLayoutData(textData);
    return parent;
  }

  protected Composite createInfoLabel(Composite p) {
    Composite parent = new Composite(p, SWT.NONE);
    Label lbl = new Label(parent, SWT.NONE);

    // layout
    parent.setLayout(new FormLayout());
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 4);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(TextField.DEFAULT_LABEL_PERCENTAGE, 0);
    labelData.bottom = new FormAttachment(100, 0);
    lbl.setLayoutData(labelData);

    Label label = new Label(parent, SWT.NONE);
    label.setText(Texts.get("ProjectAliasHelp"));
    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(lbl, 5);
    textData.right = new FormAttachment(100, 0);
    textData.bottom = new FormAttachment(100, 0);
    label.setLayoutData(textData);
    return parent;
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
    properties.setProperty(IScoutProjectNewOperation.PROP_TARGET_PLATFORM_VERSION, new Version(getTargetPlatformVersion()));
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
    ResourceUtility.showUrlInBrowser("https://wiki.eclipse.org/Scout/HowTo/4.0/Create_a_new_project#Step_1");
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
    multiStatus.add(getStatusTargetPlatform());
    multiStatus.add(getStatusTargetProject());
    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
      if (m_bundleTree.isChecked(node)) {
        ScoutBundleUiExtension ext = (ScoutBundleUiExtension) node.getData();
        if (ext != null) {
          multiStatus.add(ext.getNewScoutBundleHandler().getStatus(getWizard()));
        }
      }
    }
  }

  protected IStatus getStatusTargetPlatform() {
    if (StringUtility.isNullOrEmpty(getTargetPlatformVersion())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("PleaseChooseATargetPlatform"));
    }
    SimpleProposal p = (SimpleProposal) m_eclipseTargetPlatform.getSelectedProposal();
    boolean isCurrent = ((Boolean) p.getData(PROP_CURR_TARGET)).booleanValue();
    if (!isCurrent) {
      return new Status(IStatus.INFO, ScoutSdkUi.PLUGIN_ID, Texts.get("ACompleteEclipsePlatformWillBeDownloaded"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusProjectPostfix() {
    if (StringUtility.isNullOrEmpty(getProjectNamePostfix())) {
      return Status.OK_STATUS;
    }
    else if (getProjectNamePostfix().matches("[a-zA-Z]{1}[a-zA-Z0-9]*[a-zA-Z]{1}")) {
      return Status.OK_STATUS;
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ProjectPostfixIsNotValid"));
    }
  }

  protected IStatus getStatusTargetProject() {
    String targetPluginName = AbstractScoutProjectNewOperation.getPluginName(getProjectName(), getProjectNamePostfix(), CreateTargetProjectOperation.TARGET_PROJECT_NAME_SUFFIX);
    return ScoutUtility.validateNewBundleName(targetPluginName);
  }

  protected IStatus getStatusProjectName() {
    for (ITreeNode node : TreeUtility.findNodes(m_invisibleRootNode, NodeFilters.getVisible())) {
      if (node.isEnabled() && m_bundleTree.isChecked(node)) {
        IStatus s = ScoutUtility.validateNewBundleName(node.getText());
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
  public void setBundleNodeSelected(boolean selected, String... extensionIds) {
    ITreeNode[] nodes = TreeUtility.findNodes(m_invisibleRootNode, new P_NodeByExtensionIdFilter(extensionIds));
    for (ITreeNode n : nodes) {
      m_bundleTree.setChecked(n, selected);
    }
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

  public String getTargetPlatformVersion() {
    return getPropertyString(PROP_ECLIPSE_TARGET_PLATFORM);
  }

  private void setTargetPlatformVersionInternal(String version) {
    setPropertyString(PROP_ECLIPSE_TARGET_PLATFORM, version);
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
//
//  private static final class P_TargetPlatformContentProvider extends ContentProposalProvider {
//
//    private final ILabelProvider m_labelProvider;
//
//    private P_TargetPlatformContentProvider() {
//      m_labelProvider = new P_TargetPlatformLabelProvider();
//    }
//
//    @Override
//    public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
//      if (!StringUtility.hasText(searchPattern)) {
//        searchPattern = "*";
//      }
//      else {
//        searchPattern = searchPattern.trim();
//      }
//
//
//    }
//  }
//
//  private static final class P_TargetPlatformLabelProvider implements ILabelProvider {
//
//    @Override
//    public void addListener(ILabelProviderListener listener) {
//    }
//
//    @Override
//    public void dispose() {
//    }
//
//    @Override
//    public boolean isLabelProperty(Object element, String property) {
//      return false;
//    }
//
//    @Override
//    public void removeListener(ILabelProviderListener listener) {
//    }
//
//    @Override
//    public Image getImage(Object element) {
//      return null;
//    }
//
//    @Override
//    public String getText(Object element) {
//      return ((TargetPlatformItem) element).getDisplayName();
//    }
//  }
//
//  private static final class TargetPlatformItem {
//    private String codeName;
//    private String version;
//
//    private String getDisplayName() {
//      return "Eclipse " + codeName + " (" + version + ")";
//    }
//  }
}
