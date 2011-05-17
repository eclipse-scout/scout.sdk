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
package org.eclipse.scout.sdk.ui.view.properties.part;

import org.eclipse.scout.sdk.ui.util.TableWrapDataEx;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * <h3>{@link Section}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 */
public class Section implements ISection {

  private org.eclipse.ui.forms.widgets.Section m_uiSection;
  private String m_sectionId;
  private final ScrolledForm m_form;

  public Section(String sectionId, ScrolledForm form) {
    m_sectionId = sectionId;
    m_form = form;
  }

  public String getSectionId() {
    return m_sectionId;
  }

  void createSection(FormToolkit toolkit, Composite parent, String title, String description, int style, Section sibling) {
    m_uiSection = toolkit.createSection(parent, style);
    m_uiSection.addExpansionListener(new ExpansionAdapter() {
      @Override
      public void expansionStateChanged(ExpansionEvent e) {
        m_form.reflow(true);
      }
    });
    m_uiSection.setText(title);
    if ((style & org.eclipse.ui.forms.widgets.Section.DESCRIPTION) != 0) {
      m_uiSection.setDescription(description);
    }
    // position
    if (sibling != null) {
      m_uiSection.moveAbove(sibling.getUiSection());
    }
    // layout
    TableWrapDataEx data = new TableWrapDataEx(TableWrapData.FILL_GRAB);
    data.maxWidth = 260; // to scale presenter fill width properly. 260 = 2 x labelWidth
    m_uiSection.setLayoutData(data);
    Composite sectionClient = toolkit.createComposite(m_uiSection);
    m_uiSection.setClient(sectionClient);
    GridLayout layout = new GridLayout(1, true);
    layout.horizontalSpacing = 3;
    layout.verticalSpacing = 4;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    sectionClient.setLayout(layout);
  }

  /**
   * @return the uiSection
   */
  public org.eclipse.ui.forms.widgets.Section getUiSection() {
    return m_uiSection;
  }

  public Composite getSectionClient() {
    if (m_uiSection == null || m_uiSection.isDisposed()) {
      throw new IllegalStateException("section has not been created or is disposed.");
    }
    return (Composite) m_uiSection.getClient();
  }

  public boolean isVisible() {
    return m_uiSection.isVisible();
  }

  public void setVisible(boolean visible) {
    if (isVisible() != visible) { // do this check as this call is very expensive
      Object layoutData = m_uiSection.getLayoutData();
      if (layoutData instanceof TableWrapDataEx) {
        ((TableWrapDataEx) layoutData).exclude = !visible;
      }
      m_uiSection.setVisible(visible);
      m_form.layout(true, true);
      m_form.reflow(true);
    }
  }

  public boolean isExpanded() {
    return m_uiSection.isExpanded();
  }

  public void setExpanded(boolean expanded) {
    m_uiSection.setExpanded(expanded);
  }

  /**
   * @param listener
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#addExpansionListener(org.eclipse.ui.forms.events.IExpansionListener)
   */
  public void addExpansionListener(IExpansionListener listener) {
    m_uiSection.addExpansionListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#addMouseWheelListener(org.eclipse.swt.events.MouseWheelListener)
   */
  public void addMouseWheelListener(MouseWheelListener listener) {
    m_uiSection.addMouseWheelListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#addTraverseListener(org.eclipse.swt.events.TraverseListener)
   */
  public void addTraverseListener(TraverseListener listener) {
    m_uiSection.addTraverseListener(listener);
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#forceFocus()
   */
  public boolean forceFocus() {
    return m_uiSection.forceFocus();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBackground()
   */
  public Color getBackground() {
    return m_uiSection.getBackground();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBackgroundImage()
   */
  public Image getBackgroundImage() {
    return m_uiSection.getBackgroundImage();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Composite#getBackgroundMode()
   */
  public int getBackgroundMode() {
    return m_uiSection.getBackgroundMode();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBorderWidth()
   */
  public int getBorderWidth() {
    return m_uiSection.getBorderWidth();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBounds()
   */
  public Rectangle getBounds() {
    return m_uiSection.getBounds();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Canvas#getCaret()
   */
  public Caret getCaret() {
    return m_uiSection.getCaret();
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.Section#getDescription()
   */
  public String getDescription() {
    return m_uiSection.getDescription();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Widget#getDisplay()
   */
  public Display getDisplay() {
    return m_uiSection.getDisplay();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getDragDetect()
   */
  public boolean getDragDetect() {
    return m_uiSection.getDragDetect();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getEnabled()
   */
  public boolean getEnabled() {
    return m_uiSection.getEnabled();
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#getExpansionStyle()
   */
  public int getExpansionStyle() {
    return m_uiSection.getExpansionStyle();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getFont()
   */
  public Font getFont() {
    return m_uiSection.getFont();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getForeground()
   */
  public Color getForeground() {
    return m_uiSection.getForeground();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getMenu()
   */
  public Menu getMenu() {
    return m_uiSection.getMenu();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getMonitor()
   */
  public Monitor getMonitor() {
    return m_uiSection.getMonitor();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getShell()
   */
  public Shell getShell() {
    return m_uiSection.getShell();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getSize()
   */
  public Point getSize() {
    return m_uiSection.getSize();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Widget#getStyle()
   */
  public int getStyle() {
    return m_uiSection.getStyle();
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#getText()
   */
  public String getText() {
    return m_uiSection.getText();
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#getTextClientHeightDifference()
   */
  public int getTextClientHeightDifference() {
    return m_uiSection.getTextClientHeightDifference();
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.Section#getTitleBarBackground()
   */
  public Color getTitleBarBackground() {
    return m_uiSection.getTitleBarBackground();
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.Section#getTitleBarBorderColor()
   */
  public Color getTitleBarBorderColor() {
    return m_uiSection.getTitleBarBorderColor();
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#getTitleBarForeground()
   */
  public Color getTitleBarForeground() {
    return m_uiSection.getTitleBarForeground();
  }

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.Section#getTitleBarGradientBackground()
   */
  public Color getTitleBarGradientBackground() {
    return m_uiSection.getTitleBarGradientBackground();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getToolTipText()
   */
  public String getToolTipText() {
    return m_uiSection.getToolTipText();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getVisible()
   */
  public boolean getVisible() {
    return m_uiSection.getVisible();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Widget#isDisposed()
   */
  public boolean isDisposed() {
    return m_uiSection.isDisposed();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#isEnabled()
   */
  public boolean isEnabled() {
    return m_uiSection.isEnabled();
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#isFocusControl()
   */
  public boolean isFocusControl() {
    return m_uiSection.isFocusControl();
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#layout()
   */
  public void layout() {
    m_uiSection.layout();
  }

  /**
   * @param changed
   * @param all
   * @see org.eclipse.swt.widgets.Composite#layout(boolean, boolean)
   */
  public void layout(boolean changed, boolean all) {
    m_uiSection.layout(changed, all);
  }

  /**
   * @param changed
   * @see org.eclipse.swt.widgets.Composite#layout(boolean)
   */
  public void layout(boolean changed) {
    m_uiSection.layout(changed);
  }

  /**
   * @param changed
   * @see org.eclipse.swt.widgets.Composite#layout(org.eclipse.swt.widgets.Control[])
   */
  public void layout(Control[] changed) {
    m_uiSection.layout(changed);
  }

  /**
   * @see org.eclipse.swt.widgets.Control#pack()
   */
  public void pack() {
    m_uiSection.pack();
  }

  /**
   * @param changed
   * @see org.eclipse.swt.widgets.Control#pack(boolean)
   */
  public void pack(boolean changed) {
    m_uiSection.pack(changed);
  }

  /**
   * @param listener
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#removeExpansionListener(org.eclipse.ui.forms.events.IExpansionListener)
   */
  public void removeExpansionListener(IExpansionListener listener) {
    m_uiSection.removeExpansionListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
   */
  public void removeKeyListener(KeyListener listener) {
    m_uiSection.removeKeyListener(listener);
  }

  /**
   * @param eventType
   * @param listener
   * @see org.eclipse.swt.widgets.Widget#removeListener(int, org.eclipse.swt.widgets.Listener)
   */
  public void removeListener(int eventType, Listener listener) {
    m_uiSection.removeListener(eventType, listener);
  }

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#removeTraverseListener(org.eclipse.swt.events.TraverseListener)
   */
  public void removeTraverseListener(TraverseListener listener) {
    m_uiSection.removeTraverseListener(listener);
  }

  /**
   * @param bg
   * @see org.eclipse.ui.forms.widgets.Section#setBackground(org.eclipse.swt.graphics.Color)
   */
  public void setBackground(Color bg) {
    m_uiSection.setBackground(bg);
  }

  /**
   * @param image
   * @see org.eclipse.ui.forms.widgets.Section#setBackgroundImage(org.eclipse.swt.graphics.Image)
   */
  public final void setBackgroundImage(Image image) {
    m_uiSection.setBackgroundImage(image);
  }

  /**
   * @param mode
   * @see org.eclipse.swt.widgets.Composite#setBackgroundMode(int)
   */
  public void setBackgroundMode(int mode) {
    m_uiSection.setBackgroundMode(mode);
  }

  /**
   * @param x
   * @param y
   * @param width
   * @param height
   * @see org.eclipse.swt.widgets.Control#setBounds(int, int, int, int)
   */
  public void setBounds(int x, int y, int width, int height) {
    m_uiSection.setBounds(x, y, width, height);
  }

  /**
   * @param rect
   * @see org.eclipse.swt.widgets.Control#setBounds(org.eclipse.swt.graphics.Rectangle)
   */
  public void setBounds(Rectangle rect) {
    m_uiSection.setBounds(rect);
  }

  /**
   * @param capture
   * @see org.eclipse.swt.widgets.Control#setCapture(boolean)
   */
  public void setCapture(boolean capture) {
    m_uiSection.setCapture(capture);
  }

  /**
   * @param caret
   * @see org.eclipse.swt.widgets.Canvas#setCaret(org.eclipse.swt.widgets.Caret)
   */
  public void setCaret(Caret caret) {
    m_uiSection.setCaret(caret);
  }

  /**
   * @param cursor
   * @see org.eclipse.swt.widgets.Control#setCursor(org.eclipse.swt.graphics.Cursor)
   */
  public void setCursor(Cursor cursor) {
    m_uiSection.setCursor(cursor);
  }

  /**
   * @param description
   * @see org.eclipse.ui.forms.widgets.Section#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
    m_uiSection.setDescription(description);
  }

  /**
   * @param dragDetect
   * @see org.eclipse.swt.widgets.Control#setDragDetect(boolean)
   */
  public void setDragDetect(boolean dragDetect) {
    m_uiSection.setDragDetect(dragDetect);
  }

  /**
   * @param enabled
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled) {
    m_uiSection.setEnabled(enabled);
  }

  /**
   * @return
   * @see org.eclipse.swt.widgets.Composite#setFocus()
   */
  public boolean setFocus() {
    return m_uiSection.setFocus();
  }

  /**
   * @param font
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setFont(org.eclipse.swt.graphics.Font)
   */
  public void setFont(Font font) {
    m_uiSection.setFont(font);
  }

  /**
   * @param fg
   * @see org.eclipse.ui.forms.widgets.Section#setForeground(org.eclipse.swt.graphics.Color)
   */
  public void setForeground(Color fg) {
    m_uiSection.setForeground(fg);
  }

  /**
   * @param menu
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setMenu(org.eclipse.swt.widgets.Menu)
   */
  public void setMenu(Menu menu) {
    m_uiSection.setMenu(menu);
  }

  /**
   * @param width
   * @param height
   * @see org.eclipse.swt.widgets.Control#setSize(int, int)
   */
  public void setSize(int width, int height) {
    m_uiSection.setSize(width, height);
  }

  /**
   * @param size
   * @see org.eclipse.swt.widgets.Control#setSize(org.eclipse.swt.graphics.Point)
   */
  public void setSize(Point size) {
    m_uiSection.setSize(size);
  }

  /**
   * @param title
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setText(java.lang.String)
   */
  public void setText(String title) {
    m_uiSection.setText(title);
  }

  /**
   * @param color
   * @see org.eclipse.ui.forms.widgets.Section#setTitleBarBackground(org.eclipse.swt.graphics.Color)
   */
  public void setTitleBarBackground(Color color) {
    m_uiSection.setTitleBarBackground(color);
  }

  /**
   * @param color
   * @see org.eclipse.ui.forms.widgets.Section#setTitleBarBorderColor(org.eclipse.swt.graphics.Color)
   */
  public void setTitleBarBorderColor(Color color) {
    m_uiSection.setTitleBarBorderColor(color);
  }

  /**
   * @param color
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setTitleBarForeground(org.eclipse.swt.graphics.Color)
   */
  public void setTitleBarForeground(Color color) {
    m_uiSection.setTitleBarForeground(color);
  }

  /**
   * @param color
   * @see org.eclipse.ui.forms.widgets.Section#setTitleBarGradientBackground(org.eclipse.swt.graphics.Color)
   */
  public void setTitleBarGradientBackground(Color color) {
    m_uiSection.setTitleBarGradientBackground(color);
  }

  /**
   * @param c
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setToggleColor(org.eclipse.swt.graphics.Color)
   */
  public void setToggleColor(Color c) {
    m_uiSection.setToggleColor(c);
  }

  /**
   * @param string
   * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
   */
  public void setToolTipText(String string) {
    m_uiSection.setToolTipText(string);
  }

  /**
   * @param x
   * @param y
   * @return
   * @see org.eclipse.swt.widgets.Control#toControl(int, int)
   */
  public Point toControl(int x, int y) {
    return m_uiSection.toControl(x, y);
  }

  /**
   * @param point
   * @return
   * @see org.eclipse.swt.widgets.Control#toControl(org.eclipse.swt.graphics.Point)
   */
  public Point toControl(Point point) {
    return m_uiSection.toControl(point);
  }

  /**
   * @param x
   * @param y
   * @return
   * @see org.eclipse.swt.widgets.Control#toDisplay(int, int)
   */
  public Point toDisplay(int x, int y) {
    return m_uiSection.toDisplay(x, y);
  }

  /**
   * @param point
   * @return
   * @see org.eclipse.swt.widgets.Control#toDisplay(org.eclipse.swt.graphics.Point)
   */
  public Point toDisplay(Point point) {
    return m_uiSection.toDisplay(point);
  }

  /**
   * @param traversal
   * @return
   * @see org.eclipse.swt.widgets.Control#traverse(int)
   */
  public boolean traverse(int traversal) {
    return m_uiSection.traverse(traversal);
  }

  /**
   * @see org.eclipse.swt.widgets.Control#update()
   */
  public void update() {
    m_uiSection.update();
  }

}
