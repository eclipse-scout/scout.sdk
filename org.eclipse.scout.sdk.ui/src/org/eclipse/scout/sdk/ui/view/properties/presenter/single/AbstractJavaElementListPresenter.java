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
package org.eclipse.scout.sdk.ui.view.properties.presenter.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.table.AutoResizeColumnTable;
import org.eclipse.scout.sdk.ui.fields.table.JavaElementTableContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public abstract class AbstractJavaElementListPresenter extends AbstractMethodPresenter {

  private TableViewer m_viewer;
  private Table m_table;
  private List<? extends IJavaElement> m_sourceElements;
  private JavaElementTableContentProvider m_tableModel;
  private Button m_removeButton;
  private Button m_addButton;

  public AbstractJavaElementListPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected Control createContent(Composite container) {
    Composite rootArea = getToolkit().createComposite(container);
    Control table = createTable(rootArea);
    Control controlArea = createControlArea(rootArea);

    // layout
    GridLayout layout = new GridLayout(2, false);
    layout.horizontalSpacing = 2;
    layout.marginHeight = 0;
    layout.marginTop = 0;
    layout.marginWidth = 0;
    rootArea.setLayout(layout);
    GridData gdata = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
    gdata.heightHint = m_table.getItemHeight() * 3 + 5;
    gdata.minimumWidth = 100;
    gdata.widthHint = 150;
    table.setLayoutData(gdata);
    controlArea.setLayoutData(new GridData());
    return rootArea;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      if (enabled) {
        m_removeButton.setEnabled(!m_viewer.getSelection().isEmpty());
      }
      else {
        m_removeButton.setEnabled(false);
      }
      m_addButton.setEnabled(enabled);
    }
    super.setEnabled(enabled);
  }

  private Control createTable(Composite container) {
    // create content provider

    m_tableModel = new JavaElementTableContentProvider();
    m_table = new AutoResizeColumnTable(container, SWT.FULL_SELECTION | SWT.BORDER);
    TableColumn simpleNameCol = new TableColumn(m_table, SWT.LEFT);
    simpleNameCol.setData(AutoResizeColumnTable.COLUMN_WEIGHT, Integer.valueOf(3));
    simpleNameCol.setWidth(170);
    simpleNameCol.setText(Texts.get("Member"));
    m_table.setHeaderVisible(false);
    m_viewer = new TableViewer(m_table);
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        m_removeButton.setEnabled(!event.getSelection().isEmpty());
      }
    });
    m_viewer.setContentProvider(m_tableModel);
    m_viewer.setLabelProvider(m_tableModel);
    m_viewer.setInput(this);
    m_viewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        // TODO select the current oultine in the outline view
      }
    });

    return m_viewer.getControl();
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    super.init(method);
    m_sourceElements = readSource();
    if (m_sourceElements == null) {
      m_tableModel.setElements(null);
    }
    else {
      m_tableModel.setElements(m_sourceElements.toArray(new IJavaElement[m_sourceElements.size()]));
    }
    m_viewer.refresh();
    m_removeButton.setEnabled(!m_viewer.getSelection().isEmpty());
    m_addButton.setEnabled(true);
  }

  private Control createControlArea(Composite parent) {
    Composite pane = getToolkit().createComposite(parent);
    m_addButton = getToolkit().createButton(pane, "", SWT.PUSH);
    m_addButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolAdd));
    m_addButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleAddComponent();
      }
    });
    m_addButton.setEnabled(false);
    m_removeButton = getToolkit().createButton(pane, "", SWT.PUSH);
    m_removeButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolRemove));
    m_removeButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleRemoveComponent();
      }
    });
    m_removeButton.setEnabled(false);

    // layout
    GridLayout layout = new GridLayout(1, true);
    layout.horizontalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginTop = 0;
    layout.marginWidth = 0;
    pane.setLayout(layout);
    GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    gridData.heightHint = SdkProperties.TOOL_BUTTON_SIZE;
    gridData.widthHint = SdkProperties.TOOL_BUTTON_SIZE;
    m_addButton.setLayoutData(gridData);
    gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    gridData.heightHint = SdkProperties.TOOL_BUTTON_SIZE;
    gridData.widthHint = SdkProperties.TOOL_BUTTON_SIZE;
    m_removeButton.setLayoutData(gridData);

    return pane;
  }

  private void handleRemoveComponent() {
    IJavaElement toRemove = (IJavaElement) ((StructuredSelection) m_viewer.getSelection()).getFirstElement();
    List<IJavaElement> props = new ArrayList<IJavaElement>();
    for (IJavaElement prop : getSourceProps()) {
      if (!prop.equals(toRemove)) {
        props.add(prop);
      }
    }
    store(props);
  }

  protected abstract void handleAddComponent();

  public abstract List<? extends IJavaElement> readSource() throws CoreException;

  public abstract void store(List<? extends IJavaElement> proposals);

  public List<? extends IJavaElement> getSourceProps() {
    return m_sourceElements;
  }

}
