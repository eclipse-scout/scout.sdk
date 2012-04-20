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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.BundleImportOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;

/**
 * <h3> {@link BundleImportWizardPage}</h3> ...
 */
public class BundleImportWizardPage extends AbstractWorkspaceWizardPage {

  // ui fields
  private ProposalTextField m_pluginModelField;

  // members
  private IPluginModelBase m_pluginModel;

  // process members
  private boolean m_projectIdFieldEnabled = true;

  public BundleImportWizardPage() {
    super(BundleImportWizardPage.class.getName());
    setTitle(Texts.get("ImportScoutBundle"));
    setDescription(Texts.get("BundleImportDesc"));
  }

  @Override
  protected void createContent(Composite parent) {
    IPluginModelBase[] workspaceModels = PluginRegistry.getWorkspaceModels();
    ArrayList<IPluginModelBase> proposals = new ArrayList<IPluginModelBase>();
    for (IPluginModelBase pluginModel : workspaceModels) {
      IProject p = pluginModel.getUnderlyingResource().getProject();
      if (ScoutSdkCore.getScoutWorkspace().getScoutBundle(p) == null) {
        proposals.add(pluginModel);
      }
    }

    m_pluginModelField = getFieldToolkit().createProposalField(parent, Texts.get("PluginToImport"));
    ILabelProvider labelProvider = new P_PluginDescLabelProvider();
    m_pluginModelField.setLabelProvider(labelProvider);
    m_pluginModelField.setContentProvider(new StaticContentProvider(proposals.toArray(new Object[proposals.size()]), labelProvider));

    // m_pluginModelField.acceptProposal(m_superType);
    m_pluginModelField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          setPluginSelectionInternal((IPluginModelBase) event.proposal);
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_pluginModelField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  private void setPluginSelectionInternal(IPluginModelBase proposal) {
    m_pluginModel = proposal;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    BundleImportOperation operation = new BundleImportOperation();
    operation.setPluginModel(getPluginModel());
    operation.run(monitor, workingCopyManager);

    ScoutSdkUi.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        MessageBox box = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_INFORMATION | SWT.OK);
        box.setMessage(Texts.get("BundleImportRestartMsg"));
        box.open();
      }
    });

    return true;
  }

  public void setPluginModel(IPluginModelBase pluginModel) {
    m_pluginModel = pluginModel;
  }

  public IPluginModelBase getPluginModel() {
    return m_pluginModel;
  }

  public boolean isProjectIdFieldEnabled() {
    return m_projectIdFieldEnabled;
  }

  private class P_PluginDescLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
      IPluginModelBase pluginModel = (IPluginModelBase) element;
      return pluginModel.getBundleDescription().getName();
    }

    @Override
    public Image getImage(Object element) {
      return ScoutSdkUi.getImage(ScoutSdkUi.SharedBundle);
    }
  }
}
