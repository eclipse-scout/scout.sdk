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
import java.util.Set;

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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link FormHandlerNewWizardPage1}</h3>
 */
public class FormHandlerNewWizardPage1 extends AbstractWorkspaceWizardPage {

  private final IType iFormHandler = TypeUtility.getType(IRuntimeClasses.IFormHandler);

  private IType m_declaringType;
  private boolean m_showAllTemplates;

  private FilteredTable m_filteredTable;
  private Button m_showAllTemplatesField;

  private HandlerTemplate m_selectedTemplate;

  public FormHandlerNewWizardPage1(IType declaringType) {
    super(FormHandlerNewWizardPage1.class.getName());
    setTitle(Texts.get("FormHandlerTemplates"));
    setDescription(Texts.get("ChooseATemplateForYourFormHandler"));
    m_declaringType = declaringType;
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
          m_selectedTemplate = (HandlerTemplate) selection.getFirstElement();
          validateNextPage();
          pingStateChanging();
        }

      }
    });
    List<HandlerTemplate> templates = new ArrayList<HandlerTemplate>();
    IType formHandler = RuntimeClasses.getSuperType(IRuntimeClasses.IFormHandler, m_declaringType.getJavaProject());
    templates.add(new HandlerTemplate(Texts.get("FormHandlerNEW"), formHandler, HandlerTemplate.ID_NEW));
    templates.add(new HandlerTemplate(Texts.get("FormHandlerMODIFY"), formHandler, HandlerTemplate.ID_MODIFY));
    templates.add(new HandlerTemplate(Texts.get("FormHandler"), formHandler, HandlerTemplate.ID_CUSTOM));

    Set<IType> abstractFormHandlers = TypeUtility.getAbstractTypesOnClasspath(iFormHandler, m_declaringType.getJavaProject(), null);
    for (IType t : abstractFormHandlers) {
      templates.add(new HandlerTemplate(t.getElementName(), t, HandlerTemplate.ID_DEFAULT));
    }
    P_TableContentProvider provider = new P_TableContentProvider(templates.toArray(new HandlerTemplate[templates.size()]));
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
    FormHandlerNewWizardPage2 nextPage = (FormHandlerNewWizardPage2) getWizard().getNextPage(this);
    if (m_selectedTemplate == null || m_selectedTemplate.getId() == HandlerTemplate.ID_CUSTOM
        || m_selectedTemplate.getId() == HandlerTemplate.ID_CUSTOM) {
      nextPage.setTypeName("");
    }
    else {
      switch (m_selectedTemplate.getId()) {
        case HandlerTemplate.ID_MODIFY:
          nextPage.setTypeName(SdkProperties.TYPE_NAME_MODIFY_HANDLER_PREFIX);
          break;
        case HandlerTemplate.ID_NEW:
          nextPage.setTypeName(SdkProperties.TYPE_NAME_NEW_HANDLER_PREFIX);
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
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("ATemplateMustBeSelected")));
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

  private final class P_TableContentProvider implements IStructuredContentProvider, ITableLabelProvider {
    HandlerTemplate[] m_templates;

    private P_TableContentProvider(HandlerTemplate[] templates) {
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
      return ((HandlerTemplate) element).getTemplateName();
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

  private final class HandlerTemplate {
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

  private final class P_ModeFilter extends ViewerFilter {
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
