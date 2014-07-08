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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
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
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class SourceFolderSelectionDialog extends TitleAreaDialog {

  private final IScoutBundle m_bundle;
  private IPath m_sourceFolderPath;
  private TableViewer m_viewer;

  public SourceFolderSelectionDialog(Shell shell, IScoutBundle bundle) {
    super(shell);
    m_bundle = bundle;
    setDialogHelpAvailable(false);
    setShellStyle(getShellStyle() | SWT.RESIZE);
  }

  @Override
  protected final void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(Texts.get("SourceFolder"));
  }

  @Override
  protected Control createContents(Composite parent) {
    Control control = super.createContents(parent);
    setTitle(Texts.get("SourceFolder"));
    setMessage(Texts.get("SelectSourceFolder"), IMessageProvider.INFORMATION);
    getOkButton().setEnabled(false);
    return control;
  }

  public IPath getSourceFolderPath() {
    return m_sourceFolderPath;
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    m_viewer = new TableViewer(new Table(parent, SWT.BORDER | SWT.FULL_SELECTION));
    m_viewer.setUseHashlookup(true);
    m_viewer.getTable().setHeaderVisible(false);
    m_viewer.getTable().setLinesVisible(false);
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
        IClasspathEntry classpathEntry = (IClasspathEntry) selection.getFirstElement();
        m_sourceFolderPath = classpathEntry.getPath();

        getOkButton().setEnabled(true);
      }
    });

    m_viewer.addDoubleClickListener(new IDoubleClickListener() {

      @Override
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
        IClasspathEntry classpathEntry = (IClasspathEntry) selection.getFirstElement();
        m_sourceFolderPath = classpathEntry.getPath();

        getOkButton().setEnabled(true);
        close();
      }
    });

    TableViewerColumn nameColumn = new TableViewerColumn(m_viewer, SWT.LEFT);
    nameColumn.setLabelProvider(new P_LabelProvider());
    nameColumn.getColumn().setResizable(true);
    nameColumn.getColumn().setWidth(500);

    m_viewer.setContentProvider(new P_ContentProvider());
    m_viewer.setInput("");

    // layout
    parent.setLayout(new GridLayout());
    m_viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    return m_viewer.getTable();
  }

  private class P_LabelProvider extends CellLabelProvider {

    @Override
    public void update(ViewerCell cell) {
      IClasspathEntry sourceFolderEntry = (IClasspathEntry) cell.getElement();
      cell.setText(sourceFolderEntry.getPath().toString());

      cell.setImage(JaxWsSdk.getImage(JaxWsIcons.SourceFolder));
    }
  }

  public class P_ContentProvider implements IStructuredContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      List<IClasspathEntry> sourceFolderEntries = new LinkedList<IClasspathEntry>();
      try {
        for (IClasspathEntry classpathEntry : m_bundle.getJavaProject().getRawClasspath()) {
          if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
            sourceFolderEntries.add(classpathEntry);
          }
        }
      }
      catch (JavaModelException e) {
        JaxWsSdk.logError("Error occured while fetching source folders.", e);
      }

      return sourceFolderEntries.toArray(new IClasspathEntry[sourceFolderEntries.size()]);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
}
