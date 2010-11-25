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
import org.eclipse.core.runtime.IPath;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.nlsDirChooser.NlsDirChooserDialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class DirChooserField extends Composite {

  private Text m_text;
  private Label m_label;
  private Button m_button;
  private String[] m_extendsionFilter;
  private IProject m_project;
  private List<IInputChangedListener<IPath>> m_inputChangedListeners = new LinkedList<IInputChangedListener<IPath>>();
  private IPath m_path;

  public DirChooserField(Composite parent, String labelText, IProject project) {
    super(parent, SWT.NONE);
    m_project = project;
    createComponent(this);
    setLabelText(labelText);
  }

  protected void createComponent(Composite parent) {
    m_label = new Label(parent, SWT.NONE);
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

  public void setPath(IPath path) {
    m_path = path;
    if (m_path == null) {
      m_text.setText("");
    }
    else {
      m_text.setText(m_path.toPortableString());
    }
  }

  public IPath getPath() {
    return m_path;
  }

  public void setPathFire(IPath path) {
    setPath(path);
    for (IInputChangedListener<IPath> listener : m_inputChangedListeners) {
      listener.inputChanged(path);
    }
  }

  public String[] getExtendsionFilter() {
    return m_extendsionFilter;
  }

  public void setExtendsionFilter(String[] extendsionFilter) {
    m_extendsionFilter = extendsionFilter;
  }

  private void showFileChooserDialog() {
    NlsDirChooserDialog dialog = new NlsDirChooserDialog(getShell(), "Translation Dir Chooser", m_project);
    IPath path = dialog.openDialog();
    if (path != null) {
      setPathFire(path);
    }
  }

  public void addInputChangedListneer(IInputChangedListener<IPath> listener) {
    m_inputChangedListeners.add(listener);
  }

  public void removeInputChangedListneer(IInputChangedListener<IPath> listener) {
    m_inputChangedListeners.remove(listener);
  }

}
