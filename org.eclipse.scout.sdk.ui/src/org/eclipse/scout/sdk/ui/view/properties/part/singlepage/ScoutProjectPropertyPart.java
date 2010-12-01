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
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.ProductSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.part.Section;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.ProductLaunchPresenter;
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

  private ArrayList<ProductLaunchPresenter> m_launchPresenters = new ArrayList<ProductLaunchPresenter>();

  @Override
  protected void createSections() {
    ISection filterSection = createSection(SECTION_ID_FILTER, "Filter");
    PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), filterSection.getSectionClient(), getPage());
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    filterPresenter.getContainer().setLayoutData(layoutData);
    getSection(SECTION_ID_FILTER).setExpanded(false);
    // link area
    Section linkSection = (Section) createSection(SECTION_ID_PRODUCT_LAUNCHER, "Product launcher");
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
        dialog.setSelectedProducts(productFiles.toArray(new IFile[productFiles.size()]));
        if (dialog.open() == Dialog.OK) {
          updateProducts(dialog.getSelectedProducts());
        }
      }
    };
    action.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolEdit));
    action.setToolTipText("edit content...");
    toolBarManager.add(action);
    toolBarManager.update(true);

    linkSection.getUiSection().setTextClient(toolbar);
  }

  protected IScoutProject getScoutProject() {
    return (IScoutProject) getPage().getScoutResource();
  }

  private void updateProducts(IFile[] products) {
    ISection section = getSection(SECTION_ID_PRODUCT_LAUNCHER);
    for (ProductLaunchPresenter p : m_launchPresenters) {
      p.dispose();
    }
    m_launchPresenters.clear();
    Composite sectionClient = section.getSectionClient();
    TreeMap<CompositeObject, P_ProductFile> orderedProducts = new TreeMap<CompositeObject, P_ProductFile>();
    for (IFile productFile : products) {
      if (productFile != null && productFile.exists()) {
        IScoutBundle scoutBundle = ScoutSdk.getScoutWorkspace().getScoutBundle(productFile.getProject());
        int productType = -1;
        if (scoutBundle != null) {
          productType = scoutBundle.getType();
        }
        orderedProducts.put(new CompositeObject(-productType, productFile.getName(), productFile), new P_ProductFile(productFile, productType));
      }
    }
    for (Entry<CompositeObject, P_ProductFile> entry : orderedProducts.entrySet()) {
      P_ProductFile productFile = entry.getValue();
      ProductLaunchPresenter presenter = new ProductLaunchPresenter(getFormToolkit(), sectionClient, productFile.getFile(), productFile.getBundleType());
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
    ArrayList<IFile> products = new ArrayList<IFile>();
    IEclipsePreferences node = new InstanceScope().getNode(ScoutSdkUi.getDefault().getBundle().getSymbolicName());
    String mementoProducts = node.get(SECTION_ID_PRODUCT_LAUNCHER + "_" + getScoutProject().getProjectName(), "");
    if (!StringUtility.isNullOrEmpty(mementoProducts)) {
      String[] productLocations = mementoProducts.split(",\\s*");
      if (productLocations != null) {
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
    updateProducts(products.toArray(new IFile[products.size()]));

  }

  @Override
  public void save(IMemento memento) {
    StringBuilder mementoString = new StringBuilder();
    ProductLaunchPresenter[] presenters = m_launchPresenters.toArray(new ProductLaunchPresenter[m_launchPresenters.size()]);
    for (int i = 0; i < presenters.length; i++) {
      mementoString.append(presenters[i].getProductFile().getFullPath());
      if (i < presenters.length - 1) {
        mementoString.append(",");
      }
    }
    IEclipsePreferences node = new InstanceScope().getNode(ScoutSdkUi.getDefault().getBundle().getSymbolicName());
    node.put(SECTION_ID_PRODUCT_LAUNCHER + "_" + getScoutProject().getProjectName(), mementoString.toString());
  }

  private class P_ProductFile {
    private final int m_bundleType;
    private final IFile m_file;

    public P_ProductFile(IFile file, int bundleType) {
      m_file = file;
      m_bundleType = bundleType;
    }

    /**
     * @return the bundleType
     */
    public int getBundleType() {
      return m_bundleType;
    }

    /**
     * @return the file
     */
    public IFile getFile() {
      return m_file;
    }
  }
}
