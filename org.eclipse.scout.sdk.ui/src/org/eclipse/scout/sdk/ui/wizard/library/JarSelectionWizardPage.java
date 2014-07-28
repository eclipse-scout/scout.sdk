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
package org.eclipse.scout.sdk.ui.wizard.library;

import java.io.File;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.proposal.resources.IoFileLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.resources.ResourcesContentProvider;
import org.eclipse.scout.sdk.ui.fields.table.AutoResizeColumnTable;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableColumn;

/**
 * <h3>{@link JarSelectionWizardPage}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 29.02.2012
 */
public class JarSelectionWizardPage extends AbstractWorkspaceWizardPage {
  private static final String PREF_FILE_DIALOG_PATH = "fileDialogPath";

  public static final String PROP_JAR_FILES = "jarFiles";

  private TableViewer m_jarViewer;
  private Button m_removeButton;
  private Button m_addButton;

  /**
   * @param pageName
   */
  public JarSelectionWizardPage() {
    super(JarSelectionWizardPage.class.getName());
    setTitle(Texts.get("NewLibraryBundle"));
    setJarFilesInternal(new TreeSet<File>(new P_FileComparator()));
  }

  @Override
  protected void createContent(Composite parent) {
    AutoResizeColumnTable table = new AutoResizeColumnTable(parent, SWT.SINGLE | SWT.FULL_SELECTION);
    table.setHeaderVisible(true);
    TableColumn fileNameCol = new TableColumn(table, SWT.LEFT);
    fileNameCol.setText(Texts.get("Name"));
    fileNameCol.setWidth(100);
    TableColumn pathColumn = new TableColumn(table, SWT.LEFT);
    pathColumn.setText(Texts.get("Path"));
    pathColumn.setWidth(200);

    m_jarViewer = new TableViewer(table);
    IoFileLabelProvider labelProvider = new IoFileLabelProvider();
    m_jarViewer.setLabelProvider(labelProvider);
    m_jarViewer.setContentProvider(new P_JarFileContentProvider(labelProvider));
    m_jarViewer.setInput(getJarFiles());
    m_jarViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        m_removeButton.setEnabled(!event.getSelection().isEmpty());
      }
    });

    Composite buttonList = new Composite(parent, SWT.NONE);
    m_addButton = new Button(buttonList, SWT.PUSH | SWT.FLAT);
    m_addButton.setText(Texts.get("Add"));
    m_addButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        openJarSelectionDialog();
      }
    });
    m_removeButton = new Button(buttonList, SWT.PUSH | SWT.FLAT);
    m_removeButton.setEnabled(false);
    m_removeButton.setText(Texts.get("Remove"));
    m_removeButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        ISelection currentSelection = m_jarViewer.getSelection();
        if (!currentSelection.isEmpty()) {
          Set<File> jarFiles = getJarFiles();
          if (jarFiles.remove(((IStructuredSelection) currentSelection).getFirstElement())) {
            setJarFiels(jarFiles);
          }
        }
      }
    });

    // layout
    parent.setLayout(new GridLayout(2, false));
    m_jarViewer.getControl().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    buttonList.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING));
    buttonList.setLayout(new GridLayout(1, true));
    m_addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    m_removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  }

  private void openJarSelectionDialog() {
    IDialogSettings prefs = ScoutSdkUi.getDefault().getDialogSettingsSection(JarSelectionWizardPage.class.getName() + ".FileDialog", true);
    FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
    dialog.setFilterExtensions(new String[]{"*.jar"});
    dialog.setText(Texts.get("JARFileSelection"));
    String filterPath = prefs.get(PREF_FILE_DIALOG_PATH);
    if (StringUtility.hasText(filterPath)) {
      dialog.setFilterPath(filterPath);
    }
    boolean ok = dialog.open() != null;

    // store prefs
    prefs.put(PREF_FILE_DIALOG_PATH, dialog.getFilterPath());
    if (ok) {
      for (String fileName : dialog.getFileNames()) {
        File file = new File(dialog.getFilterPath(), fileName);
        if (file.exists()) {
          Set<File> jarFiles = getJarFiles();
          jarFiles.add(file);
          setJarFiels(jarFiles);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public Set<File> getJarFiles() {
    return (Set<File>) getProperty(PROP_JAR_FILES);
  }

  public void setJarFiels(Set<File> jarFiles) {
    try {
      setStateChanging(true);
      setJarFilesInternal(jarFiles);
      if (isControlCreated()) {
        m_jarViewer.setInput(jarFiles);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setJarFilesInternal(Set<File> jarFiles) {
    setPropertyAlwaysFire(PROP_JAR_FILES, jarFiles);
  }

  // validation
  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusJarFiles());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusJarFiles() throws JavaModelException {
    Set<File> jarFiles = getJarFiles();
    if (jarFiles == null || jarFiles.isEmpty()) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoJarFilesSelected"));
    }
    return Status.OK_STATUS;
  }

  private class P_JarFileContentProvider extends ResourcesContentProvider {
    /**
     * @param labelProvider
     */
    public P_JarFileContentProvider(ILabelProvider labelProvider) {
      super(labelProvider);
    }

    @Override
    public Object[] getElements() {
      Set<File> jarFiles = getJarFiles();
      if (jarFiles != null) {
        return jarFiles.toArray(new File[jarFiles.size()]);
      }
      return new File[0];
    }
  }

  private class P_FileComparator implements Comparator<File> {
    @Override
    public int compare(File o1, File o2) {
      if (o1 == null && o2 == null) {
        return 0;
      }
      else if (o1 == null) {
        return -1;
      }
      else if (o2 == null) {
        return 1;
      }
      else {
        return CompareUtility.compareTo(o1.getName(), o2.getName());
      }
    }
  }

}
