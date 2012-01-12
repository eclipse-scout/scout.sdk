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
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.dialog.ProductSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ProjectVersionPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.part.Section;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.ProductLaunchPresenter;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IMemento;

/**
 * <h3>{@link ScoutProjectPropertyPart}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.09.2010
 */
public class ScoutProjectPropertyPart extends AbstractSinglePageSectionBasedViewPart {

  private static final String SECTION_ID_FILTER = "section.filter";
  private static final String SECTION_ID_PRODUCT_LAUNCHER = "section.productLauncher";
  private static final String SECTION_ID_VERSION = "section.version";

  private ArrayList<ProductLaunchPresenter> m_launchPresenters = new ArrayList<ProductLaunchPresenter>();

  @Override
  protected void createSections() {
    // filter
    ISection filterSection = createSection(SECTION_ID_FILTER, Texts.get("Filter"));
    PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), filterSection.getSectionClient(), getPage());
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    filterPresenter.getContainer().setLayoutData(layoutData);
    getSection(SECTION_ID_FILTER).setExpanded(false);

    // version
    ISection versionSection = createSection(SECTION_ID_VERSION, Texts.get("ProjectVersion"));
    ProjectVersionPresenter versionPresenter = new ProjectVersionPresenter(getFormToolkit(), versionSection.getSectionClient(), getScoutProject());
    GridData versionLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    versionLayoutData.widthHint = 200;
    versionPresenter.getContainer().setLayoutData(versionLayoutData);
    getSection(SECTION_ID_VERSION).setExpanded(true);

    // product launchers
    Section linkSection = (Section) createSection(SECTION_ID_PRODUCT_LAUNCHER, Texts.get("ProductLauncher"));
    ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
    ToolBar toolbar = toolBarManager.createControl(linkSection.getUiSection());
    final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
    toolbar.setCursor(handCursor);
    Action action = new Action() {
      @Override
      public void run() {
        ArrayList<IFile> productFiles = new ArrayList<IFile>();
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
    toolBarManager.add(action);
    toolBarManager.update(true);

    linkSection.getUiSection().setTextClient(toolbar);
  }

  protected IScoutProject getScoutProject() {
    return (IScoutProject) getPage().getScoutResource();
  }

  private void refreshProductLaunchPresenters(IFile[] productFiles) {
    ISection section = getSection(SECTION_ID_PRODUCT_LAUNCHER);
    for (ProductLaunchPresenter p : m_launchPresenters) {
      p.dispose();
    }
    m_launchPresenters.clear();
    Composite sectionClient = section.getSectionClient();
    TreeMap<CompositeObject, P_ProductFile> orderedProducts = new TreeMap<CompositeObject, P_ProductFile>();
    for (IFile productFile : productFiles) {
      if (productFile != null && productFile.exists()) {
        IScoutBundle scoutBundle = ScoutSdkCore.getScoutWorkspace().getScoutBundle(productFile.getProject());
        int productType = -1;
        if (scoutBundle != null) {
          productType = scoutBundle.getType();
        }
        orderedProducts.put(new CompositeObject(-productType, productFile.getName(), productFile), new P_ProductFile(productFile, scoutBundle));
      }
    }
    for (Entry<CompositeObject, P_ProductFile> entry : orderedProducts.entrySet()) {
      P_ProductFile productFile = entry.getValue();
      ProductLaunchPresenter presenter = new ProductLaunchPresenter(getFormToolkit(), sectionClient, productFile.getFile(), productFile.getBundle());
      GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      layoutData.widthHint = 200;
      presenter.getContainer().setLayoutData(layoutData);
      m_launchPresenters.add(presenter);
    }
    getForm().layout(true, true);
    getForm().reflow(true);
  }

  @Override
  public void init(IMemento memento) {
    refreshProductLaunchPresenters(SdkProperties.getProjectProductLaunchers(getScoutProject().getProjectName()));
  }

  @Override
  public void save(IMemento memento) {
    IFile[] files = new IFile[m_launchPresenters.size()];
    for (int i = 0; i < files.length; i++) {
      files[i] = m_launchPresenters.get(i).getProductFile();
    }
    SdkProperties.saveProjectProductLaunchers(getScoutProject().getProjectName(), files);
  }

  private class P_ProductFile {
    private final IScoutBundle m_bundle;
    private final IFile m_file;

    public P_ProductFile(IFile file, IScoutBundle bundle) {
      m_file = file;
      m_bundle = bundle;
    }

    /**
     * @return the bundleType
     */
    public IScoutBundle getBundle() {
      return m_bundle;
    }

    /**
     * @return the file
     */
    public IFile getFile() {
      return m_file;
    }
  }
}
