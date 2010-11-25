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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.table.AutoResizeColumnTable;
import org.eclipse.scout.sdk.ui.fields.table.FileTableContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

public class FileSelectionDialog extends TitleAreaDialog {
  public static final String PROP_SELECTED_FILES = "selectedFiles";

  private BasicPropertySupport m_propertySupport;
  private IFile[] m_files;
  private CheckboxTableViewer m_viewer;
  private final String m_title;

  private String m_message;

  public FileSelectionDialog(Shell parentShell, String title, String message) {
    this(parentShell, title);
    m_message = message;
  }

  public FileSelectionDialog(Shell parentShell, String title) {
    super(parentShell);
    m_title = title;
    setShellStyle(getShellStyle() | SWT.RESIZE);
    m_propertySupport = new BasicPropertySupport(this);
    m_propertySupport.setProperty(PROP_SELECTED_FILES, new IFile[0]);
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    if (m_title != null) {
      newShell.setText(m_title);
    }
  }

  public IFile[] getFiles() {
    return m_files;
  }

  public void setFiles(IFile[] files) {
    m_files = files;
    if (getContents() != null) {
      getContentProvider().setElements(files);
      m_viewer.refresh();
    }
  }

  public IFile[] getSelectedFiles() {
    return (IFile[]) m_propertySupport.getProperty(PROP_SELECTED_FILES);
  }

  public void setSelectedFiles(IFile[] selectedFiles) {
    m_propertySupport.setProperty(PROP_SELECTED_FILES, selectedFiles);
    if (getContents() != null) {
      m_viewer.setCheckedElements(selectedFiles);
    }
  }

  private void setSelectedFilesFromUI(IFile[] selectedFiles) {
    m_propertySupport.setProperty(PROP_SELECTED_FILES, selectedFiles);
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    if (m_message != null) {
      setMessage(m_message);
    }
    Composite rootPane = new Composite(parent, SWT.NONE);
    AutoResizeColumnTable table = new AutoResizeColumnTable(rootPane, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
    TableColumn simpleNameCol = new TableColumn(table, SWT.LEFT);
    simpleNameCol.setData(AutoResizeColumnTable.COLUMN_WEIGHT, new Integer(3));
    simpleNameCol.setWidth(170);
    simpleNameCol.setText("File");
    TableColumn packageCol = new TableColumn(table, SWT.LEFT);
    packageCol.setData(AutoResizeColumnTable.COLUMN_WEIGHT, new Integer(5));
    packageCol.setText("Path");
    packageCol.setWidth(270);
    m_viewer = new CheckboxTableViewer(table);
    m_viewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        Object[] checkedElements = m_viewer.getCheckedElements();
        setSelectedFilesFromUI(TypeCastUtility.castValue(checkedElements, IFile[].class));
      }
    });
    FileTableContentProvider provider = new FileTableContentProvider();
    provider.setElements(getFiles());
    m_viewer.setContentProvider(provider);
    m_viewer.setLabelProvider(provider);
    m_viewer.setInput(provider);
    m_viewer.setCheckedElements(getSelectedFiles());
    // layout
    if (parent.getLayout() instanceof GridLayout) {
      rootPane.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
    }
    rootPane.setLayout(new GridLayout(1, true));
    m_viewer.getTable().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL));
    return rootPane;
  }

  private FileTableContentProvider getContentProvider() {
    return (FileTableContentProvider) m_viewer.getContentProvider();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

}
