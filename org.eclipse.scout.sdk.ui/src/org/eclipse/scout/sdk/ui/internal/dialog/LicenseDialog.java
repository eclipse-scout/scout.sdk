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
package org.eclipse.scout.sdk.ui.internal.dialog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.compatibility.License;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <h3>{@link LicenseDialog}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 16.02.2012
 */
public class LicenseDialog extends TitleAreaDialog {

  private final Map<String, License[]> m_iuToLicenses;

  private boolean m_complete;

  private TreeViewer m_iuViewer;
  private Text m_licenseTextBox;
  private Button m_acceptButton;
  private Button m_declineButton;
  private SashForm m_sashForm;

  public LicenseDialog(Shell parentShell, Map<String, License[]> iuToLicenses) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
    if (iuToLicenses == null) {
      m_iuToLicenses = Collections.emptyMap();
    }
    else {
      m_iuToLicenses = new LinkedHashMap<>(iuToLicenses.size());
      for (Entry<String, License[]> entry : iuToLicenses.entrySet()) {
        if (StringUtility.hasText(entry.getKey()) && entry.getValue() != null && entry.getValue().length > 0) {
          m_iuToLicenses.put(entry.getKey(), entry.getValue());
        }
      }
    }
    m_complete = false;
  }

  @Override
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    setMessage(Texts.get("LicDialogMsg"));
    setTitle(Texts.get("LicDialogTitle"));
    m_iuViewer.setSelection(m_iuViewer.getSelection(), true);
    return c;
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    Control buttonbar = super.createButtonBar(parent);
    getButton(IDialogConstants.OK_ID).setEnabled(isComplete());
    return buttonbar;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    m_sashForm = new SashForm(parent, SWT.HORIZONTAL);
    m_sashForm.setLayout(new GridLayout());
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    m_sashForm.setLayoutData(gd);

    createLicenseListSection(m_sashForm);
    createLicenseContentSection(m_sashForm);
    return m_sashForm;
  }

  private void createLicenseContentSection(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    Label label = new Label(composite, SWT.NONE);
    label.setText(Texts.get("LicDialogContentLabel"));
    m_licenseTextBox = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
    m_licenseTextBox.setBackground(m_licenseTextBox.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    createLicenseAcceptSection(composite);

    // layout
    GridLayout layout = new GridLayout();
    layout.marginWidth = 2;
    layout.marginHeight = 8;
    composite.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_BOTH);
    composite.setLayoutData(gd);

    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.widthHint = 450;
    gd.heightHint = 400;
    m_licenseTextBox.setLayoutData(gd);
  }

  private void createLicenseListSection(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    Label label = new Label(composite, SWT.NONE);
    label.setText(Texts.get("LicDialogListLabel"));
    m_iuViewer = new TreeViewer(composite, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    m_iuViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
    m_iuViewer.setContentProvider(new P_ContentProvider());
    m_iuViewer.setLabelProvider(new P_LabelProvider());
    m_iuViewer.setComparator(new ViewerComparator());
    m_iuViewer.setInput(m_iuToLicenses);
    if (m_iuToLicenses != null && m_iuToLicenses.size() > 0) {
      for (License[] licenses : m_iuToLicenses.values()) {
        if (licenses != null && licenses.length > 0) {
          m_iuViewer.setSelection(new StructuredSelection(licenses[0]), false);
          break;
        }
      }
    }
    m_iuViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        handleSelectionChanged((IStructuredSelection) event.getSelection());
      }
    });

    // layout
    GridLayout layout = new GridLayout();
    layout.marginWidth = 2;
    layout.marginHeight = 8;
    composite.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_BOTH);
    composite.setLayoutData(gd);

    gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 150;
    gd.heightHint = 400;
    m_iuViewer.getControl().setLayoutData(gd);
  }

  private void createLicenseAcceptSection(Composite parent) {
    Composite buttonContainer = new Composite(parent, SWT.NULL);

    m_acceptButton = new Button(buttonContainer, SWT.RADIO);
    m_acceptButton.setText(Texts.get("LicDialogAcceptButton"));
    m_acceptButton.setSelection(false);
    m_acceptButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setComplete(m_acceptButton.getSelection());
      }
    });

    m_declineButton = new Button(buttonContainer, SWT.RADIO);
    m_declineButton.setText(Texts.get("LicDialogDeclineButton"));
    m_declineButton.setSelection(true);
    m_declineButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setComplete(!m_declineButton.getSelection());
      }
    });

    // layout
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    buttonContainer.setLayout(new GridLayout());
    buttonContainer.setLayoutData(gd);
  }

  private void handleSelectionChanged(IStructuredSelection selection) {
    m_licenseTextBox.setText("");
    if (!selection.isEmpty()) {
      Object selected = selection.getFirstElement();
      if (selected instanceof License) {
        m_licenseTextBox.setText(((License) selected).getBody());
      }
    }
  }

  public void setComplete(boolean complete) {
    m_complete = complete;
    if (getShell() != null && !getShell().isDisposed()) {
      getButton(IDialogConstants.OK_ID).setEnabled(complete);
    }
  }

  public boolean isComplete() {
    return m_complete;
  }

  private class P_LabelProvider extends LabelProvider {
    @Override
    public Image getImage(Object element) {
      return null;
    }

    @Override
    public String getText(Object element) {
      if (element instanceof String) {
        return (String) element;
      }
      else if (element instanceof License) {
        return ((License) element).getTitle();
      }
      return "";
    }
  }

  private class P_ContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      return m_iuToLicenses.keySet().toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
      if (!(parentElement instanceof String)) {
        return new Object[]{};
      }

      License[] lics = m_iuToLicenses.get(parentElement);
      if (lics != null && lics.length > 0) {
        return lics;
      }

      return null;
    }

    @Override
    public Object getParent(Object element) {
      if (element instanceof License) {
        License l = (License) element;
        return l.getInstallableUnitId();
      }
      return null;
    }

    @Override
    public boolean hasChildren(Object element) {
      return m_iuToLicenses.containsKey(element);
    }

    @Override
    public void dispose() {
      // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // Nothing to do
    }
  }
}
