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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class TypeSelectionDialogOld extends TitleAreaDialog {

  protected static final String IStructuredSelection = null;
  private IType m_type;
  private TableViewer m_viewer;
  private String m_dialogTitle;
  private String m_dialogMessage;
  private IType[] m_types;

  public TypeSelectionDialogOld(Shell shell, String dialogTitle, String dialogMessage) {
    super(shell);
    m_dialogTitle = dialogTitle;
    m_dialogMessage = dialogMessage;
    m_types = new IType[0];
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

  public IType getType() {
    return m_type;
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite tableComposite = new Composite(parent, SWT.NONE);
    m_viewer = new TableViewer(new Table(tableComposite, SWT.BORDER | SWT.FULL_SELECTION));
    m_viewer.setUseHashlookup(true);
    m_viewer.getTable().setHeaderVisible(false);
    m_viewer.getTable().setLinesVisible(false);
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
        m_type = (IType) selection.getFirstElement();

        getOkButton().setEnabled(true);
      }
    });

    m_viewer.addDoubleClickListener(new IDoubleClickListener() {

      @Override
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
        m_type = (IType) selection.getFirstElement();
        getOkButton().setEnabled(true);
        close();
      }
    });

    TableViewerColumn nameColumn = new TableViewerColumn(m_viewer, SWT.LEFT, 0);
    nameColumn.setLabelProvider(new P_LabelProvider());
    nameColumn.getColumn().setResizable(true);
    nameColumn.getColumn().setWidth(200);

    TableViewerColumn pathColumn = new TableViewerColumn(m_viewer, SWT.LEFT, 1);
    pathColumn.setLabelProvider(new P_LabelProvider());
    pathColumn.getColumn().setResizable(true);
    pathColumn.getColumn().setWidth(400);

    m_viewer.setContentProvider(new P_ContentProvider());
    m_viewer.setInput(m_types);

    // layout
    parent.setLayout(new GridLayout());
    tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    TableColumnLayout tableLayout = new TableColumnLayout();
    tableComposite.setLayout(tableLayout);
    tableLayout.setColumnData(nameColumn.getColumn(), new ColumnPixelData(300));
    tableLayout.setColumnData(pathColumn.getColumn(), new ColumnWeightData(100));

    return tableComposite;
  }

  private class P_LabelProvider extends CellLabelProvider {

    @Override
    public void update(ViewerCell cell) {
      IType type = (IType) cell.getElement();
      if (cell.getColumnIndex() == 0) {
        cell.setText(type.getElementName());
        try {
          ImageDescriptor desc;
          if (Flags.isAbstract(type.getFlags())) {
            desc = new JavaElementImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), JavaElementImageDescriptor.ABSTRACT, new Point(16, 16));
          }
          else if (type.isInterface()) {
            desc = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface);
          }
          else {
            desc = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class);
          }
          cell.setImage(desc.createImage());
        }
        catch (JavaModelException e) {
          JaxWsSdk.logError(e);
        }
      }
      else {
        cell.setText(type.getFullyQualifiedName());
      }
    }
  }

  public class P_ContentProvider implements IStructuredContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      return (IType[]) inputElement;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }

  public void setTypes(IType[] types) {
    m_types = types;
  }

}
