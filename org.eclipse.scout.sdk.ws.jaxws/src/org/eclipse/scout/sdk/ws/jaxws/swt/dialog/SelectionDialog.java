/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.dialog;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class SelectionDialog<T> extends TitleAreaDialog {

  private T m_element;
  private TableViewer m_viewer;
  private String m_dialogTitle;
  private String m_dialogMessage;
  private Collection<T> m_elements;

  public SelectionDialog(Shell shell, String dialogTitle, String dialogMessage) {
    super(shell);
    m_dialogTitle = dialogTitle;
    m_dialogMessage = dialogMessage;
    m_elements = new LinkedList<T>();
    setDialogHelpAvailable(false);
    setShellStyle(getShellStyle() | SWT.RESIZE);
  }

  @Override
  protected final void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(m_dialogTitle);
  }

  @Override
  protected Control createContents(Composite parent) {
    Control control = super.createContents(parent);
    setTitle(m_dialogTitle);
    setMessage(m_dialogMessage, IMessageProvider.INFORMATION);
    getOkButton().setEnabled(false);
    return control;
  }

  public void setElements(Collection<T> elements) {
    m_elements = elements;
  }

  public T getElement() {
    return m_element;
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite tableComposite = new Composite(parent, SWT.NONE);
    m_viewer = new TableViewer(new Table(tableComposite, SWT.BORDER | SWT.FULL_SELECTION));
    m_viewer.setUseHashlookup(true);
    m_viewer.getTable().setHeaderVisible(true);
    m_viewer.getTable().setLinesVisible(false);
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
        m_element = (T) selection.getFirstElement();

        getOkButton().setEnabled(true);
      }
    });

    m_viewer.addDoubleClickListener(new IDoubleClickListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
        m_element = (T) selection.getFirstElement();
        getOkButton().setEnabled(true);
        close();
      }
    });

    TableViewerColumn nameColumn = new TableViewerColumn(m_viewer, SWT.LEFT, 0);
    nameColumn.setLabelProvider(new P_LabelProvider());
    nameColumn.getColumn().setResizable(true);
    nameColumn.getColumn().setText(getConfiguredNameColumnText());

    TableViewerColumn descriptionColumn = null;
    if (getConfiguredIsDescriptionColumnVisible()) {
      descriptionColumn = new TableViewerColumn(m_viewer, SWT.LEFT, 1);
      descriptionColumn.setLabelProvider(new P_LabelProvider());
      descriptionColumn.getColumn().setResizable(true);
      descriptionColumn.getColumn().setText(getConfiguredDescriptionColumnText());
    }

    m_viewer.setContentProvider(new P_ContentProvider());
    m_viewer.setInput(m_elements.toArray());

    // layout
    parent.setLayout(new GridLayout());
    tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    TableColumnLayout tableLayout = new TableColumnLayout();
    tableComposite.setLayout(tableLayout);
    if (descriptionColumn == null) {
      tableLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(1, false));
    }
    else {
      tableLayout.setColumnData(nameColumn.getColumn(), new ColumnPixelData(120, true));
      tableLayout.setColumnData(descriptionColumn.getColumn(), new ColumnWeightData(1, true));
    }

    return tableComposite;
  }

  protected void execDecorateElement(T element, ViewerCell cell) {
    cell.setText(element.toString());
  }

  protected boolean getConfiguredIsDescriptionColumnVisible() {
    return false;
  }

  protected String getConfiguredNameColumnText() {
    return Texts.get("Name");
  }

  protected String getConfiguredDescriptionColumnText() {
    return Texts.get("Description");
  }

  private class P_LabelProvider extends CellLabelProvider {

    @SuppressWarnings("unchecked")
    @Override
    public void update(ViewerCell cell) {
      execDecorateElement((T) cell.getElement(), cell);
    }
  }

  public class P_ContentProvider implements IStructuredContentProvider {

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getElements(Object inputElement) {
      return (T[]) inputElement;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
}
