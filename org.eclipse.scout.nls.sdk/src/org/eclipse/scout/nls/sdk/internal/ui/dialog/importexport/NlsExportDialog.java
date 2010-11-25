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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.nls.sdk.internal.ui.fields.FileChooserField;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.scout.nls.sdk.util.concurrent.UiRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class NlsExportDialog extends TitleAreaDialog {

  private Composite m_rootArea;
  private FileChooserField m_selectCsv;
  private NlsExportDialogModel m_model;
  private Button m_selectedOnly;

  public NlsExportDialog(Shell parentShell, NlsExportDialogModel desc) {
    super(parentShell);
    m_model = desc;
    m_model.addPropertyChangeListener(new P_PropertyChangedListener());
  }

  public NlsExportDialogModel getModel() {
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
    m_selectCsv = new FileChooserField(m_rootArea, "Select XLS-File to export the data to..");
    m_selectCsv.setButtonText("Browse...");
    m_selectCsv.setExtendsionFilter(new String[]{"*.xls"});
    m_selectCsv.setLabelText("XLS-File:");
    m_selectCsv.addInputChangedListener(SWT.Modify, new IInputChangedListener<String>() {
      public void inputChanged(String input) {
        m_model.setExportFile(input);
      }
    });
    attachGridData(m_selectCsv);

    m_selectedOnly = new Button(m_rootArea, SWT.CHECK);
    m_selectedOnly.setText("Export only selected Items");
    m_selectedOnly.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_model.setExportSelectedItems(m_selectedOnly.getSelection());
      }
    });

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
    boolean valid = false;
    if (m_model.getExportFile() == null || m_model.getExportFile().equals("")) {
      setMessage("The export file must be set.", IMessageProvider.WARNING);
    }
    else {
      valid = true;
      setMessage(null);
    }
    m_selectedOnly.setEnabled((m_model.getSelectedEntries() != null));
    getButton(IDialogConstants.OK_ID).setEnabled(valid);
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
