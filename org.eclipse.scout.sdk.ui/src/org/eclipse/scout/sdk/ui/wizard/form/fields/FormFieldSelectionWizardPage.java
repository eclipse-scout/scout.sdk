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
package org.eclipse.scout.sdk.ui.wizard.form.fields;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.ui.extensions.IFormFieldExtension;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.fields.table.ISeparator;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.FormFieldExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>FormFieldSelectionWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 02.03.2010
 */
public class FormFieldSelectionWizardPage extends AbstractWorkspaceWizardPage {

  private IType iFormField = TypeUtility.getType(RuntimeClasses.IFormField);

  private final IType m_declaringType;
  private AbstractScoutWizardPage m_nextPage;
  private Object m_currentSelection;
  private HashSet<IType> m_modelTypeShortList;

  // ui fields
  private FilteredTable m_table;

  /**
   * @param pageName
   */
  public FormFieldSelectionWizardPage(IType declaringType) {
    super(FormFieldSelectionWizardPage.class.getName());
    m_declaringType = declaringType;
    setTitle(Texts.get("FormField"));
    setDescription(Texts.get("FormFieldDesc"));

  }

  @Override
  protected void createContent(Composite parent) {
    m_modelTypeShortList = new HashSet<IType>();
    ArrayList<Object> elements = new ArrayList<Object>();
    elements.add(new ISeparator() {
    });
    // entries
    IPrimaryTypeTypeHierarchy formFieldHierarchy = TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(RuntimeClasses.IFormField));
    IType[] abstractFormFields = formFieldHierarchy.getAllSubtypes(iFormField, TypeFilters.getAbstractOnClasspath(m_declaringType.getJavaProject()));
    for (IType formField : abstractFormFields) {
      IFormFieldExtension formFieldExtension = FormFieldExtensionPoint.findExtension(formField, 1);
      if (formFieldExtension != null && formFieldExtension.isInShortList()) {
        m_modelTypeShortList.add(formField);
      }
      elements.add(formField);
    }
    // ui
    m_table = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_table.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        if (m_currentSelection != null) {
          m_table.getViewer().refresh(m_currentSelection);
        }
        m_currentSelection = null;
        if (!event.getSelection().isEmpty()) {
          m_currentSelection = ((StructuredSelection) event.getSelection()).getFirstElement();
        }
        m_table.getViewer().refresh(m_currentSelection);
        handleSelection(m_currentSelection);
      }
    });

    m_table.getViewer().addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        Object selectedItem = null;
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          selectedItem = selection.getFirstElement();
        }
        handleSelection(selectedItem);
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
    });
    P_ContentProvider provider = new P_ContentProvider(elements.toArray());
    m_table.getViewer().setLabelProvider(provider);
    m_table.getViewer().setContentProvider(provider);
    m_table.getViewer().setInput(provider);
    m_table.getViewer().setSorter(new P_TableSorter());

    // layout
    parent.setLayout(new GridLayout(1, true));
    GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
    tableData.heightHint = 150;
    m_table.setLayoutData(tableData);
  }

  private void handleSelection(Object selectedItem) {

    AbstractFormFieldWizard wizard = null;
    if (selectedItem instanceof IType) {
      IType formField = (IType) selectedItem;
      wizard = (AbstractFormFieldWizard) FormFieldExtensionPoint.createNewWizard(formField);
      wizard.initWizard(m_declaringType);
      wizard.setSuperType(formField);
    }
    if (wizard != null) {
      m_nextPage = (AbstractScoutWizardPage) wizard.getPages()[0];
    }
    else {
      m_nextPage = null;
    }
    revalidate();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusFieldList());
  }

  private IStatus getStatusFieldList() {
    if (m_nextPage == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoFieldSelected"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public IWizardPage getNextPage() {
    return m_nextPage;
  }

  private class P_ContentProvider implements IStructuredContentProvider, ITableLabelProvider, IStyledLabelProvider {

    private Object[] m_elements;
    private Pattern m_typeNamePattern = Pattern.compile("^(Abstract|Abstract)?(.*)$");

    public P_ContentProvider(Object[] elements) {
      m_elements = elements;
    }

    @Override
    public StyledString getStyledText(Object element) {
      return null;
    }

    @Override
    public Image getImage(Object element) {
      return null;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_elements;
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
        StringBuilder label = new StringBuilder();
        if (element instanceof ISeparator) {
          return "-------------- more fields ------------------";
        }
        StructuredSelection selection = (StructuredSelection) m_table.getViewer().getSelection();

        IType t = (IType) element;
        String typeName = t.getElementName();
        if (typeName.toLowerCase().startsWith("abstract")) {
          typeName = typeName.substring("abstract".length());
        }
        label.append(typeName);
        if (selection.toList().contains(element)) {
          label.append("  (").append(t.getFullyQualifiedName()).append(")");
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
      return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }
  } // end class P_ByClassProvider

  private class P_TableSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {

      CompositeObject comp1;
      if (e1 instanceof ISeparator) {
        comp1 = new CompositeObject(2);
      }
      else {
        IType modelType = (IType) e1;
        if (m_modelTypeShortList.contains(modelType)) {
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
        if (m_modelTypeShortList.contains(modelType)) {
          comp2 = new CompositeObject(1, modelType.getElementName(), modelType.getFullyQualifiedName());
        }
        else {
          comp2 = new CompositeObject(3, modelType.getElementName(), modelType.getFullyQualifiedName());
        }
      }

      return comp1.compareTo(comp2);
    }
  }

}
