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
package org.eclipse.scout.sdk.ui.internal.wizard.newproject;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.sdk.operation.project.template.EmptyTemplateOperation;
import org.eclipse.scout.sdk.operation.project.template.IScoutProjectTemplateOperation;
import org.eclipse.scout.sdk.operation.project.template.OutlineTemplateOperation;
import org.eclipse.scout.sdk.operation.project.template.SingleFormTemplateOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.internal.wizard.AbstractWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>{@link ScoutProjectTemplateWizardPage}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class ScoutProjectTemplateWizardPage extends AbstractWizardPage {

  private FilteredTable m_table;
  private IScoutProjectTemplateOperation m_selectedTemplate;
  private Label m_descriptionLabel;

  /**
   * @param pageName
   */
  public ScoutProjectTemplateWizardPage() {
    super(ScoutProjectTemplateWizardPage.class.getName());
    setTitle("Scout application templates");
  }

  @Override
  protected void createContent(Composite parent) {
    m_table = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_table.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IScoutProjectTemplateOperation selectedItem = null;
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          selectedItem = (IScoutProjectTemplateOperation) selection.getFirstElement();
        }
        handleSelection(selectedItem);
      }

    });

    ArrayList<IScoutProjectTemplateOperation> elements = new ArrayList<IScoutProjectTemplateOperation>();
    elements.add(new EmptyTemplateOperation());
    OutlineTemplateOperation outlineTemplate = new OutlineTemplateOperation();
    elements.add(outlineTemplate);
    SingleFormTemplateOperation singleFormTemplate = new SingleFormTemplateOperation();
    elements.add(singleFormTemplate);
    P_ContentProvider provider = new P_ContentProvider(elements.toArray(new IScoutProjectTemplateOperation[elements.size()]));
    m_table.getViewer().setLabelProvider(provider);
    m_table.getViewer().setContentProvider(provider);
    m_table.getViewer().setInput(provider);
    m_table.getViewer().setSelection(new StructuredSelection(singleFormTemplate));
    m_descriptionLabel = new Label(parent, SWT.SHADOW_ETCHED_IN | SWT.WRAP);
    m_descriptionLabel.setText(outlineTemplate.getDescription());

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_descriptionLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

  }

  private void handleSelection(IScoutProjectTemplateOperation selectedItem) {
    m_selectedTemplate = selectedItem;
    if (isControlCreated()) {
      String description = "";
      if (m_selectedTemplate != null) {
        description = m_selectedTemplate.getDescription();
      }
//    setMessage(description);
      m_descriptionLabel.setText(description);
    }

  }

  /**
   * @return the selectedTemplate
   */
  public IScoutProjectTemplateOperation getSelectedTemplate() {

    return m_selectedTemplate;
  }

  @Override
  public ScoutProjectNewWizard getWizard() {
    return (ScoutProjectNewWizard) super.getWizard();
  }

  public void performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws IllegalArgumentException, CoreException {
    m_selectedTemplate.run(monitor, workingCopyManager);
  }

  private class P_ContentProvider implements IStructuredContentProvider, ITableLabelProvider {

    private IScoutProjectTemplateOperation[] m_templates;

    public P_ContentProvider(IScoutProjectTemplateOperation[] templates) {
      m_templates = templates;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_templates;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      IScoutProjectTemplateOperation op = (IScoutProjectTemplateOperation) element;
      return op.getTemplateName();
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return ScoutSdkUi.getImage(ScoutSdkUi.Templates);
      }
      return null;
    }

    @Override
    public void dispose() {
      // TODO Auto-generated method stub

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // TODO Auto-generated method stub

    }

    @Override
    public void addListener(ILabelProviderListener listener) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
      // TODO Auto-generated method stub

    }

  }
}
