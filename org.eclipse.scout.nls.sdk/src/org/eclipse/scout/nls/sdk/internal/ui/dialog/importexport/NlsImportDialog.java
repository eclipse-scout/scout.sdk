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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.importexport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.nls.sdk.internal.ui.fields.FileChooserField;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.util.concurrent.UiRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class NlsImportDialog extends TitleAreaDialog {

  private NlsImportDialogModel m_model;
  private Composite m_rootArea;

  public NlsImportDialog(Shell parentShell, NlsImportDialogModel desc) {
    super(parentShell);
    m_model = desc;
    m_model.addPropertyChangeListener(new P_PropertyChangedListener());
  }

  public NlsImportDialogModel getModel() {
    return m_model;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Import NLS Entries");
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    revalidate();
    return contents;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    m_rootArea = new Composite(parent, SWT.NONE);
    FileChooserField selectCsv = new FileChooserField(m_rootArea, "Select File to import");
    selectCsv.setButtonText("Browse...");
    selectCsv.setExtendsionFilter(new String[]{"*.csv; *.properties; *.xls"});
    selectCsv.setLabelText("File:");
    selectCsv.addInputChangedListener(SWT.Modify, new IInputChangedListener<String>() {
      public void inputChanged(String input) {
        m_model.setImportFile(input);
      }
    });
    attachGridData(selectCsv);
    // final Button createKeys = new Button(m_rootArea, SWT.CHECK);
    // createKeys.setText("create new keys");
    // createKeys.addSelectionListener(new SelectionAdapter(){
    // public void widgetSelected(SelectionEvent e) {
    // m_model.setCreateNewKeys(createKeys.getSelection());
    // }
    // });
    // attachGridData(createKeys);

    m_rootArea.setLayout(new GridLayout(1, true));
    return m_rootArea;
  }

  private void attachGridData(Control c) {
    GridData d = new GridData();
    d.grabExcessHorizontalSpace = true;
    d.horizontalAlignment = SWT.FILL;
    c.setLayoutData(d);
  }

  private void revalidate() {
    if (m_model.getImportFile() == null || m_model.getImportFile().equals("")) {
      setMessage("The import file must be set.", IMessageProvider.WARNING);
      return;
    }
    File f = new File(m_model.getImportFile());
    if (f.exists()) {
      setMessage(null);
      return;
    }
    else {
      setMessage("The file to import does not exist.", IMessageProvider.WARNING);
      return;
    }
  }

  private class P_PropertyChangedListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      if (getContents() != null && !getContents().isDisposed()) {
        getContents().getDisplay().asyncExec(new UiRunnable(new Object[]{evt}) {
          public void run() {
            syncPropertyChange((PropertyChangeEvent) p_args[0]);
          }
        });
      }
    }

    private void syncPropertyChange(PropertyChangeEvent evt) {
      revalidate();
    }
  }

}
