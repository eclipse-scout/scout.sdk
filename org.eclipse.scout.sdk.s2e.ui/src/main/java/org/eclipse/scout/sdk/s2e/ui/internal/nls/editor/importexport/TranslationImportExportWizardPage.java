/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.importexport;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * <h4>SelectNlsExporterWizardPage</h4>
 *
 * @since 1.1.0 (12.11.2010)
 */
public class TranslationImportExportWizardPage extends WizardPage {

  private final TranslationManager m_project;
  private final List<TranslationImportExportWizardExtension> m_extensions;
  private IWizardPage m_nextPage;

  public TranslationImportExportWizardPage(String title, String description, TranslationManager project, List<TranslationImportExportWizardExtension> extensions) {
    super(TranslationImportExportWizardPage.class.getName());
    m_project = project;
    m_extensions = extensions;
    setTitle(title);
    setDescription(description);
  }

  @Override
  public void createControl(Composite parent) {
    var table = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    StructuredViewer viewer = new TableViewer(table);
    var provider = new P_TableContentProvider();
    viewer.setLabelProvider(provider);
    viewer.setContentProvider(provider);
    viewer.setInput(provider);
    viewer.addDoubleClickListener(event -> {
      var selection = (IStructuredSelection) event.getSelection();
      TranslationImportExportWizardExtension extension = null;
      if (!selection.isEmpty()) {
        extension = (TranslationImportExportWizardExtension) selection.getFirstElement();
      }
      handleWizardExtensionSelected(extension);
      var page = getNextPage();
      if (page != null) {
        // show the next page
        var container = getWizard().getContainer();
        if (container != null) {
          container.showPage(page);

        }
      }
    });
    viewer.addSelectionChangedListener(event -> {
      var selection = (IStructuredSelection) event.getSelection();
      TranslationImportExportWizardExtension extension = null;
      if (!selection.isEmpty()) {
        extension = (TranslationImportExportWizardExtension) selection.getFirstElement();
      }
      handleWizardExtensionSelected(extension);
    });
    setControl(table);
  }

  private void handleWizardExtensionSelected(TranslationImportExportWizardExtension extension) {
    var pageComplete = false;
    if (extension != null) {
      var wizard = extension.createWizard();
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
      return m_extensions.toArray();
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return ((TranslationImportExportWizardExtension) element).getName();
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
    public void removeListener(ILabelProviderListener listener) {
      // nop
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }
  }
}
