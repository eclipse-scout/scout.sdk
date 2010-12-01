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
package org.eclipse.scout.sdk.ui.wizard.form.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link FormHandlerNewWizardPage1}</h3> ...
 */
public class FormHandlerNewWizardPage1 extends AbstractWorkspaceWizardPage {
  final IType iFormHandler = ScoutSdk.getType(RuntimeClasses.IFormHandler);
  private IType m_declaringType;
  private boolean m_showAllTemplates;

  private FilteredTable m_filteredTable;
  private Button m_showAllTemplatesField;

  private HandlerTemplate m_selectedTemplate;
  private IWizardPage m_nextPage;

  public FormHandlerNewWizardPage1(IType declaringType) {
    super("Form Handler Templates");
    setTitle("Form Handler Templates");
    setDefaultMessage("Choose a template for your form handler.");
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
          m_selectedTemplate = (HandlerTemplate) selection.getFirstElement();
          validateNextPage();
          pingStateChanging();
        }

      }
    });
    List<HandlerTemplate> templates = new ArrayList<HandlerTemplate>();
    templates.add(new HandlerTemplate("Form Handler NEW", ScoutSdk.getType(RuntimeClasses.AbstractFormHandler), HandlerTemplate.ID_NEW));
    templates.add(new HandlerTemplate("Form Handler MODIFY", ScoutSdk.getType(RuntimeClasses.AbstractFormHandler), HandlerTemplate.ID_MODIFY));
    templates.add(new HandlerTemplate("Form Handler", ScoutSdk.getType(RuntimeClasses.AbstractFormHandler), HandlerTemplate.ID_CUSTOM));

    IType[] abstractFormHandlers = ScoutSdk.getPrimaryTypeHierarchy(iFormHandler).getAllSubtypes(iFormHandler, TypeFilters.getAbstractOnClasspath(m_declaringType.getJavaProject()), TypeComparators.getTypeNameComparator());
    for (IType t : abstractFormHandlers) {
      templates.add(new HandlerTemplate(t.getElementName(), t, HandlerTemplate.ID_DEFAULT));
    }
    P_TableContentProvider provider = new P_TableContentProvider(templates.toArray(new HandlerTemplate[templates.size()]));
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
    FormHandlerNewWizardPage2 nextPage = (FormHandlerNewWizardPage2) getWizard().getNextPage(this);
    if (m_selectedTemplate == null || m_selectedTemplate.getId() == HandlerTemplate.ID_CUSTOM ||
        m_selectedTemplate.getId() == HandlerTemplate.ID_CUSTOM) {
      nextPage.setTypeName("");
    }
    else {
      switch (m_selectedTemplate.getId()) {
        case HandlerTemplate.ID_MODIFY:
          nextPage.setTypeName("Modify");
          break;
        case HandlerTemplate.ID_NEW:
          nextPage.setTypeName("New");
          break;
      }
    }
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
      if (((HandlerTemplate) row).getType().equals(selectedType)) {
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
    HandlerTemplate[] m_templates;

    private P_TableContentProvider(HandlerTemplate[] templates) {
      m_templates = templates;
    }

    public Object[] getElements(Object inputElement) {
      return m_templates;
    }

    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return ScoutSdkUi.getImage(ScoutSdkUi.FormField);
      }
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      return ((HandlerTemplate) element).getTemplateName();
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

  private class HandlerTemplate {
    public static final int ID_NEW = 1;
    public static final int ID_MODIFY = 2;
    public static final int ID_CUSTOM = 3;
    public static final int ID_DEFAULT = 4;

    private final IType m_type;
    private final String m_templateName;
    private final int m_id;

    public HandlerTemplate(String templateName, IType type, int id) {
      m_templateName = templateName;
      m_type = type;
      m_id = id;
    }

    public IType getType() {
      return m_type;
    }

    public String getTemplateName() {
      return m_templateName;
    }

    public int getId() {
      return m_id;
    }

  } // end class P_BCTypeTemplate

  private class P_ModeFilter extends ViewerFilter {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (m_showAllTemplates) {
        return ((HandlerTemplate) element).getId() == HandlerTemplate.ID_DEFAULT;
      }
      else {
        return ((HandlerTemplate) element).getId() != HandlerTemplate.ID_DEFAULT;
      }
    }
  }

}
