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
package org.eclipse.scout.sdk.ui.view.properties.part.singlepage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.dialog.ProductSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ProjectVersionPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.TechnologyPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.part.Section;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.ProductLaunchPresenter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IMemento;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <h3>{@link ScoutProjectPropertyPart}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 09.09.2010
 */
public class ScoutProjectPropertyPart extends AbstractSinglePageSectionBasedViewPart {

  private static final String SECTION_ID_FILTER = "section.filter";
  private static final String SECTION_ID_PRODUCT_LAUNCHER = "section.productLauncher";
  private static final String SECTION_ID_VERSION = "section.version";
  private static final String SECTION_ID_TECHNOLOGY = "section.technology";

  private static final String PROJECT_PROD_LAUNCHERS = "pref_scout_project_prod_launcher";

  private final ArrayList<ProductLaunchPresenter> m_launchPresenters = new ArrayList<>();

  @Override
  protected void createSections() {
    // filter
    ISection filterSection = createSection(SECTION_ID_FILTER, Texts.get("Filter"));
    PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), filterSection.getSectionClient(), getPage());
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    filterPresenter.getContainer().setLayoutData(layoutData);
    filterSection.setExpanded(wasSectionExpanded(SECTION_ID_FILTER, false));

    // version
    if (!getScoutProject().isBinary()) {
      ISection versionSection = createSection(SECTION_ID_VERSION, Texts.get("ProjectVersion"));
      ProjectVersionPresenter versionPresenter = new ProjectVersionPresenter(getFormToolkit(), versionSection.getSectionClient(), getScoutProject());
      GridData versionLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      versionLayoutData.widthHint = 200;
      versionPresenter.getContainer().setLayoutData(versionLayoutData);
      versionSection.setExpanded(wasSectionExpanded(SECTION_ID_VERSION, true));
    }

    // product launchers
    Section linkSection = (Section) createSection(SECTION_ID_PRODUCT_LAUNCHER, Texts.get("ProductLauncher"));
    ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
    ToolBar toolbar = toolBarManager.createControl(linkSection.getUiSection());
    final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
    toolbar.setCursor(handCursor);
    Action action = new Action() {
      @Override
      public void run() {
        ArrayList<IFile> productFiles = new ArrayList<>();
        for (ProductLaunchPresenter p : m_launchPresenters) {
          productFiles.add(p.getProductFile());
        }
        ProductSelectionDialog dialog = new ProductSelectionDialog(getForm().getShell(), getScoutProject());
        dialog.setCheckedProductFiles(productFiles.toArray(new IFile[productFiles.size()]));
        if (dialog.open() == Dialog.OK) {
          refreshProductLaunchPresenters(dialog.getCheckedProductFiles());
        }
      }
    };
    action.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolEdit));
    action.setToolTipText(Texts.get("EditContent"));
    action.setText(Texts.get("EditContent"));
    toolBarManager.add(action);
    toolBarManager.update(true);
    linkSection.getUiSection().setTextClient(toolbar);
    linkSection.setExpanded(wasSectionExpanded(SECTION_ID_PRODUCT_LAUNCHER, true));

    // technologies
    if (!getScoutProject().isBinary()) {
      Section techSection = (Section) createSection(SECTION_ID_TECHNOLOGY, Texts.get("Technologies"));
      final TechnologyPresenter techPresenter = new TechnologyPresenter(getFormToolkit(), techSection.getSectionClient(), getScoutProject());
      GridData techLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      techLayoutData.widthHint = 200;
      techPresenter.getContainer().setLayoutData(techLayoutData);

      // load technologies lazy
      final ProgressIndicator indicator = new ProgressIndicator(techPresenter.getContainer(), SWT.INDETERMINATE | SWT.SMOOTH);
      indicator.beginAnimatedTask();
      GridData indicatorData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      indicatorData.horizontalSpan = 2;
      indicatorData.heightHint = 5;
      indicator.setLayoutData(indicatorData);
      Job j = new Job("load technologies...") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          if (techPresenter != null && !techPresenter.isDisposed()) {
            try {
              techPresenter.loadModel();
            }
            finally {
              if (techPresenter.getContainer() != null && !techPresenter.getContainer().isDisposed()) {
                techPresenter.getContainer().getDisplay().asyncExec(new Runnable() {
                  @Override
                  public void run() {
                    if (techPresenter != null && !techPresenter.isDisposed()) {
                      techPresenter.createContent();
                      indicator.dispose();
                      getForm().layout(true, true);
                      getForm().updateToolBar();
                      getForm().reflow(true);
                    }
                  }
                });
              }
            }
          }
          return Status.OK_STATUS;
        }
      };
      j.setSystem(true);
      j.schedule();
      techSection.setExpanded(wasSectionExpanded(SECTION_ID_TECHNOLOGY, true));
    }
  }

  protected IScoutBundle getScoutProject() {
    return getPage().getScoutBundle();
  }

  private void refreshProductLaunchPresenters(IFile[] productFiles) {
    ISection section = getSection(SECTION_ID_PRODUCT_LAUNCHER);
    for (ProductLaunchPresenter p : m_launchPresenters) {
      p.dispose();
    }
    m_launchPresenters.clear();

    Arrays.sort(productFiles, 0, productFiles.length, new Comparator<IFile>() {
      @Override
      public int compare(IFile o1, IFile o2) {
        String serverKeyWord = "server";
        boolean o1IsServer = o1.getName().contains(serverKeyWord);
        boolean o2IsServer = o2.getName().contains(serverKeyWord);
        if (o1IsServer == o2IsServer) {
          return o1.getName().compareTo(o2.getName());
        }
        else {
          return Boolean.valueOf(o2IsServer).compareTo(Boolean.valueOf(o1IsServer));
        }
      }
    });

    for (IFile prodFile : productFiles) {
      try {
        ProductLaunchPresenter presenter = new ProductLaunchPresenter(getFormToolkit(), section.getSectionClient(), prodFile);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        layoutData.widthHint = 200;
        presenter.getContainer().setLayoutData(layoutData);
        m_launchPresenters.add(presenter);
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("Unable to create product file presenter for file '" + prodFile.getFullPath().toOSString() + "'.", e);
      }
    }
    getForm().layout(true, true);
    getForm().reflow(true);
  }

  @Override
  public void init(IMemento memento) {
    refreshProductLaunchPresenters(getProjectProductLaunchers(getScoutProject().getSymbolicName()));
  }

  @Override
  public void save(IMemento memento) {
    IFile[] files = new IFile[m_launchPresenters.size()];
    for (int i = 0; i < files.length; i++) {
      files[i] = m_launchPresenters.get(i).getProductFile();
    }
    saveProjectProductLaunchers(getScoutProject().getSymbolicName(), files);
  }

  public static void saveProjectProductLaunchers(String projectName, IFile[] productFiles) {
    StringBuilder mementoString = new StringBuilder();
    for (int i = 0; i < productFiles.length; i++) {
      mementoString.append(productFiles[i].getFullPath());
      if (i < productFiles.length - 1) {
        mementoString.append(",");
      }
    }
    IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ScoutSdkUi.getDefault().getBundle().getSymbolicName());
    node.put(PROJECT_PROD_LAUNCHERS + "_" + projectName, mementoString.toString());
    try {
      node.flush();
    }
    catch (BackingStoreException e) {
      ScoutSdkUi.logError("unable to persist project product launcher settings.", e);
    }
  }

  public static void addProjectProductLauncher(String projectName, IFile productFile) {
    IFile[] existingLaunchers = getProjectProductLaunchers(projectName);
    IPath path = productFile.getFullPath();
    for (IFile existing : existingLaunchers) {
      if (existing.getFullPath().equals(path)) {
        return; /* this entry already exists */
      }
    }

    IFile[] newProdFiles = new IFile[existingLaunchers.length + 1];
    System.arraycopy(existingLaunchers, 0, newProdFiles, 0, existingLaunchers.length);
    newProdFiles[existingLaunchers.length] = productFile;
    saveProjectProductLaunchers(projectName, newProdFiles);
  }

  public static IFile[] getProjectProductLaunchers(String projectName) {
    ArrayList<IFile> products = new ArrayList<>();
    IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ScoutSdkUi.getDefault().getBundle().getSymbolicName());
    String mementoProducts = node.get(PROJECT_PROD_LAUNCHERS + "_" + projectName, "");
    if (!StringUtility.isNullOrEmpty(mementoProducts)) {
      String[] productLocations = mementoProducts.split(",\\s*");
      if (productLocations != null && productLocations.length > 0) {
        for (String productPath : productLocations) {
          if (!StringUtility.isNullOrEmpty(productPath)) {
            IFile productFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(productPath));
            if (productFile != null && productFile.exists()) {
              products.add(productFile);
            }
          }
        }
      }
    }
    return products.toArray(new IFile[products.size()]);
  }
}
