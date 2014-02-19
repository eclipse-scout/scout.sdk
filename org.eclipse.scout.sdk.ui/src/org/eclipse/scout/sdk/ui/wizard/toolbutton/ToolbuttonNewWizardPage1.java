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
package org.eclipse.scout.sdk.ui.wizard.toolbutton;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>TableColumnNewWizardPage</h3> ...
 */
public class ToolbuttonNewWizardPage1 extends AbstractWorkspaceWizardPage {

  private final IType iToolbutton = TypeUtility.getType(RuntimeClasses.IToolButton);
  private final IType abstractOutlineViewButton = TypeUtility.getType(RuntimeClasses.AbstractOutlineViewButton);

  private IType m_declaringType;
  private FilteredTable m_filteredTable;
  private IWizardPage m_nextPage;
  private IType m_superType;

  public ToolbuttonNewWizardPage1(IType declaringType) {
    super(ToolbuttonNewWizardPage1.class.getName());
    setTitle(Texts.get("ToolButtonTemplates"));
    setDescription(Texts.get("ChooseATemplateForYourToolButton"));
    m_declaringType = declaringType;
  }

  @Override
  protected void createContent(Composite parent) {
    m_filteredTable = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_filteredTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          m_superType = (IType) selection.getFirstElement();
          validateNextPage();
          pingStateChanging();
        }

      }
    });

    P_TableContentProvider provider = new P_TableContentProvider(ScoutTypeUtility.getAbstractTypesOnClasspath(iToolbutton, m_declaringType.getJavaProject()));
    m_filteredTable.getViewer().setLabelProvider(provider);
    m_filteredTable.getViewer().setContentProvider(provider);
    m_filteredTable.getViewer().setInput(provider);

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_filteredTable.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
  }

  protected void validateNextPage() {
    if (TypeUtility.exists(getSuperType())) {
      ITypeHierarchy superTypeHierarchy = null;
      try {
        superTypeHierarchy = getSuperType().newSupertypeHierarchy(null);
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logError("could not build super type hierarchy of '" + getSuperType().getFullyQualifiedName() + "'.", e);
      }
      if (superTypeHierarchy != null && superTypeHierarchy.contains(abstractOutlineViewButton)) {
        m_nextPage = getWizard().getPage(OutlineToolbuttonNewWizardPage.class.getName());
      }
      else {
        m_nextPage = getWizard().getPage(ToolbuttonNewWizardPage2.class.getName());
      }
    }
    else {
      m_nextPage = null;
    }
  }

  @Override
  public IWizardPage getNextPage() {
    return m_nextPage;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (getSuperType() != null) {
      multiStatus.add(Status.OK_STATUS);
    }
    else {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ATemplateMustBeSelected")));
    }
  }

  public void setSuperType(IType selectedType) {
    try {
      m_superType = selectedType;
      if (isControlCreated()) {
        m_filteredTable.getViewer().setSelection(new StructuredSelection(selectedType));

        validateNextPage();
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public IType getSuperType() {
    return m_superType;
  }

  private class P_TableContentProvider implements IStructuredContentProvider, ITableLabelProvider {
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
        return ScoutSdkUi.getImage(ScoutSdkUi.FormField);
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
