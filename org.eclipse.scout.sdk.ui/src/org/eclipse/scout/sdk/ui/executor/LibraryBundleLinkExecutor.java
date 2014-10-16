/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.ui.dialogs.PluginSelectionDialog;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.library.LibraryBundlesAddOperation;
import org.eclipse.scout.sdk.ui.action.library.LibraryBundleLinkAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link LibraryBundleLinkExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
@SuppressWarnings("restriction")
public class LibraryBundleLinkExecutor extends AbstractExecutor {

  private IScoutBundle m_bundle;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_bundle = UiUtility.getScoutBundleFromSelection(selection);
    return isEditable(m_bundle);
  }

  @Override
  public Object run(final Shell shell, final IStructuredSelection selection, ExecutionEvent event) {
    BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
      @Override
      public void run() {
        PluginSelectionDialog dialog = new PluginSelectionDialog(shell, getPotentialLibraryBundles(m_bundle), true);
        dialog.setDialogBoundsSettings(ScoutSdkUi.getDefault().getDialogSettingsSection(LibraryBundleLinkAction.class.getName() + ".DialogSettings"), Dialog.DIALOG_PERSISTSIZE);
        dialog.setInitialPattern("**");
        dialog.create();
        if (dialog.open() == Window.OK) {
          Object[] result = dialog.getResult();
          Set<IPluginModelBase> libraries = new HashSet<IPluginModelBase>(result.length);
          for (Object o : result) {
            if (o instanceof IPluginModelBase) {
              libraries.add((IPluginModelBase) o);
            }
          }
          // doit
          addLibraries(m_bundle, libraries);
        }
      }

    });
    return null;
  }

  protected void addLibraries(IScoutBundle b, Set<IPluginModelBase> libraries) {
    LibraryBundlesAddOperation addOp = new LibraryBundlesAddOperation(b, libraries);
    new OperationJob(addOp).schedule();
  }

  protected IPluginModelBase[] getPotentialLibraryBundles(IScoutBundle b) {
    Set<String> alreadyLinkedLibraries = new HashSet<String>();
    PluginModelHelper helper = new PluginModelHelper(b.getProject());
    for (IPluginImport imp : helper.Manifest.getAllDependencies()) {
      alreadyLinkedLibraries.add(imp.getId());
    }

    IPluginModelBase[] workspaceModels = PluginRegistry.getWorkspaceModels();
    List<IPluginModelBase> plugins = new ArrayList<IPluginModelBase>(workspaceModels.length);
    for (IPluginModelBase bundle : workspaceModels) {
      if (bundle instanceof IPluginModel) {
        IResource underlyingResource = bundle.getUnderlyingResource();
        if (ResourceUtility.exists(underlyingResource)) {
          IProject project = underlyingResource.getProject();
          if (project != null && project.exists()) {
            try {
              if (project.isOpen() && project.hasNature(ScoutSdk.LIBRARY_NATURE_ID) && !alreadyLinkedLibraries.contains(project.getName())) {
                plugins.add(bundle);
              }
            }
            catch (CoreException e) {
              ScoutSdkUi.logWarning("Could not determ library or not on '" + bundle.getBundleDescription().getName() + "'.", e);
            }
          }
        }
      }
    }
    return plugins.toArray(new IPluginModelBase[plugins.size()]);
  }
}
