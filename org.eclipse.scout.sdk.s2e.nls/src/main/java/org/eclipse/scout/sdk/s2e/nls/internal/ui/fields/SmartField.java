/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.fields;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SmartField extends Composite {
  private Text m_text;
  private Label m_label;
  private Button m_smartButton;
  private ISmartFieldModel m_smartFieldModel;
  private SmartDialog m_smartDialog;
  private String m_lastVerifiedInput = "";
  private Font m_font;
  private Image m_smartImage;
  private final int m_labelColWidth;
  private List<ISmartFieldListener> m_smartFieldListenerList = new LinkedList<>();

  public SmartField(Composite parent, int style) {
    this(parent, style, 40);
  }

  public SmartField(Composite parent, int style, int labelColWidth) {
    super(parent, style);
    m_smartImage = NlsCore.getImage(INlsIcons.MAGNIFIER);
    m_labelColWidth = labelColWidth;
    createComponent(this);
  }

  protected void createComponent(Composite parent) {
    m_smartDialog = new SmartDialog(parent.getShell());
    m_smartDialog.addSmartDialogListener(new ISmartDialogListener() {
      @Override
      public void itemSelected(Object item) {
        m_text.setText(m_smartFieldModel.getText(item));
        m_lastVerifiedInput = m_text.getText();
        // notify all listeners
        fireInputChanged(item);
      }
    });

    m_label = new Label(parent, SWT.INHERIT_DEFAULT | SWT.TRAIL);
    m_text = new Text(parent, SWT.INHERIT_DEFAULT | SWT.BORDER);
    m_text.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        if (!m_text.getText().equals(m_lastVerifiedInput)) {
          handleFocusLost();
        }
      }
    });
    m_text.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == SWT.ESC) {
          return;
        }
        showSmartDialog(m_text.getText());
      }
    });
    m_text.addTraverseListener(new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent e) {
        List<Object> props = null;
        if (e.character == SWT.ESC) {
          props = m_smartFieldModel.getProposals(m_lastVerifiedInput);
          e.doit = !m_smartDialog.isVisible();
        }
        else {
          String text = m_text.getText();
          if (text != null && text.length() > 0) {
            props = m_smartFieldModel.getProposals(m_text.getText());
            if (props.size() == 0) {
              props = m_smartFieldModel.getProposals(m_lastVerifiedInput);
            }
          }
        }
        if (props != null && props.size() > 0) {
          m_smartDialog.notifyItemSelection(props.get(0));
        }
      }
    });
    m_smartButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_smartButton.setImage(m_smartImage);
    m_smartButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (!m_text.getText().equals(m_lastVerifiedInput)) {
          showSmartDialog(m_text.getText());
        }
        else {
          showSmartDialog("");
        }
      }
    });
    parent.setTabList(new Control[]{m_text});

    // layout
    parent.setLayout(new FormLayout());

    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.bottom = new FormAttachment(100, 0);
    data.right = new FormAttachment(m_labelColWidth, 0);
    m_label.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(m_label, 5);
    data.bottom = new FormAttachment(100, 0);
    data.right = new FormAttachment(m_smartButton, -2);
    m_text.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.bottom = new FormAttachment(100, 0);
    data.right = new FormAttachment(100, 0);
    m_smartButton.setLayoutData(data);
  }

  protected void handleFocusLost() {
    String txt = m_text.getText();
    if (txt == null || txt.length() < 1) {
      fireInputChanged(null);
    }
    else {
      m_text.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          showSmartDialogLazy(m_text.getText());
        }
      });
    }
  }

  private void fireInputChanged(Object item) {
    List<ISmartFieldListener> list = new ArrayList<>(m_smartFieldListenerList);
    for (ISmartFieldListener listener : list) {
      listener.itemSelected(item);
    }
  }

  protected void showSmartDialog(String text) {
    Rectangle textBounds = m_text.getBounds();
    Point location = m_text.toDisplay(new Point(0, textBounds.height));
    Rectangle popupBounds = new Rectangle(location.x, location.y, textBounds.width, SWT.DEFAULT);
    m_smartDialog.open(popupBounds, text);
  }

  protected void showSmartDialogLazy(String text) {
    Rectangle textBounds = m_text.getBounds();
    Point location = m_text.toDisplay(new Point(0, textBounds.height));
    Rectangle popupBounds = new Rectangle(location.x, location.y, textBounds.width, SWT.DEFAULT);
    m_smartDialog.lazyOpen(popupBounds, text);
  }

  public void addSmartFieldListener(ISmartFieldListener listener) {
    m_smartFieldListenerList.add(listener);
  }

  public void removeSmartFieldListener(ISmartFieldListener listener) {
    m_smartFieldListenerList.remove(listener);
  }

  public void setValue(Object value) {
    String txt = m_smartFieldModel.getText(value);
    m_text.setText(txt);
    m_text.setSelection(0, txt.length());
    m_lastVerifiedInput = m_text.getText();
    fireInputChanged(value);
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_smartButton.setEnabled(enabled);
    m_text.setEnabled(enabled);
  }

  @Override
  public boolean getEnabled() {
    return m_smartButton.getEnabled();
  }

  public void setDefaultFont(Font font) {
    m_font = font;
    m_smartDialog.setFont(font);
    m_text.setFont(font);
    m_label.setFont(font);
  }

  public Font getDefaultFont() {
    return m_font;
  }

  public void setImage(Image image) {
    m_smartImage = image;
    m_smartButton.setImage(image);
  }

  public ISmartFieldModel getSmartFieldModel() {
    return m_smartFieldModel;
  }

  public void setSmartFieldModel(ISmartFieldModel model) {
    m_smartFieldModel = model;
    m_smartDialog.setSmartFieldModel(model);
  }

  @Override
  public void addFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  @Override
  public void removeFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  public void setLabel(String label) {
    m_label.setText(label);
  }

  public void setText(String text) {
    m_text.setText(text);
  }

  public String getText() {
    return m_text.getText();
  }

  public void setLabelFont(Font font) {
    m_label.setFont(font);
  }

  public Font getLabelFont() {
    return m_label.getFont();
  }

  public void setTextFont(Font font) {
    m_text.setFont(font);
  }

  public Font getTextFont() {
    return m_text.getFont();
  }

  public void setTextLimit(int limit) {
    m_text.setTextLimit(limit);
  }

  public int getTextLimit() {
    return m_text.getTextLimit();
  }

  @Override
  public void dispose() {
    super.dispose();
    if (m_smartImage != null && (!m_smartImage.isDisposed())) {
      m_smartImage.dispose();
    }
  }

}
