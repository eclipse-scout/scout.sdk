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
package org.eclipse.scout.nls.sdk.internal.ui.dialog;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

@SuppressWarnings("restriction")
public class ProjectChooserDialog extends TitleAreaDialog {

  private String m_title;
  private HashMap<String, IProject> m_selection = new HashMap<String, IProject>();
  private Collection<IProject> m_workspaceProjects = new LinkedList<IProject>();
  private CheckboxTableViewer m_viewer;

  public ProjectChooserDialog(Shell parentShell, String title) {
    super(parentShell);
    m_title = title;
    ResourcesPlugin.getWorkspace().getRoot();
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);

    m_viewer = CheckboxTableViewer.newCheckList(rootArea, SWT.BORDER);
    m_viewer.addCheckStateListener(new ICheckStateListener() {
      @Override
      public void checkStateChanged(CheckStateChangedEvent event) {
        handleSelectionChanged((IProject) event.getElement(), event.getChecked());
      }
    });
    for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      try {
        if (project.isOpen() && project.hasNature(PDE.PLUGIN_NATURE)) {
          m_workspaceProjects.add(project);
        }
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        NlsCore.logWarning(e);
      }
    }
    P_ProjectModel model = new P_ProjectModel(m_workspaceProjects);
    m_viewer.setContentProvider(model);
    m_viewer.setLabelProvider(model);
    m_viewer.setInput(model);

    Control controlArea = createControlArea(rootArea);

    // layout
    GridData data = new GridData();
    data.horizontalAlignment = SWT.FILL;
    data.grabExcessHorizontalSpace = true;
    data.verticalAlignment = SWT.FILL;
    data.grabExcessVerticalSpace = true;
    rootArea.setLayoutData(data);
    rootArea.setLayout(new GridLayout(2, false));
    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    m_viewer.getTable().setLayoutData(data);
    controlArea.setLayoutData(new GridData());
    return rootArea;
  }

  private Control createControlArea(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);
    Button selectAll = new Button(rootArea, SWT.PUSH | SWT.FLAT);
    selectAll.setText("Select all");
    selectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_viewer.setAllChecked(true);
        for (IProject project : m_workspaceProjects) {
          m_selection.put(project.getName(), project);
        }
        revalidate();
      }
    });
    Button deselectAll = new Button(rootArea, SWT.PUSH | SWT.FLAT);
    deselectAll.setText("Deselect all");
    deselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_viewer.setAllChecked(false);
        m_selection.clear();
        revalidate();
      }
    });
    // layout
    rootArea.setLayout(new GridLayout(1, true));
    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    selectAll.setLayoutData(data);
    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    deselectAll.setLayoutData(data);
    return rootArea;

  }

  public void setSelection(Collection<IProject> projects) {
    for (IProject project : projects) {
      if (m_workspaceProjects.contains(project)) {
        m_viewer.setChecked(project, true);
        m_selection.put(project.getName(), project);
      }
    }
    revalidate();
  }

  public Collection<IProject> getSelection() {
    return m_selection.values();
  }

  private void handleSelectionChanged(IProject project, boolean checked) {
    if (checked) {
      m_selection.put(project.getName(), project);
    }
    else {
      m_selection.remove(project.getName());
    }
    revalidate();
  }

  private void revalidate() {
    if (m_selection.isEmpty()) {
      getButton(OK).setEnabled(false);
    }
    else {
      getButton(OK).setEnabled(true);
    }
  }

  private class P_ProjectModel implements IStructuredContentProvider, ITableLabelProvider {
    private Collection<IProject> m_pluginProjects;

    private P_ProjectModel(Collection<IProject> workspaceProjects) {
      m_pluginProjects = workspaceProjects;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_pluginProjects.toArray();
    }

    @Override
    public Image getColumnImage(Object element, int column) {
      return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
    }

    @Override
    public String getColumnText(Object element, int column) {
      return ((IProject) element).getName();
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

  } // P_ProjectTableModel

}
