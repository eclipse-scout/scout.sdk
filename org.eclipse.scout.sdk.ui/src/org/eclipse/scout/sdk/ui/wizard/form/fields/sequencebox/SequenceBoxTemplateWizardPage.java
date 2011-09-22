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
package org.eclipse.scout.sdk.ui.wizard.form.fields.sequencebox;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.template.IContentTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DateFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DateTimeFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DoubleFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.IntegerFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.LongFromToTemplate;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.form.fields.EmptyTemplate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class SequenceBoxTemplateWizardPage extends AbstractWorkspaceWizardPage {

  private IType m_declaringType;
  private FilteredTable m_filteredTable;
  private IContentTemplate m_selectedTemplate;

  public SequenceBoxTemplateWizardPage(IType declaringType) {
    super(Texts.get("TemplatesForSequenceBox"));
    m_declaringType = declaringType;
  }

  @Override
  protected void createContent(Composite parent) {
    setTitle(Texts.get("Templates"));
    setDefaultMessage(Texts.get("ChooseATemplateForYourSequenceBox"));
    m_filteredTable = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_filteredTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          m_selectedTemplate = (IContentTemplate) selection.getFirstElement();
        }

      }
    });
    P_TableContentProvider provider = new P_TableContentProvider(new IContentTemplate[]{new EmptyTemplate(), new DateFromToTemplate(),
        new DateTimeFromToTemplate(), new IntegerFromToTemplate(), new DoubleFromToTemplate(), new LongFromToTemplate()});
    m_filteredTable.getViewer().setLabelProvider(provider);
    m_filteredTable.getViewer().setContentProvider(provider);
    m_filteredTable.getViewer().setInput(provider);
    // layout
    parent.setLayout(new GridLayout(1, true));
    m_filteredTable.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    if (m_selectedTemplate != null) {
      SequenceBoxNewWizardPage previousPage = (SequenceBoxNewWizardPage) getPreviousPage();
      IType sequenceBox = previousPage.getCreatedType();
      if (sequenceBox != null) {
        m_selectedTemplate.apply(sequenceBox, manager, monitor);
      }
    }

    return true;
  }

  /**
   * @return the selectedTemplate
   */
  public IContentTemplate getSelectedTemplate() {
    return m_selectedTemplate;
  }

  private class P_TableContentProvider implements IStructuredContentProvider, ITableLabelProvider {
    IContentTemplate[] m_templates;

    private P_TableContentProvider(IContentTemplate[] templates) {
      m_templates = templates;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_templates;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return ScoutSdkUi.getImage(ScoutSdkUi.FormField);
      }
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {

      return ((IContentTemplate) element).getName();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

  }
}
