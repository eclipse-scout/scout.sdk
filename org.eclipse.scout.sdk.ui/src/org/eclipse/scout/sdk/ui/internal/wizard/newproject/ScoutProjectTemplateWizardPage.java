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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.project.template.ProjectTemplateExtension;
import org.eclipse.scout.sdk.ui.internal.extensions.project.template.ProjectTemplateExtensionPoint;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizardPage;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
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
public class ScoutProjectTemplateWizardPage extends AbstractProjectNewWizardPage {

  private FilteredTable m_table;
//  private IScoutProjectTemplateOperation m_selectedTemplate;
  private Label m_descriptionLabel;
  private P_ContentProvider m_provider;
  private ProjectTemplateExtension m_selectedTemplate;
  private ProjectTemplateExtension[] m_extensions;

  /**
   * @param pageName
   */
  public ScoutProjectTemplateWizardPage() {
    super(ScoutProjectTemplateWizardPage.class.getName());
    setTitle(Texts.get("ScoutApplicationTemplates"));
    m_extensions = ProjectTemplateExtensionPoint.getExtensions();
  }

  @Override
  protected void createContent(Composite parent) {
    m_table = new FilteredTable(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    m_table.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        ProjectTemplateExtension selectedItem = null;
        if (!event.getSelection().isEmpty()) {
          StructuredSelection selection = (StructuredSelection) event.getSelection();
          selectedItem = (ProjectTemplateExtension) selection.getFirstElement();
        }
        handleSelection(selectedItem);
      }
    });

    m_provider = new P_ContentProvider();
    m_table.getViewer().setLabelProvider(m_provider);
    m_table.getViewer().setContentProvider(m_provider);
    m_table.getViewer().setInput(m_provider);

    m_descriptionLabel = new Label(parent, SWT.SHADOW_ETCHED_IN | SWT.WRAP);

    reloadTemplates();

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_table.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    m_descriptionLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public void putProperties(PropertyMap properties) {
    if (m_selectedTemplate != null) {
      // set which template has been selected
      properties.setProperty(IScoutProjectNewOperation.PROP_SELECTED_TEMPLATE_NAME, m_selectedTemplate.getTemplate().getId());
    }
  }

  @Override
  public void performHelp() {
    //TODO: remove external link and use eclipse help instead
    ResourceUtility.showUrlInBrowser("http://wiki.eclipse.org/Scout/HowTo/3.8/Create_a_new_project#Step_2");
  }

  @Override
  public ScoutProjectNewWizard getWizard() {
    return (ScoutProjectNewWizard) super.getWizard();
  }

  @Override
  public void setWizard(IWizard newWizard) {
    super.setWizard(newWizard);
    getWizard().getProjectWizardPage().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(IScoutProjectWizardPage.PROP_SELECTED_BUNDLES)) {
          reloadTemplates();
        }
      }
    });
  }

  private void reloadTemplates() {
    if (m_table != null && !m_table.isDisposed()) {
      P_ContentProvider contentProvider = (P_ContentProvider) m_table.getViewer().getContentProvider();
      ProjectTemplateExtension extToSelect = contentProvider.revalidate();
      m_table.refresh(false);
      m_selectedTemplate = null;
      if (extToSelect != null) {
        m_table.getViewer().setSelection(new StructuredSelection(extToSelect));
      }
      else {
        m_table.getViewer().setSelection(StructuredSelection.EMPTY);
      }
    }
  }

  private void handleSelection(ProjectTemplateExtension selectedItem) {
    m_selectedTemplate = selectedItem;
    if (isControlCreated()) {
      String description = "";
      if (m_selectedTemplate != null) {
        description = m_selectedTemplate.getTemplate().getDescription();
      }
      m_descriptionLabel.setText(description);
    }
  }

  private class P_ContentProvider implements IStructuredContentProvider, ITableLabelProvider {

    private ArrayList<ProjectTemplateExtension> m_activeExtensions;

    private P_ContentProvider() {
      m_activeExtensions = new ArrayList<ProjectTemplateExtension>();
    }

    public ProjectTemplateExtension revalidate() {
      m_activeExtensions.clear();
      ProjectTemplateExtension firstExt = null;
      for (ProjectTemplateExtension ext : m_extensions) {
        if (ext.getTemplate().isApplicable(getWizard())) {
          m_activeExtensions.add(ext);
          if (firstExt == null) {
            firstExt = ext;
          }
        }
      }
      return firstExt;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_activeExtensions.toArray(new ProjectTemplateExtension[m_activeExtensions.size()]);
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      ProjectTemplateExtension extension = (ProjectTemplateExtension) element;
      return extension.getTemplate().getText();
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      ProjectTemplateExtension extension = (ProjectTemplateExtension) element;
      Image img = ScoutSdkUi.getImage(extension.getIconPath());
      return img;
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
  } // end class P_ContentProvider
}
