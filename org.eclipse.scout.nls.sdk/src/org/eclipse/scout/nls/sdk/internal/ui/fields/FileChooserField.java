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
package org.eclipse.scout.nls.sdk.internal.ui.fields;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FileChooserField extends Composite {

  private Text m_text;
  private Label m_label;
  private Button m_button;
  private String[] m_extendsionFilter;
  private String m_fileName;
  private String m_title;
  private List<IInputChangedListener<String>> m_focusOutListener = new LinkedList<>();
  private List<IInputChangedListener<String>> m_modifyListener = new LinkedList<>();

  public FileChooserField(Composite parent, String title) {
    super(parent, SWT.NONE);
    m_title = title;
    createComponent(this);
  }

  protected void createComponent(Composite parent) {
    m_label = new Label(parent, SWT.NONE);
    m_text = new Text(parent, SWT.BORDER);
    m_text.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        for (IInputChangedListener<String> listener : m_modifyListener) {
          listener.inputChanged(m_text.getText());
        }
      }
    });
    m_text.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        for (IInputChangedListener<String> listener : m_focusOutListener) {
          listener.inputChanged(m_text.getText());
        }
      }
    });
    m_button = new Button(parent, SWT.PUSH | SWT.FLAT);
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
    data.right = new FormAttachment(10, 0);
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

  /**
   * @param type
   *          SWT.Modify or SWT.FocusOut
   * @param listener
   */
  public void addInputChangedListener(int type, IInputChangedListener<String> listener) {
    if (type == SWT.FocusOut) {
      m_focusOutListener.add(listener);
    }
    else if (type == SWT.Modify) {
      m_modifyListener.add(listener);
    }
  }

  /**
   * @param type
   *          SWT.Modify or SWT.FocusOut
   * @param listener
   */
  public void removeInputChangedListener(int type, IInputChangedListener<String> listener) {
    if (type == SWT.FocusOut) {
      m_focusOutListener.remove(listener);
    }
    else if (type == SWT.Modify) {
      m_modifyListener.remove(listener);
    }
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

  public void setEditable(boolean editable) {
    m_text.setEditable(editable);
  }

  public void setValue(String s) {
    m_text.setText(s);
  }

  public String[] getExtendsionFilter() {
    return m_extendsionFilter;
  }

  public void setExtendsionFilter(String[] extendsionFilter) {
    m_extendsionFilter = extendsionFilter;
  }

  private void showFileChooserDialog() {
    FileDialog dialog = new FileDialog(getShell());
    if (m_extendsionFilter != null) {
      dialog.setFilterExtensions(m_extendsionFilter);
    }
    dialog.setText(m_title);
    if (!StringUtility.isNullOrEmpty(getFileName())) {
      dialog.setFileName(getFileName());
    }
    String file = dialog.open();
    if (file != null) {
      setSelectedFile(file);
    }

  }

  private void setSelectedFile(String fileName) {
    m_text.setText(fileName);
    for (IInputChangedListener<String> listener : m_focusOutListener) {
      listener.inputChanged(fileName);
    }
    for (IInputChangedListener<String> listener : m_modifyListener) {
      listener.inputChanged(fileName);
    }
  }

  /**
   * @param fileName
   *          the fileName to set
   */
  public void setFileName(String fileName) {
    m_fileName = fileName;
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return m_fileName;
  }

}
