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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.fields.table.ISeparator;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.tablecolumn.TableColumnNewWizard.CONTINUE_OPERATION;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link TableColumnNewWizardPage1}</h3> ...
 */
public class TableColumnNewWizardPage1 extends AbstractWorkspaceWizardPage {
  private final IType iSmartColumn = TypeUtility.getType(IRuntimeClasses.ISmartColumn);

  private IType m_declaringType;
  private CONTINUE_OPERATION m_nextOperation;

  private FilteredTable m_filteredTable;
  private Object m_currentSelection;
  private IType m_selectedTemplate;
  private IWizardPage m_nextPage;
  private String m_name;
  private SiblingProposal m_sibling;

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
    m_filteredTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        if (m_currentSelection != null) {
          m_filteredTable.getViewer().update(m_currentSelection, new String[]{"label"});
        }
        m_currentSelection = null;
        m_selectedTemplate = null;

        StructuredSelection selection = (StructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          m_currentSelection = selection.getFirstElement();
          if (!(m_currentSelection instanceof ISeparator)) {
            m_selectedTemplate = (IType) selection.getFirstElement();
          }
          validateNextPage();
          pingStateChanging();
        }
        if (m_currentSelection != null) {
          m_filteredTable.getViewer().update(m_currentSelection, new String[]{"label"});
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
          if (selectedItem instanceof ISeparator) {
            return;
          }
          m_selectedTemplate = (IType) selectedItem;
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

    P_TableContentProvider provider = new P_TableContentProvider();
    m_filteredTable.getViewer().setLabelProvider(provider);
    m_filteredTable.getViewer().setContentProvider(provider);
    m_filteredTable.getViewer().setInput(provider);
    m_filteredTable.getViewer().setSorter(provider);

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_filteredTable.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
  }

  protected void validateNextPage() {
    if (m_selectedTemplate == null) {
      m_nextPage = null;
    }
    else {
      ITypeHierarchy selectedSuperTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(m_selectedTemplate);
      if (selectedSuperTypeHierarchy != null && selectedSuperTypeHierarchy.contains(iSmartColumn)) {
        SmartTableColumnNewWizard wizard = new SmartTableColumnNewWizard(m_nextOperation);
        wizard.initWizard(m_declaringType);
        wizard.setSuperType(m_selectedTemplate);
        // forward properties to next page
        wizard.getSmartTableColumnNewWizardPage().setTypeName(getTypeName());
        wizard.getSmartTableColumnNewWizardPage().setSibling(getSibling());
        m_nextPage = wizard.getPages()[0];
      }
      else {
        DefaultTableColumnNewWizard wizard = new DefaultTableColumnNewWizard(m_nextOperation);
        wizard.initWizard(m_declaringType);
        wizard.setSuperType(m_selectedTemplate);
        // forward properties to next page
        wizard.getDefaultTableColumnNewWizardPage().setTypeName(getTypeName());
        wizard.getDefaultTableColumnNewWizardPage().setSibling(getSibling());
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
      if (((IType) row).equals(selectedType)) {
        m_filteredTable.getViewer().setSelection(new StructuredSelection(selectedType));
      }
    }
    validateNextPage();
  }

  public IType getSelectedSuperType() {
    return m_selectedTemplate;
  }

  public String getTypeName() {
    return m_name;
  }

  public void setTypeName(String name) {
    m_name = name;
  }

  public SiblingProposal getSibling() {
    return m_sibling;
  }

  public void setSibling(SiblingProposal sibling) {
    m_sibling = sibling;
  }

  private final class P_TableContentProvider extends ViewerSorter implements IStructuredContentProvider, ITableLabelProvider {
    private final Object[] m_templates;
    private final HashSet<String> m_shortList;

    private P_TableContentProvider() {
      List<Object> templates = new ArrayList<Object>();
      IJavaProject javaProject = m_declaringType.getJavaProject();

      IType stringCol = RuntimeClasses.getSuperType(IRuntimeClasses.IStringColumn, javaProject);
      IType boolCol = RuntimeClasses.getSuperType(IRuntimeClasses.IBooleanColumn, javaProject);
      IType dateCol = RuntimeClasses.getSuperType(IRuntimeClasses.IDateColumn, javaProject);
      IType doubleCol = RuntimeClasses.getSuperType(IRuntimeClasses.IDoubleColumn, javaProject);
      IType intCol = RuntimeClasses.getSuperType(IRuntimeClasses.IIntegerColumn, javaProject);
      IType longCol = RuntimeClasses.getSuperType(IRuntimeClasses.ILongColumn, javaProject);
      IType smartCol = RuntimeClasses.getSuperType(IRuntimeClasses.ISmartColumn, javaProject);
      IType bigDecCol = RuntimeClasses.getSuperType(IRuntimeClasses.IBigDecimalColumn, javaProject);
      IType bigIntCol = RuntimeClasses.getSuperType(IRuntimeClasses.IBigIntegerColumn, javaProject);

      templates.add(stringCol);
      templates.add(boolCol);
      templates.add(dateCol);
      templates.add(doubleCol);
      templates.add(intCol);
      templates.add(longCol);
      templates.add(smartCol);
      templates.add(bigDecCol);
      templates.add(bigIntCol);

      m_shortList = new HashSet<String>(9);
      m_shortList.add(stringCol.getFullyQualifiedName());
      m_shortList.add(boolCol.getFullyQualifiedName());
      m_shortList.add(dateCol.getFullyQualifiedName());
      m_shortList.add(doubleCol.getFullyQualifiedName());
      m_shortList.add(intCol.getFullyQualifiedName());
      m_shortList.add(longCol.getFullyQualifiedName());
      m_shortList.add(smartCol.getFullyQualifiedName());
      m_shortList.add(bigDecCol.getFullyQualifiedName());
      m_shortList.add(bigIntCol.getFullyQualifiedName());

      templates.add(new ISeparator() {
      });

      IType iColumn = TypeUtility.getType(IRuntimeClasses.IColumn);
      Set<IType> abstractColumnsOnClasspath = TypeUtility.getAbstractTypesOnClasspath(iColumn, javaProject, TypeFilters.getPrimaryTypeFilter());
      for (IType t : abstractColumnsOnClasspath) {
        if (!m_shortList.contains(t.getFullyQualifiedName())) {
          templates.add(t);
        }
      }

      m_templates = templates.toArray(new Object[templates.size()]);
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_templates;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        if (element instanceof ISeparator) {
          return ScoutSdkUi.getImage(ScoutSdkUi.Separator);
        }
        return ScoutSdkUi.getImage(ScoutSdkUi.FormField);
      }
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      if (columnIndex == 0) {
        if (element instanceof ISeparator) {
          return "------------------ more columns ------------------";
        }

        StringBuilder label = new StringBuilder();

        IType t = (IType) element;
        String typeName = t.getElementName();
        if (typeName.toLowerCase().startsWith("abstract")) {
          typeName = typeName.substring("abstract".length());
        }
        label.append(typeName);

        StructuredSelection selection = (StructuredSelection) m_filteredTable.getViewer().getSelection();
        if (selection.toList().contains(element)) {
          label.append(" - ").append(t.getFullyQualifiedName());
        }
        return label.toString();
      }
      return null;
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
      return "label".equals(property);
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {

      CompositeObject comp1;
      if (e1 instanceof ISeparator) {
        comp1 = new CompositeObject(2);
      }
      else {
        IType modelType = (IType) e1;
        if (m_shortList.contains(modelType.getFullyQualifiedName())) {
          comp1 = new CompositeObject(1, modelType.getElementName(), modelType.getFullyQualifiedName());
        }
        else {
          comp1 = new CompositeObject(3, modelType.getElementName(), modelType.getFullyQualifiedName());
        }
      }

      CompositeObject comp2;
      if (e2 instanceof ISeparator) {
        comp2 = new CompositeObject(2);
      }
      else {
        IType modelType = (IType) e2;
        if (m_shortList.contains(modelType.getFullyQualifiedName())) {
          comp2 = new CompositeObject(1, modelType.getElementName(), modelType.getFullyQualifiedName());
        }
        else {
          comp2 = new CompositeObject(3, modelType.getElementName(), modelType.getFullyQualifiedName());
        }
      }

      return comp1.compareTo(comp2);
    }
  } // end class P_TableContentProvider
}
