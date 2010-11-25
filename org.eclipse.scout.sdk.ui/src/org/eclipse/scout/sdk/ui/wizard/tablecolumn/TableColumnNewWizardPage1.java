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
package org.eclipse.scout.sdk.ui.wizard.tablecolumn;

import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link TableColumnNewWizardPage1}</h3> ...
 */
public class TableColumnNewWizardPage1 extends AbstractWorkspaceWizardPage {
  final IType iColumn = ScoutSdk.getType(RuntimeClasses.IColumn);
  final IType iSmartColumn = ScoutSdk.getType(RuntimeClasses.ISmartColumn);
  private IType m_declaringType;
  private boolean m_showAllTemplates;

  private FilteredTable m_filteredTable;
  private Button m_showAllTemplatesField;

  private P_BCTypeTemplate m_selectedTemplate;
  private IWizardPage m_nextPage;

  public TableColumnNewWizardPage1(IType declaringType) {
    super(TableColumnNewWizardPage1.class.getName());
    setTitle("Table Column Templates");
    setDefaultMessage("Choose a template for your table column.");
    m_declaringType = declaringType;
  }

  @Override
  protected void createContent(Composite parent) {
    m_filteredTable = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_filteredTable.getViewer().addFilter(new P_ModeFilter());
    m_filteredTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          m_selectedTemplate = (P_BCTypeTemplate) selection.getFirstElement();
          validateNextPage();
          pingStateChanging();
        }

      }
    });
    m_filteredTable.getViewer().addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        Object selectedItem = null;
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          selectedItem = selection.getFirstElement();
          m_selectedTemplate = (P_BCTypeTemplate) selectedItem;
          validateNextPage();
          IWizardPage page = getNextPage();
          if (page == null) {
            // something must have happend getting the next page
            return;
          }

          // show the next page
          IWizardContainer container = getWizard().getContainer();
          if (container != null) {
            container.showPage(page);
          }
        }
      }
    });

    HashMap<String, P_BCTypeTemplate> templates = new HashMap<String, P_BCTypeTemplate>();
    templates.put(RuntimeClasses.AbstractStringColumn, new P_BCTypeTemplate("String Column", ScoutSdk.getType(RuntimeClasses.AbstractStringColumn)));
    templates.put(RuntimeClasses.AbstractBooleanColumn, new P_BCTypeTemplate("Boolean Column", ScoutSdk.getType(RuntimeClasses.AbstractBooleanColumn)));
    templates.put(RuntimeClasses.AbstractDateColumn, new P_BCTypeTemplate("Date Column", ScoutSdk.getType(RuntimeClasses.AbstractDateColumn)));
    templates.put(RuntimeClasses.AbstractDoubleColumn, new P_BCTypeTemplate("Double Column", ScoutSdk.getType(RuntimeClasses.AbstractDoubleColumn)));
    templates.put(RuntimeClasses.AbstractIntegerColumn, new P_BCTypeTemplate("Integer Column", ScoutSdk.getType(RuntimeClasses.AbstractIntegerColumn)));
    templates.put(RuntimeClasses.AbstractLongColumn, new P_BCTypeTemplate("Long Column", ScoutSdk.getType(RuntimeClasses.AbstractLongColumn)));
    templates.put(RuntimeClasses.AbstractTimeColumn, new P_BCTypeTemplate("Time Column", ScoutSdk.getType(RuntimeClasses.AbstractTimeColumn)));
    templates.put(RuntimeClasses.AbstractSmartColumn, new P_BCTypeTemplate("Smart Column", ScoutSdk.getType(RuntimeClasses.AbstractSmartColumn)));

    ITypeHierarchy columnHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iColumn);
    for (IType t : columnHierarchy.getAllClasses(TypeFilters.getAbstractOnClasspath(m_declaringType.getJavaProject()))) {
      if (!templates.containsKey(t.getFullyQualifiedName())) {
        templates.put(t.getFullyQualifiedName(), new P_BCTypeTemplate(null, t));
      }
    }
    P_TableContentProvider provider = new P_TableContentProvider(templates.values().toArray(new P_BCTypeTemplate[templates.size()]));
    m_filteredTable.getViewer().setLabelProvider(provider);
    m_filteredTable.getViewer().setContentProvider(provider);
    m_filteredTable.getViewer().setInput(provider);

    m_showAllTemplatesField = new Button(parent, SWT.CHECK);
    m_showAllTemplatesField.setSelection(false);
    m_showAllTemplatesField.setText("Show all templates");
    m_showAllTemplatesField.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setShowAllTemplates(m_showAllTemplatesField.getSelection());

        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_filteredTable.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
  }

  protected void validateNextPage() {
    if (m_selectedTemplate == null) {
      m_nextPage = null;
    }
    else {
      org.eclipse.jdt.core.ITypeHierarchy selectedSuperTypeHierarchy = null;
      try {
        selectedSuperTypeHierarchy = m_selectedTemplate.getType().newSupertypeHierarchy(null);
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logError("could not build type hierarchy of '" + m_selectedTemplate.getType().getFullyQualifiedName() + "'.", e);
      }
      if (selectedSuperTypeHierarchy != null && selectedSuperTypeHierarchy.contains(iSmartColumn)) {
        SmartTableColumnNewWizard wizard = new SmartTableColumnNewWizard();
        wizard.initWizard(m_declaringType);
        wizard.setSuperType(m_selectedTemplate.getType());
        m_nextPage = wizard.getPages()[0];
      }
      else {
        DefaultTableColumnNewWizard wizard = new DefaultTableColumnNewWizard();
        wizard.initWizard(m_declaringType);
        wizard.setSuperType(m_selectedTemplate.getType());
        m_nextPage = wizard.getPages()[0];
      }
    }
  }

  @Override
  public IWizardPage getNextPage() {
    return m_nextPage;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (m_selectedTemplate != null) {
      multiStatus.add(Status.OK_STATUS);
    }
    else {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "A template must be selected."));
    }
  }

  public void setSuperType(IType selectedType) {
    IStructuredContentProvider prov = (IStructuredContentProvider) m_filteredTable.getViewer().getContentProvider();
    for (Object row : prov.getElements(null)) {
      if (((P_BCTypeTemplate) row).getType().equals(selectedType)) {
        m_filteredTable.getViewer().setSelection(new StructuredSelection(selectedType));
      }
    }
    validateNextPage();
  }

  public boolean isShowAllTemplates() {
    return m_showAllTemplates;
  }

  public void setShowAllTemplates(boolean showAllTemplates) {
    try {
      setStateChanging(true);
      m_showAllTemplates = showAllTemplates;
      if (isControlCreated()) {
        m_showAllTemplatesField.setSelection(showAllTemplates);
        m_filteredTable.getViewer().refresh();
      }

    }
    finally {
      setStateChanging(false);
    }
  }

  public IType getSelectedSuperType() {
    return m_selectedTemplate.getType();
  }

  private class P_TableContentProvider implements IStructuredContentProvider, ITableLabelProvider {
    P_BCTypeTemplate[] m_templates;

    private P_TableContentProvider(P_BCTypeTemplate[] templates) {
      m_templates = templates;
    }

    public Object[] getElements(Object inputElement) {
      return m_templates;
    }

    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return ScoutSdkUi.getImage(ScoutSdkUi.IMG_FIELD_DEFAULT);
      }
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      if (isShowAllTemplates()) {
        return ((P_BCTypeTemplate) element).getType().getElementName();
      }
      else {
        return ((P_BCTypeTemplate) element).getTemplateName();
      }
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
  } // end class P_TableContentProvider

  private class P_BCTypeTemplate {
    private final IType m_type;
    private final String m_templateName;

    public P_BCTypeTemplate(String templateName, IType type) {
      m_templateName = templateName;
      m_type = type;

    }

    public IType getType() {
      return m_type;
    }

    public String getTemplateName() {
      return m_templateName;
    }

  } // end class P_BCTypeTemplate

  private class P_ModeFilter extends ViewerFilter {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (m_showAllTemplates) {
        return true;
      }
      else {
        return ((P_BCTypeTemplate) element).getTemplateName() != null;
      }
    }
  }

}
