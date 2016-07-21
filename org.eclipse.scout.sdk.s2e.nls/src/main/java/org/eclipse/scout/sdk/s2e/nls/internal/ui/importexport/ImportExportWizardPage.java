/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.importexport;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scout.sdk.s2e.nls.importexport.AbstractImportExportWizard;
import org.eclipse.scout.sdk.s2e.nls.importexport.NlsExportImportExtensionPoints;
import org.eclipse.scout.sdk.s2e.nls.importexport.WizardExtension;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * <h4>SelectNlsExporterWizardPage</h4>
 *
 * @author Andreas Hoegger
 * @since 1.1.0 (12.11.2010)
 */
public class ImportExportWizardPage extends WizardPage {

  private IWizardPage m_nextPage;
  private final String m_extensionPointId;
  private final INlsProject m_project;

  public ImportExportWizardPage(String title, String description, INlsProject project, String extensionPointId) {
    super(ImportExportWizardPage.class.getName());
    m_project = project;
    m_extensionPointId = extensionPointId;
    setTitle(title);
    setDescription(description);
  }

  @Override
  public void createControl(Composite parent) {
    Table table = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    TableViewer viewer = new TableViewer(table);
    P_TableContentProvider provider = new P_TableContentProvider();
    viewer.setLabelProvider(provider);
    viewer.setContentProvider(provider);
    viewer.setInput(provider);
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        WizardExtension extension = null;
        if (!selection.isEmpty()) {
          extension = (WizardExtension) selection.getFirstElement();
        }
        handleWizardExtensionSelected(extension);
        IWizardPage page = getNextPage();
        if (page != null) {
          // show the next page
          IWizardContainer container = getWizard().getContainer();
          if (container != null) {
            container.showPage(page);

          }
        }
      }
    });
    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        WizardExtension extension = null;
        if (!selection.isEmpty()) {
          extension = (WizardExtension) selection.getFirstElement();
        }
        handleWizardExtensionSelected(extension);
      }
    });
    setControl(table);
  }

  private void handleWizardExtensionSelected(WizardExtension extension) {
    boolean pageComplete = false;
    if (extension != null) {
      AbstractImportExportWizard wizard = extension.createWizard();
      wizard.setNlsProject(m_project);
      wizard.addPages();
      if (wizard.getPageCount() > 0) {
        setNextPage(wizard.getPages()[0]);
        pageComplete = true;
      }

    }
    setPageComplete(pageComplete);
  }

  @Override
  public IWizardPage getNextPage() {
    return m_nextPage;
  }

  private void setNextPage(IWizardPage nextPage) {
    m_nextPage = nextPage;
  }

  private class P_TableContentProvider implements IStructuredContentProvider, ITableLabelProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      List<WizardExtension> exts = NlsExportImportExtensionPoints.getExtensions(m_extensionPointId);
      return exts.toArray(new WizardExtension[exts.size()]);
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return ((WizardExtension) element).getName();
      }
      return null;
    }

    @Override
    public void dispose() {
      // nop
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // nop
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
      // nop
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
      // nop
    }
  }
}
