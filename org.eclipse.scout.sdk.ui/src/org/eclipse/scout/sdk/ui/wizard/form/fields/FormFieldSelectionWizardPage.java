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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.ui.extensions.IFormFieldExtension;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.internal.extensions.FormFieldExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>FormFieldSelectionWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 02.03.2010
 */
public class FormFieldSelectionWizardPage extends AbstractWorkspaceWizardPage {

  private IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);

  private final IType m_declaringType;
  private AbstractScoutWizardPage m_nextPage;

  private HashSet<IFormFieldExtension> m_byExtensionEntries = new HashSet<IFormFieldExtension>();
  private HashSet<IType> m_byClassEntries = new HashSet<IType>();
  // ui fields
  private FilteredTable m_table;
  private Button m_allFieldsButton;
  private P_TableFilter m_tableFilter;

  /**
   * @param pageName
   */
  public FormFieldSelectionWizardPage(IType declaringType) {
    super(FormFieldSelectionWizardPage.class.getName());
    m_declaringType = declaringType;
    setTitle(Texts.get("FormField"));
    setDefaultMessage(Texts.get("FormFieldDesc"));

  }

  @Override
  protected void createContent(Composite parent) {
    // entries
    for (IFormFieldExtension ext : FormFieldExtensionPoint.getAllFormFieldExtensions()) {
      if (ext.isInShortList() && !StringUtility.isNullOrEmpty(ext.getName())) {
        m_byExtensionEntries.add(ext);
      }
    }
    IPrimaryTypeTypeHierarchy formFieldHierarchy = ScoutSdk.getPrimaryTypeHierarchy(ScoutSdk.getType(RuntimeClasses.IFormField));
    IType[] abstractFormFields = formFieldHierarchy.getAllSubtypes(iFormField, TypeFilters.getAbstractOnClasspath(m_declaringType.getJavaProject()), TypeComparators.getOrderAnnotationComparator());
    for (IType formField : abstractFormFields) {
      m_byClassEntries.add(formField);
    }
    m_tableFilter = new P_TableFilter();
    // ui
    m_table = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_table.getViewer().addFilter(m_tableFilter);
    m_table.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        Object selectedItem = null;
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          selectedItem = selection.getFirstElement();
        }
        handleSelection(selectedItem);
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
    ArrayList<Object> elements = new ArrayList<Object>();
    elements.addAll(m_byClassEntries);
    elements.addAll(m_byExtensionEntries);
    P_ContentProvider provider = new P_ContentProvider(elements.toArray());
    m_table.getViewer().setLabelProvider(provider);
    m_table.getViewer().setContentProvider(provider);
    m_table.getViewer().setInput(provider);
    m_allFieldsButton = new Button(parent, SWT.CHECK);
    m_allFieldsButton.setText(Texts.get("ShowAllFields"));
    m_allFieldsButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showAllFields(((Button) e.widget).getSelection());
      }
    });
    showAllFields(false);

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
  }

  private void handleSelection(Object selectedItem) {

    AbstractFormFieldWizard wizard = null;
    if (selectedItem instanceof IType) {
      IType selectedType = (IType) selectedItem;

      wizard = (AbstractFormFieldWizard) FormFieldExtensionPoint.createNewWizard(selectedType);
      wizard.initWizard(m_declaringType);
      wizard.setSuperType(selectedType);
    }
    else if (selectedItem instanceof IFormFieldExtension) {
      IFormFieldExtension ext = (IFormFieldExtension) selectedItem;
      wizard = (AbstractFormFieldWizard) ext.createNewWizard();
      wizard.initWizard(m_declaringType);
    }
    if (wizard != null) {
      m_nextPage = (AbstractScoutWizardPage) wizard.getPages()[0];
    }
    else {
      m_nextPage = null;
    }
    revalidate();
  }

  /**
   * @param selection
   */
  protected void showAllFields(boolean selection) {
    m_tableFilter.setByClass(selection);
    m_table.refresh(true);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusFieldList());
  }

  private IStatus getStatusFieldList() {
    if (m_nextPage == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("NoFieldSelected"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public IWizardPage getNextPage() {
    return m_nextPage;
  }

  private class P_TableFilter extends ViewerFilter {
    private boolean m_byClass = false;

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (m_byClass) {
        return element instanceof IType;
      }
      else {
        return element instanceof IFormFieldExtension;
      }
    }

    /**
     * @param byClass
     *          the byClass to set
     */
    public void setByClass(boolean byClass) {
      m_byClass = byClass;
    }

  } // end class P_TableFilter

  private class P_ContentProvider implements IStructuredContentProvider, ITableLabelProvider {

    private Object[] m_elements;

    public P_ContentProvider(Object[] elements) {
      m_elements = elements;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_elements;
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
      if (columnIndex == 0) {
        String label = "";
        if (element instanceof IType) {
          IType t = (IType) element;
          label = t.getElementName();
          StructuredSelection selection = (StructuredSelection) m_table.getViewer().getSelection();
          if (selection.toList().contains(element)) {
            label = label + "  (" + t.getPackageFragment().getElementName() + ")";
          }
        }
        else if (element instanceof IFormFieldExtension) {
          label = ((IFormFieldExtension) element).getName();
        }
        return label;
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

}
