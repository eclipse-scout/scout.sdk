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
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;

public class AdditionalResourcesWizardPage extends AbstractWorkspaceWizardPage {

  private Set<File> m_files;

  private TableViewer m_tableViewer;
  private Button m_addButton;
  private Button m_removeButton;

  public AdditionalResourcesWizardPage() {
    super(AdditionalResourcesWizardPage.class.getName());
    setTitle(Texts.get("AdditionalResources"));
    setDescription(Texts.get("AddAdditionalResources1"));
  }

  @Override
  protected void createContent(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);

    Composite tableComposite = new Composite(container, SWT.NONE);
    m_tableViewer = new TableViewer(new Table(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI));
    m_tableViewer.setUseHashlookup(true);
    m_tableViewer.getTable().setHeaderVisible(true);
    m_tableViewer.getTable().setLinesVisible(false);
    m_tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        m_removeButton.setEnabled(!event.getSelection().isEmpty());
      }
    });

    m_tableViewer.getTable().addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.DEL) {
          if (m_removeButton.isEnabled()) {
            IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
            Iterator iterator = selection.iterator();

            while (iterator.hasNext()) {
              m_files.remove(iterator.next());
            }
            m_tableViewer.refresh();
          }
        }
      }
    });

    TableViewerColumn nameColumn = new TableViewerColumn(m_tableViewer, SWT.LEFT, 0);
    nameColumn.setLabelProvider(new P_LabelProvider());
    nameColumn.getColumn().setResizable(false);
    nameColumn.getColumn().setText(Texts.get("Resource"));

    m_tableViewer.setContentProvider(ArrayContentProvider.getInstance());
    m_tableViewer.setInput(m_files);

    Composite buttonComposite = new Composite(container, SWT.NONE);

    m_addButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
    m_addButton.setText(Texts.get("Add"));
    m_addButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        openFileBrowser();
      }
    });

    m_removeButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
    m_removeButton.setText(Texts.get("Remove"));
    m_removeButton.setEnabled(false);
    m_removeButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
        Iterator iterator = selection.iterator();

        while (iterator.hasNext()) {
          m_files.remove(iterator.next());
        }
        m_tableViewer.refresh();
      }
    });

    // layout
    container.setLayout(new GridLayout(2, false));
    // table composite
    GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
    tableComposite.setLayoutData(gd);

    // button composite
    gd = new GridData(GridData.FILL_VERTICAL);
    gd.horizontalAlignment = SWT.TOP;
    buttonComposite.setLayoutData(gd);

    // layout within button composite
    GridLayout layout = new GridLayout(1, true);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    buttonComposite.setLayout(layout);
    m_addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    m_removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // layout within table composite (autoresize of columns)
    TableColumnLayout tableLayout = new TableColumnLayout();
    tableComposite.setLayout(tableLayout);
    tableLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(100));
  }

  private void openFileBrowser() {
    FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
    String[] filterNames = new String[]{"XSD Schema (*.xsd)", "WSDL file (*.wsdl)", "All Files (*.*)"};
    String[] filterExtensions = new String[]{"*.xsd", "*.wsdl", "*.*"};
    dialog.setFilterNames(filterNames);
    dialog.setFilterExtensions(filterExtensions);
    if (dialog.open() != null) {
      String[] files = dialog.getFileNames();

      // prefix with base path since files only consists of filename
      IPath basePath = new Path(dialog.getFilterPath());
      for (String filename : files) {
        File file = basePath.append(filename).toFile();
        m_files.add(file);
      }
    }

    m_tableViewer.refresh();
  }

  public File[] getFiles() {
    return m_files.toArray(new File[m_files.size()]);
  }

  public void setFiles(File[] files) {
    m_files = new HashSet<File>(Arrays.asList(files));
  }

  private class P_LabelProvider extends CellLabelProvider {

    @Override
    public void update(ViewerCell cell) {
      File file = (File) cell.getElement();
      cell.setText(file.getName());

      String fileExtension = IOUtility.getFileExtension(file.getName());
      if (StringUtility.equalsIgnoreCase(fileExtension, "wsdl")) {
        cell.setImage(JaxWsSdk.getImage(JaxWsIcons.WsdlFile));
      }
      else {
        cell.setImage(JaxWsSdk.getImage(JaxWsIcons.XsdSchema));
      }
    }
  }
}
