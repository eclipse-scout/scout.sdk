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
package org.eclipse.scout.nls.sdk.internal.ui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.NlsTypeSourceFilter;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class TypeChooserField extends Composite {

  private Text m_text;
  private Hyperlink m_label;
  private Button m_button;
  private IProject[] m_projects;
  private String m_title;
  private Object m_value;
  private List<IInputChangedListener<IType>> m_inputChangedListeners = new LinkedList<IInputChangedListener<IType>>();
  private FormToolkit m_toolkit;

  public TypeChooserField(Composite parent, FormToolkit toolkit, String title) {
    super(parent, SWT.NONE);
    m_toolkit = toolkit;
    m_title = title;
    createComponent(this);
  }

  public void addHyperlinkListner(IHyperlinkListener listener) {
    m_label.addHyperlinkListener(listener);
  }

  protected void createComponent(Composite parent) {
    m_label = m_toolkit.createHyperlink(parent, m_title, SWT.NONE);

    // m_label = new Label(parent, SWT.NONE);
    m_text = new Text(parent, SWT.BORDER);
    m_button = new Button(parent, SWT.PUSH);
    m_button.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showFileChooserDialog();
      }
    });
    // layout
    setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(40, 0);
    data.bottom = new FormAttachment(100, 0);
    m_label.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(m_label, 5);
    data.right = new FormAttachment(m_button, -5);
    data.bottom = new FormAttachment(100, 0);
    m_text.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(100, 0);
    m_button.setLayoutData(data);

  }

  public void addInputChangedListneer(IInputChangedListener<IType> listener) {
    m_inputChangedListeners.add(listener);
  }

  public void removeInputChangedListneer(IInputChangedListener<IType> listener) {
    m_inputChangedListeners.remove(listener);
  }

  public void setButtonImage(Image img) {
    m_button.setImage(img);
  }

  public Image getButtonImage() {
    return m_button.getImage();
  }

  public void setButtonText(String string) {
    m_button.setText(string);
  }

  public String getButtonText() {
    return m_button.getText();
  }

  public void setLabelText(String text) {
    m_label.setText(text);
  }

  public String getLabelText() {
    return m_label.getText();
  }

  public void setType(IType input) {
    m_value = input;
    if (input == null) {
      m_text.setText("");
    }
    else {
      m_text.setText(input.getFullyQualifiedName());
    }
  }

  private void setTypeFire(IType type) {
    setType(type);
    for (IInputChangedListener<IType> listener : m_inputChangedListeners) {
      listener.inputChanged(type);
    }
  }

  public IType getType() {
    return (IType) m_value;
  }

  public IProject[] getProjects() {
    return m_projects;
  }

  public void setProjects(IProject[] projects) {
    m_projects = projects;
  }

  private void showFileChooserDialog() {
    try {
      SelectionDialog dialog = JavaUI.createTypeDialog(getShell(), PlatformUI.getWorkbench().getProgressService(),
          new NlsTypeSourceFilter(m_projects), IJavaElementSearchConstants.CONSIDER_CLASSES, false);
      dialog.setTitle(m_title);

      if (dialog.open() == Dialog.OK) {
        Object[] result = dialog.getResult();
        setTypeFire((IType) result[0]);
      }
    }
    catch (JavaModelException e) {
      // TODO Auto-generated catch block
      NlsCore.logWarning(e);
    }

  }

}
