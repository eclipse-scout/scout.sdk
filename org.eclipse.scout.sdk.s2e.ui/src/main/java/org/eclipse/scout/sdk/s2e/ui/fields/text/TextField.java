/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.fields.text;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.AbstractHyperlink;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * <h3>TextField with Label and Image support</h3>
 */
public class TextField extends Composite {

  /**
   * Type constant to have a normal label in front of the text field.
   */
  public static final int TYPE_LABEL = 1;

  /**
   * Type constant to have a hyperlink in front of the text field.
   */
  public static final int TYPE_HYPERLINK = 1 << 1;

  /**
   * Type constant to have an image between the label or hyperlink and the text field<br>
   * Use {@link #setImage(Image)} to apply an image.
   */
  public static final int TYPE_IMAGE = 1 << 2;

  /**
   * Type constant to have a multi line text field.
   */
  public static final int TYPE_MULTI_LINE = 1 << 3;

  /**
   * The default width of the label
   */
  public static final int DEFAULT_LABEL_WIDTH = 150;

  private Control m_label;
  private Label m_imgLabel;
  private StyledTextEx m_text;
  private Color m_backupTextBackground;

  private final int m_labelColumnWidth;
  private final int m_type;

  public TextField(Composite parent) {
    this(parent, TYPE_LABEL);
  }

  /**
   * @param parent
   *          a widget which will be the parent of the new instance. Must not be {@code null}.
   * @param type
   *          One of {@link #TYPE_LABEL}, {@link #TYPE_HYPERLINK}, {@link #TYPE_IMAGE}.
   */
  public TextField(Composite parent, int type) {
    this(parent, type, DEFAULT_LABEL_WIDTH);
  }

  /**
   * @param parent
   *          a widget which will be the parent of the new instance. Must not be {@code null}.
   * @param type
   *          One of {@link #TYPE_LABEL}, {@link #TYPE_HYPERLINK}, {@link #TYPE_IMAGE}.
   * @param labelWidth
   *          The width in pixels of the label. The image is considered to be part of the label width.
   */
  public TextField(Composite parent, int type, int labelWidth) {
    super(parent, SWT.NONE);
    m_labelColumnWidth = labelWidth;
    m_type = type;
    //noinspection ThisEscapedInObjectConstruction
    createContent(this);
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected void createContent(Composite parent) {
    boolean hasLabel = hasLabel();
    boolean isHyperLinkLabel = isHyperlinkLabel();
    boolean hasImage = hasImage();
    boolean isMultiLine = isMultiLine();

    // create controls
    if (isHyperLinkLabel) {
      Hyperlink hyperlink = new Hyperlink(parent, SWT.NONE);
      hyperlink.setUnderlined(true);

      Color fg = JFaceColors.getHyperlinkText(parent.getDisplay());
      if (fg == null) {
        fg = parent.getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND);
      }
      hyperlink.setForeground(fg);

      m_label = hyperlink;
    }
    else {
      m_label = new Label(parent, SWT.NONE);
    }
    m_label.setVisible(hasLabel);
    if (hasImage) {
      m_imgLabel = new Label(parent, SWT.NONE);
      m_imgLabel.setImage(getImage());
    }
    int textStyle = SWT.BORDER;
    if (isMultiLine) {
      textStyle |= SWT.MULTI;
    }
    else {
      textStyle |= SWT.SINGLE;
    }
    m_text = new StyledTextEx(parent, textStyle);

    // calculate offsets
    int textFieldMarginLeft = 0;
    int imgOffset = 0;
    int labelOffset = 0;
    if (hasLabel) {
      labelOffset = getLabelWidth();
    }
    if (hasImage) {
      imgOffset = 22;
    }
    if (hasLabel || hasImage) {
      textFieldMarginLeft = 5;
    }

    // layout
    parent.setLayout(new FormLayout());
    FormData labelData = new FormData();
    if (isHyperLinkLabel) {
      labelData.top = new FormAttachment(0, 3);
    }
    else {
      labelData.top = new FormAttachment(0, 4);
    }
    labelData.right = new FormAttachment(m_text, -textFieldMarginLeft - imgOffset);
    m_label.setLayoutData(labelData);
    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.right = new FormAttachment(100, 0);
    textData.left = new FormAttachment(0, textFieldMarginLeft + labelOffset + imgOffset);
    if (isMultiLine) {
      textData.bottom = new FormAttachment(100, 0); // grow vertically as long as there is space in the parent.
    }
    m_text.setLayoutData(textData);
    if (m_imgLabel != null) {
      FormData imgData = new FormData();
      imgData.top = new FormAttachment(0, 5);
      imgData.left = new FormAttachment(0, labelOffset + 6);
      m_imgLabel.setLayoutData(imgData);
    }
  }

  protected boolean isMultiLine() {
    return (getType() & TYPE_MULTI_LINE) != 0;
  }

  protected boolean hasLabel() {
    return (getType() & TYPE_HYPERLINK) != 0 || (getType() & TYPE_LABEL) != 0;
  }

  protected boolean hasImage() {
    return (getType() & TYPE_IMAGE) != 0;
  }

  protected boolean isHyperlinkLabel() {
    return (getType() & TYPE_HYPERLINK) != 0;
  }

  /**
   * @see Text#addFocusListener(FocusListener)
   */
  @Override
  public void addFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  /**
   * @see Text#removeFocusListener(FocusListener)
   */
  @Override
  public void removeFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  /**
   * @see Text#addVerifyListener(VerifyListener)
   */
  public void addVerifyListener(VerifyListener verifyListener) {
    m_text.addVerifyListener(verifyListener);
  }

  /**
   * @see Text#removeVerifyListener(VerifyListener)
   */
  public void removeVerifyListener(VerifyListener verifyListener) {
    m_text.removeVerifyListener(verifyListener);
  }

  /**
   * @see Text#addModifyListener(ModifyListener)
   */
  public void addModifyListener(ModifyListener listener) {
    m_text.addModifyListener(listener);
  }

  /**
   * @see Text#removeModifyListener(ModifyListener)
   */
  public void removeModifyListener(ModifyListener listener) {
    m_text.removeModifyListener(listener);
  }

  /**
   * @see AbstractHyperlink#removeHyperlinkListener(IHyperlinkListener)
   */
  public void removeHyperlinkListener(IHyperlinkListener listener) {
    if (!isHyperlinkLabel()) {
      return;
    }
    ((AbstractHyperlink) m_label).removeHyperlinkListener(listener);
  }

  /**
   * @see AbstractHyperlink#addHyperlinkListener(IHyperlinkListener)
   */
  public void addHyperlinkListener(IHyperlinkListener listener) {
    if (!isHyperlinkLabel()) {
      return;
    }
    ((AbstractHyperlink) m_label).addHyperlinkListener(listener);
  }

  /**
   * Sets the text of the label component.
   *
   * @param text
   *          The new text. May be {@code null}.
   */
  public void setLabelText(String text) {
    if (text == null) {
      text = "";
    }
    if (isHyperlinkLabel()) {
      ((Hyperlink) m_label).setText(text);
    }
    else {
      ((Label) m_label).setText(text);
    }
  }

  /**
   * @return The text of the label component
   */
  public String getLabelText() {
    if (isHyperlinkLabel()) {
      return ((AbstractHyperlink) m_label).getText();
    }
    return ((Label) m_label).getText();
  }

  @Override
  public void setToolTipText(String tooltip) {
    m_label.setToolTipText(tooltip);
    m_text.setToolTipText(tooltip);
    super.setToolTipText(tooltip);
  }

  @Override
  public Menu getMenu() {
    return m_text.getMenu();
  }

  @Override
  public void setMenu(Menu menu) {
    m_text.setMenu(menu);
  }

  /**
   * Sets the text of this text field.
   *
   * @param text
   *          The new text.
   */
  public void setText(String text) {
    if (text == null) {
      text = "";
    }
    m_text.setText(text);
  }

  /**
   * @return The current text of the text component
   */
  public String getText() {
    return m_text.getText();
  }

  /**
   * @see Text#setEditable(boolean)
   */
  public void setEditable(boolean editable) {
    m_text.setEditable(editable);
  }

  /**
   * @see Text#getEditable()
   */
  public boolean getEditable() {
    return m_text.getEditable();
  }

  /**
   * @see Text#getEditable()
   */
  public boolean isEditable() {
    return m_text.getEditable();
  }

  /**
   * @see Text#setFocus()
   */
  @Override
  public boolean setFocus() {
    return m_text.setFocus();
  }

  /**
   * @return The text component of this field
   */
  public StyledTextEx getTextComponent() {
    return m_text;
  }

  /**
   * @return The label component of this field
   */
  public Control getLabelComponent() {
    return m_label;
  }

  /**
   * @return The image component of this field.
   */
  public Label getImageComponent() {
    return m_imgLabel;
  }

  /**
   * @see Text#getSelection()
   */
  public Point getSelection() {
    return m_text.getSelection();
  }

  /**
   * @see Text#setSelection(int)
   */
  public void setSelection(int start) {
    m_text.setSelection(start);
  }

  /**
   * @see Text#setSelection(Point)
   */
  public void setSelection(Point point) {
    m_text.setSelection(point);
  }

  /**
   * @see Text#getEnabled()
   */
  @Override
  public boolean getEnabled() {
    if (m_text == null || m_text.isDisposed()) {
      return super.getEnabled();
    }
    return m_text.getEnabled();
  }

  /**
   * @see Composite#isEnabled()
   */
  @Override
  public boolean isEnabled() {
    boolean enabled = super.isEnabled();
    if (m_text == null || m_text.isDisposed()) {
      return enabled;
    }
    return enabled && m_text.getEnabled();
  }

  /**
   * @see Text#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean enabled) {
    if (m_backupTextBackground == null) {
      m_backupTextBackground = m_text.getBackground();
    }
    m_text.setEnabled(enabled);
    m_label.setEnabled(enabled);
    if (enabled) {
      m_text.setBackground(m_backupTextBackground);
    }
    else {
      m_text.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
    }
  }

  /**
   * @return The width of the text component.
   */
  public int getLabelWidth() {
    return m_labelColumnWidth;
  }

  /**
   * @return The type of this field.
   * @see #TYPE_HYPERLINK
   * @see #TYPE_LABEL
   * @see #TYPE_IMAGE
   */
  public int getType() {
    return m_type;
  }

  /**
   * @return The image of this field. Only valid if this field has been created using {@link #TYPE_IMAGE}
   */
  public Image getImage() {
    if (m_imgLabel == null) {
      return null;
    }
    return m_imgLabel.getImage();
  }

  /**
   * Sets the image of this field. Only valid if this field has been created using {@link #TYPE_IMAGE}
   *
   * @param image
   *          The new image or {@code null} if not image should be shown.
   */
  public void setImage(Image image) {
    if (m_imgLabel == null) {
      return;
    }
    m_imgLabel.setImage(image);
  }
}
