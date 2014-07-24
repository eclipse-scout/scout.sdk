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
package org.eclipse.scout.sdk.ui.dialog;

import java.beans.PropertyChangeListener;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.table.AutoResizeColumnTable;
import org.eclipse.scout.sdk.ui.fields.table.FilteredTable;
import org.eclipse.scout.sdk.ui.fields.table.JavaElementTableContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

public class JavaElementSelectionDialog extends Dialog {
  public static final String PROP_SELECTED_ELEMENTS = "selectedElements";

  private BasicPropertySupport m_propertySupport;
  private FilteredTable m_filteredTable;
  private IJavaElement[] m_javaElements;
  private final String m_title;
  private Point m_initialLocation;
  private boolean m_multiSelect = false;

  public JavaElementSelectionDialog(Shell parentShell, String title) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    m_title = title;
    m_propertySupport = new BasicPropertySupport(this);
    // init
    m_propertySupport.setProperty(PROP_SELECTED_ELEMENTS, new IJavaElement[0]);
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  @Override
  protected Point getInitialLocation(Point initialSize) {
    if (m_initialLocation != null) {
      return m_initialLocation;
    }
    else {
      return super.getInitialLocation(initialSize);
    }
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);
    int style = (isMultiSelect() ? SWT.MULTI : SWT.SINGLE) | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL;
    m_filteredTable = new FilteredTable(rootArea, style);
    m_filteredTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        StructuredSelection selection = (StructuredSelection) event.getSelection();
        IJavaElement[] selectedElements = TypeCastUtility.castValue(selection.toArray(), IJavaElement[].class);
        m_propertySupport.setProperty(PROP_SELECTED_ELEMENTS, selectedElements);
      }
    });

    TableColumn simpleNameCol = new TableColumn(m_filteredTable.getTable(), SWT.LEFT);
    simpleNameCol.setData(AutoResizeColumnTable.COLUMN_WEIGHT, Integer.valueOf(3));
    simpleNameCol.setWidth(170);
    simpleNameCol.setText(Texts.get("Member"));
    TableColumn packageCol = new TableColumn(m_filteredTable.getTable(), SWT.LEFT);
    packageCol.setData(AutoResizeColumnTable.COLUMN_WEIGHT, Integer.valueOf(5));
    packageCol.setText(Texts.get("Package"));
    packageCol.setWidth(270);

    JavaElementTableContentProvider provider = new JavaElementTableContentProvider();
    provider.setElements(getJavaElements());
    m_filteredTable.getViewer().setLabelProvider(provider);
    m_filteredTable.getViewer().setContentProvider(provider);
    m_filteredTable.getViewer().setInput(provider);

    rootArea.setLayout(new FillLayout());
    rootArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    return rootArea;
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  public void setJavaElements(IJavaElement[] javaElements) {
    if (getContents() != null) {
      throw new IllegalStateException("control already created.");
    }
    m_javaElements = javaElements;
  }

  public IJavaElement[] getJavaElements() {
    return m_javaElements;
  }

  public void setMultiSelect(boolean multiSelect) {
    m_multiSelect = multiSelect;
  }

  public boolean isMultiSelect() {
    return m_multiSelect;
  }

  public IJavaElement[] getSelectedElements() {
    return (IJavaElement[]) m_propertySupport.getProperty(PROP_SELECTED_ELEMENTS);
  }

  public void setInitialLocation(Point initialLocation) {
    m_initialLocation = initialLocation;
  }

  public Point getInitialLocation() {
    return m_initialLocation;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(propertyName, listener);
  }
}
