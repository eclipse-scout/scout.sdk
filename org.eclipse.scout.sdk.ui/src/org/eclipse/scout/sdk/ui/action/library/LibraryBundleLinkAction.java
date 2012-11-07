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
package org.eclipse.scout.sdk.ui.action.library;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.ui.dialogs.PluginSelectionDialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.library.LibraryBundlesAddOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link LibraryBundleLinkAction}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 12.03.2012
 */
@SuppressWarnings("restriction")
public class LibraryBundleLinkAction extends AbstractScoutHandler {

  private IScoutBundle m_libraryUserBundle;

  public LibraryBundleLinkAction() {
    super(Texts.get("LinkLibraryBundlePopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.LibrariesAdd));
  }

  @Override
  public boolean isVisible() {
    return getLibraryUserBundle() != null;
  }

  @Override
  public Object execute(final Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
      @Override
      public void run() {
        PluginSelectionDialog dialog = new PluginSelectionDialog(shell, getPotentialLibraryBundles(), true);
        dialog.setDialogBoundsSettings(ScoutSdkUi.getDefault().getDialogSettingsSection(LibraryBundleLinkAction.class.getName() + ".DialogSettings"), Dialog.DIALOG_PERSISTSIZE);
        dialog.setInitialPattern("**");
        dialog.create();
        if (dialog.open() == Window.OK) {
          Set<IPluginModelBase> libraries = new HashSet<IPluginModelBase>();
          Object[] result = dialog.getResult();
          for (Object o : result) {
            if (o instanceof IPluginModelBase) {
              libraries.add((IPluginModelBase) o);
            }
          }
          // doit
          addLibraries(libraries);
        }
      }

    });
    return null;
  }

  protected IPluginModelBase[] getPotentialLibraryBundles() {
    Set<String> alreadyLinkedLibraries = new HashSet<String>();
    PluginModelHelper helper = new PluginModelHelper(getLibraryUserBundle().getProject());
    for (IPluginImport imp : helper.Manifest.getAllDependencies()) {
      alreadyLinkedLibraries.add(imp.getId());
    }
    List<IPluginModelBase> plugins = new LinkedList<IPluginModelBase>();
    for (IPluginModelBase bundle : PluginRegistry.getWorkspaceModels()) {
      if (bundle instanceof IPluginModel) {
        IProject project = bundle.getUnderlyingResource().getProject();
        if (project != null && project.exists()) {
          try {
            if (project.isOpen() && project.hasNature(ScoutSdk.LIBRARY_NATURE_ID) && !alreadyLinkedLibraries.contains(project.getName())) {
              plugins.add(bundle);
            }
          }
          catch (CoreException e) {
            ScoutSdkUi.logWarning("Could not determ library or not on '" + bundle.getBundleDescription().getName() + "'.");
          }
        }
      }
    }
    return plugins.toArray(new IPluginModelBase[plugins.size()]);
  }

  protected void addLibraries(Set<IPluginModelBase> libraries) {
    LibraryBundlesAddOperation addOp = new LibraryBundlesAddOperation(getLibraryUserBundle(), libraries);
    new OperationJob(addOp).schedule();

  }

  public void setLibraryUserBundle(IScoutBundle libraryUserBundle) {
    m_libraryUserBundle = libraryUserBundle;
  }

  public IScoutBundle getLibraryUserBundle() {
    return m_libraryUserBundle;
  }

}
