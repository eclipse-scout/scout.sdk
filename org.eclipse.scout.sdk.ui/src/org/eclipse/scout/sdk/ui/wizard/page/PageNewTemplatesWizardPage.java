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
package org.eclipse.scout.sdk.ui.wizard.page;

import java.util.Set;

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
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>PageNewWizardPage1</h3> ...
 */
public class PageNewTemplatesWizardPage extends AbstractWorkspaceWizardPage {
  private final IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);

  private FilteredTable m_filteredTable;
  private IType m_selectedType;
  private final IScoutBundle m_clientBundle;

  public PageNewTemplatesWizardPage(IScoutBundle clientBundle) {
    super(PageNewTemplatesWizardPage.class.getName());
    setTitle(Texts.get("PageTemplates"));
    setDescription(Texts.get("ChooseATemplateForYourPage"));
    m_clientBundle = clientBundle;
  }

  @Override
  protected void createContent(Composite parent) {
    boolean isEnabled = getClientBundle() != null;
    m_filteredTable = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_filteredTable.setEnabled(isEnabled);
    m_filteredTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          setSelectedTypeInternal((IType) selection.getFirstElement());
          pingStateChanging();
        }

      }
    });
    m_filteredTable.getViewer().addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          setSelectedTypeInternal((IType) selection.getFirstElement());
          pingStateChanging();
          IWizardPage page = getNextPage();
          if (page == null) {
            // something must have happened getting the next page
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
    updateUi();

    // layout
    parent.setLayout(new GridLayout(1, true));
    GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    gd.heightHint = 300;
    m_filteredTable.setLayoutData(gd);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (getClientBundle() == null) {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoNewXWithoutScoutBundle", Texts.get("Page"))));
    }

    if (getSelectedType() != null) {
      multiStatus.add(Status.OK_STATUS);
    }
    else {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ATemplateMustBeSelected")));
    }
    PageNewAttributesWizardPage page = (PageNewAttributesWizardPage) getWizard().getPage(PageNewAttributesWizardPage.class.getName());
    if (page != null) {
      page.setSuperType(getSelectedType());
    }
  }

  protected void updateUi() {
    if (getClientBundle() != null) {
      Set<IType> templates = TypeUtility.getAbstractTypesOnClasspath(iPage, getClientBundle().getJavaProject(), TypeFilters.getPrimaryTypeFilter());
      P_TableContentProvider provider = new P_TableContentProvider(templates.toArray(new IType[templates.size()]));
      m_filteredTable.getViewer().setLabelProvider(provider);
      m_filteredTable.getViewer().setContentProvider(provider);
      m_filteredTable.getViewer().setInput(provider);
      m_filteredTable.getViewer().refresh();
    }
    else {
      P_TableContentProvider provider = new P_TableContentProvider(new IType[0]);
      m_filteredTable.getViewer().setLabelProvider(provider);
      m_filteredTable.getViewer().setContentProvider(provider);
      m_filteredTable.getViewer().setInput(provider);
      m_filteredTable.getViewer().refresh();
    }
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public void setSuperType(IType selectedType) {
    IStructuredContentProvider prov = (IStructuredContentProvider) m_filteredTable.getViewer().getContentProvider();
    for (Object row : prov.getElements(null)) {
      if (((IType) row).equals(selectedType)) {
        m_filteredTable.getViewer().setSelection(new StructuredSelection(selectedType));
      }
    }
    pingStateChanging();
  }

  private void setSelectedTypeInternal(IType type) {
    m_selectedType = type;
    boolean showLocationPage = TypeUtility.getSupertypeHierarchy(type).contains(TypeUtility.getType(IRuntimeClasses.IPageWithTable));
    ((PageNewWizard) getWizard()).setLocationWizardPageVisible(showLocationPage);
  }

  public IType getSelectedType() {
    return m_selectedType;
  }

  private final class P_TableContentProvider implements IStructuredContentProvider, ITableLabelProvider {
    IType[] m_templates;

    private P_TableContentProvider(IType[] templates) {
      m_templates = templates;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_templates;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return ScoutSdkUi.getImage(ScoutSdkUi.Class);
      }
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      return ((IType) element).getElementName();
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

}
