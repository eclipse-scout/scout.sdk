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
package org.eclipse.scout.sdk.ui.wizard.bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.BundleImportOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.PluginDescriptorProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.BundleTypeProposal;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link BundleImportWizardPage}</h3> ...
 */
public class BundleImportWizardPage extends AbstractWorkspaceWizardPage {

  // ui fields
  private StyledTextField m_projectIdField;
  private ProposalTextField m_pluginModelField;
  private ProposalTextField m_bundleTypeField;

  // members
  private String m_projectId;
  private PluginDescriptorProposal m_pluginModel;
  private BundleTypeProposal m_bundleType;

  // process members
  private HashMap<Integer, BundleTypeProposal> m_bundleProposals = new HashMap<Integer, BundleTypeProposal>();
  private boolean m_projectIdFieldEnabled = true;
  private boolean m_projectAliasFieldEnabled = true;
  private boolean m_autoGeneratedProjectId = true;
  private boolean m_autoGeneratedAlias = true;

  public BundleImportWizardPage() {
    super(BundleImportWizardPage.class.getName());
    setTitle(Texts.get("ImportScoutBundle"));
    setDescription(Texts.get("BundleImportDesc"));
  }

  @Override
  protected void createContent(Composite parent) {
    IPluginModelBase[] workspaceModels = PluginRegistry.getWorkspaceModels();
    ArrayList<PluginDescriptorProposal> proposals = new ArrayList<PluginDescriptorProposal>();
    for (IPluginModelBase pluginModel : workspaceModels) {
      IProject p = pluginModel.getUnderlyingResource().getProject();
      if (ScoutSdkCore.getScoutWorkspace().getScoutBundle(p) == null) {
        proposals.add(new PluginDescriptorProposal(pluginModel));
      }
    }
    m_pluginModelField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(proposals.toArray(new IContentProposalEx[proposals.size()])), Texts.get("PluginToImport"));
    // m_pluginModelField.acceptProposal(m_superType);
    m_pluginModelField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          setPluginSelectionInternal((PluginDescriptorProposal) event.proposal);
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    for (BundleTypeProposal p : ScoutProposalUtility.getAllBundleProposals()) {
      m_bundleProposals.put(p.getType(), p);
    }
    m_bundleTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(ScoutProposalUtility.getAllBundleProposals()), Texts.get("BundleType"));
    // m_pluginModelField.acceptProposal(m_superType);
    m_bundleTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_bundleType = (BundleTypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    m_projectIdField = getFieldToolkit().createStyledTextField(parent, Texts.get("ScoutProjectId"));
    m_projectIdField.setText(getProjectId());
    m_projectIdField.setEnabled(isProjectIdFieldEnabled());
    m_projectIdField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        m_projectId = m_projectIdField.getText();
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_pluginModelField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_bundleTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_projectIdField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private int LEGACY_PROJECT_ID = 1 << 1;
  private int LEGACY_BUNDLE_TYPE = 1 << 3;

  private void setPluginSelectionInternal(PluginDescriptorProposal proposal) {
    if (proposal == null) {
      m_bundleTypeField.acceptProposal(null);
    }
    else {
      int legacyCoding = 0;
      // parse legacy
      PluginModelHelper h = new PluginModelHelper(proposal.getPluginBase());
      String bundleName = proposal.getPluginBase().getBundleDescription().getName();
      String groupId = h.Manifest.getEntry("BsiCase-ProjectGroupId");
      if (!StringUtility.isNullOrEmpty(groupId)) {
        legacyCoding |= LEGACY_PROJECT_ID;
        setProjectIdInternal(groupId);
      }
      String bundleType = h.Manifest.getEntry("BsiCase-BundleType");
      if (!StringUtility.isNullOrEmpty(bundleType)) {
        if (bundleType.equals("client")) {
          legacyCoding |= LEGACY_BUNDLE_TYPE;
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_CLIENT));
        }
        else if (bundleType.equals("shared")) {
          legacyCoding |= LEGACY_BUNDLE_TYPE;
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_SHARED));
        }
        else if (bundleType.equals("server")) {
          legacyCoding |= LEGACY_BUNDLE_TYPE;
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_SERVER));
        }
      }

      if ((legacyCoding & LEGACY_BUNDLE_TYPE) == 0) {
        if (bundleName.matches("^.*client\\.test.*$")) {
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_TEST_CLIENT));
        }
        else if (bundleName.matches("^.*client.*$")) {
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_CLIENT));
        }
        else if (bundleName.matches("^.*shared.*$")) {
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_SHARED));
        }
        else if (bundleName.matches("^.*server\\.app.*$")) {
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_SERVER_APPLICATION));
        }
        else if (bundleName.matches("^.*server.*$")) {
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_SERVER));
        }
        else if (bundleName.matches("^.*ui\\.swt.*$")) {
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_UI_SWT));
        }
        else if (bundleName.matches("^.*ui\\.swing.*$")) {
          setBundleType(m_bundleProposals.get(SdkProperties.BUNDLE_TYPE_UI_SWING));
        }
      }
      Matcher m = Pattern.compile("^(([^.]*\\.)*)([^.]*)\\.(client|ui\\.swt\\.app|ui\\.swt|ui\\.swing|shared|server\\.app | server).*$").matcher(proposal.getPluginBase().getBundleDescription().getName());
      if (m.find()) {

        if ((legacyCoding & LEGACY_PROJECT_ID) == 0) {
          String id = m.group(1);
          if (id.length() > 0) {
            id = id.substring(0, id.length() - 1);
          }
          setProjectId(id);
        }
      }
    }
    m_pluginModel = proposal;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    BundleImportOperation operation = new BundleImportOperation();
    operation.setProjectId(getProjectId());
    operation.setBundleType(getBundleType().getType());
    operation.setPluginModel(getPluginModel().getPluginBase());
    operation.run(monitor, workingCopyManager);
    return true;
  }

  public void setProjectId(String projectId) {
    try {
      setStateChanging(true);
      setProjectIdInternal(projectId);
      m_autoGeneratedProjectId = false;
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setProjectIdInternal(String projectId) {
    m_projectId = projectId;
    if (isControlCreated()) {
      m_projectIdField.setText(projectId);
    }
  }

  public String getProjectId() {
    return m_projectId;
  }

  public void setPluginModel(PluginDescriptorProposal pluginModel) {
    m_pluginModel = pluginModel;
  }

  public PluginDescriptorProposal getPluginModel() {
    return m_pluginModel;
  }

  protected void setBundleType(BundleTypeProposal bundleType) {
    try {
      setStateChanging(true);
      m_bundleType = bundleType;
      if (isControlCreated()) {
        m_bundleTypeField.acceptProposal(bundleType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected BundleTypeProposal getBundleType() {
    return m_bundleType;
  }

  public void setProjectIdFieldEnabled(boolean projectIdFieldEnabled) {
    m_projectIdFieldEnabled = projectIdFieldEnabled;
    if (isControlCreated()) {
      m_projectIdField.setEnabled(projectIdFieldEnabled);
    }
  }

  public boolean isProjectIdFieldEnabled() {
    return m_projectIdFieldEnabled;
  }

  // @Override
  // protected void validatePage(MultiStatus multiStatus){
  // try{
  // // multiStatus.add(getStatusNameField());
  // }
  // catch(JavaModelException e){
  // SDEUI.logError("could not validate name field.", e);
  // }
  // }

  // protected IStatus getStatusNameField() throws JavaModelException{
  // if(StringUtility.isNullOrEmpty(getBundleName())||getBundleName().equals(getParentBundle().getScoutProject().getGroupId())){
  // return new Status(IStatus.ERROR, SDE.PLUGIN_ID, Texts.get("Error_fieldNull"));
  // }
  // // check not allowed names
  // if(ResourcesPlugin.getWorkspace().getRoot().getProject(getBundleName()) != null){
  // return new Status(IStatus.ERROR, SDE.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
  // }
  // if(!getBundleName().matches("[a-zA-Z0-9\\._-]*")){
  // return new Status(IStatus.ERROR, SDE.PLUGIN_ID, Texts.get("Error_invalidFieldX", getBundleName()));
  // }
  // else{
  // return Status.OK_STATUS;
  // }
  // }

}
