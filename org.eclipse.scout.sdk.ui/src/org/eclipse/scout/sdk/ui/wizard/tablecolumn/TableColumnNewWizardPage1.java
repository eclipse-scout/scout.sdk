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
import org.eclipse.jdt.core.IJavaProject;
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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.tablecolumn.TableColumnNewWizard.CONTINUE_OPERATION;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
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
  final IType iColumn = TypeUtility.getType(RuntimeClasses.IColumn);
  final IType iSmartColumn = TypeUtility.getType(RuntimeClasses.ISmartColumn);

  private IType m_declaringType;
  private boolean m_showAllTemplates;
  private CONTINUE_OPERATION m_nextOperation;

  private FilteredTable m_filteredTable;
  private Button m_showAllTemplatesField;
  private P_BCTypeTemplate m_selectedTemplate;
  private IWizardPage m_nextPage;

  public TableColumnNewWizardPage1(IType declaringType, CONTINUE_OPERATION op) {
    super(TableColumnNewWizardPage1.class.getName());
    setTitle(Texts.get("TableColumnTemplates"));
    setDescription(Texts.get("ChooseATemplateForYourTableColumn"));
    m_declaringType = declaringType;
    m_nextOperation = op;
  }

  @Override
  protected void createContent(Composite parent) {
    m_filteredTable = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_filteredTable.getViewer().addFilter(new P_ModeFilter());
    m_filteredTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
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
    IJavaProject javaProject = m_declaringType.getJavaProject();
    templates.put(RuntimeClasses.getSuperTypeName(RuntimeClasses.IStringColumn, javaProject),
        new P_BCTypeTemplate(Texts.get("StringColumn"), RuntimeClasses.getSuperType(RuntimeClasses.IStringColumn, javaProject)));
    templates.put(RuntimeClasses.getSuperTypeName(RuntimeClasses.IBooleanColumn, javaProject),
        new P_BCTypeTemplate(Texts.get("BooleanColumn"), RuntimeClasses.getSuperType(RuntimeClasses.IBooleanColumn, javaProject)));
    templates.put(RuntimeClasses.getSuperTypeName(RuntimeClasses.IDateColumn, javaProject),
        new P_BCTypeTemplate(Texts.get("DateColumn"), RuntimeClasses.getSuperType(RuntimeClasses.IDateColumn, javaProject)));
    templates.put(RuntimeClasses.getSuperTypeName(RuntimeClasses.IDoubleColumn, javaProject),
        new P_BCTypeTemplate(Texts.get("DoubleColumn"), RuntimeClasses.getSuperType(RuntimeClasses.IDoubleColumn, javaProject)));
    templates.put(RuntimeClasses.getSuperTypeName(RuntimeClasses.IIntegerColumn, javaProject),
        new P_BCTypeTemplate(Texts.get("IntegerColumn"), RuntimeClasses.getSuperType(RuntimeClasses.IIntegerColumn, javaProject)));
    templates.put(RuntimeClasses.getSuperTypeName(RuntimeClasses.ILongColumn, javaProject),
        new P_BCTypeTemplate(Texts.get("LongColumn"), RuntimeClasses.getSuperType(RuntimeClasses.ILongColumn, javaProject)));
    templates.put(RuntimeClasses.getSuperTypeName(RuntimeClasses.ISmartColumn, javaProject),
        new P_BCTypeTemplate(Texts.get("SmartColumn"), RuntimeClasses.getSuperType(RuntimeClasses.ISmartColumn, javaProject)));

    ITypeHierarchy columnHierarchy = TypeUtility.getPrimaryTypeHierarchy(iColumn);
    for (IType t : columnHierarchy.getAllClasses(TypeFilters.getAbstractOnClasspath(javaProject))) {
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
    m_showAllTemplatesField.setText(Texts.get("ShowAllTemplates"));
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
        SmartTableColumnNewWizard wizard = new SmartTableColumnNewWizard(m_nextOperation);
        wizard.initWizard(m_declaringType);
        wizard.setSuperType(m_selectedTemplate.getType());
        m_nextPage = wizard.getPages()[0];
      }
      else {
        DefaultTableColumnNewWizard wizard = new DefaultTableColumnNewWizard(m_nextOperation);
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
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ATemplateMustBeSelected")));
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
      if (isShowAllTemplates()) {
        return ((P_BCTypeTemplate) element).getType().getElementName();
      }
      else {
        return ((P_BCTypeTemplate) element).getTemplateName();
      }
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
