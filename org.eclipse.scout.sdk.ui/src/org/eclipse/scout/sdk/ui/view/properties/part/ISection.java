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

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.IExpansionListener;

/**
 * <h3>{@link ISection}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 */
public interface ISection {
  String getSectionId();

  Composite getSectionClient();

  boolean isVisible();

  void setVisible(boolean visible);

  boolean isExpanded();

  void setExpanded(boolean expanded);

  /**
   * @param listener
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#addExpansionListener(org.eclipse.ui.forms.events.IExpansionListener)
   */
  void addExpansionListener(IExpansionListener listener);

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#addMouseWheelListener(org.eclipse.swt.events.MouseWheelListener)
   */
  void addMouseWheelListener(MouseWheelListener listener);

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#addTraverseListener(org.eclipse.swt.events.TraverseListener)
   */
  void addTraverseListener(TraverseListener listener);

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#forceFocus()
   */
  boolean forceFocus();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBackground()
   */
  Color getBackground();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBackgroundImage()
   */
  Image getBackgroundImage();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Composite#getBackgroundMode()
   */
  int getBackgroundMode();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBorderWidth()
   */
  int getBorderWidth();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getBounds()
   */
  Rectangle getBounds();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Canvas#getCaret()
   */
  Caret getCaret();

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.Section#getDescription()
   */
  String getDescription();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Widget#getDisplay()
   */
  Display getDisplay();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getDragDetect()
   */
  boolean getDragDetect();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getEnabled()
   */
  boolean getEnabled();

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#getExpansionStyle()
   */
  int getExpansionStyle();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getFont()
   */
  Font getFont();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getForeground()
   */
  Color getForeground();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getMenu()
   */
  Menu getMenu();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getMonitor()
   */
  Monitor getMonitor();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getShell()
   */
  Shell getShell();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getSize()
   */
  Point getSize();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Widget#getStyle()
   */
  int getStyle();

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#getText()
   */
  String getText();

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#getTextClientHeightDifference()
   */
  int getTextClientHeightDifference();

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.Section#getTitleBarBackground()
   */
  Color getTitleBarBackground();

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.Section#getTitleBarBorderColor()
   */
  Color getTitleBarBorderColor();

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#getTitleBarForeground()
   */
  Color getTitleBarForeground();

  /**
   * @return
   * @see org.eclipse.ui.forms.widgets.Section#getTitleBarGradientBackground()
   */
  Color getTitleBarGradientBackground();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getToolTipText()
   */
  String getToolTipText();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#getVisible()
   */
  boolean getVisible();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Widget#isDisposed()
   */
  boolean isDisposed();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#isEnabled()
   */
  boolean isEnabled();

  /**
   * @return
   * @see org.eclipse.swt.widgets.Control#isFocusControl()
   */
  boolean isFocusControl();

  /**
   * @see org.eclipse.swt.widgets.Composite#layout()
   */
  void layout();

  /**
   * @param changed
   * @param all
   * @see org.eclipse.swt.widgets.Composite#layout(boolean, boolean)
   */
  void layout(boolean changed, boolean all);

  /**
   * @param changed
   * @see org.eclipse.swt.widgets.Composite#layout(boolean)
   */
  void layout(boolean changed);

  /**
   * @param changed
   * @see org.eclipse.swt.widgets.Composite#layout(org.eclipse.swt.widgets.Control[])
   */
  void layout(Control[] changed);

  /**
   * @see org.eclipse.swt.widgets.Control#pack()
   */
  void pack();

  /**
   * @param changed
   * @see org.eclipse.swt.widgets.Control#pack(boolean)
   */
  void pack(boolean changed);

  /**
   * @param listener
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#removeExpansionListener(org.eclipse.ui.forms.events.IExpansionListener)
   */
  void removeExpansionListener(IExpansionListener listener);

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
   */
  void removeKeyListener(KeyListener listener);

  /**
   * @param eventType
   * @param listener
   * @see org.eclipse.swt.widgets.Widget#removeListener(int, org.eclipse.swt.widgets.Listener)
   */
  void removeListener(int eventType, Listener listener);

  /**
   * @param listener
   * @see org.eclipse.swt.widgets.Control#removeTraverseListener(org.eclipse.swt.events.TraverseListener)
   */
  void removeTraverseListener(TraverseListener listener);

  /**
   * @param bg
   * @see org.eclipse.ui.forms.widgets.Section#setBackground(org.eclipse.swt.graphics.Color)
   */
  void setBackground(Color bg);

  /**
   * @param image
   * @see org.eclipse.ui.forms.widgets.Section#setBackgroundImage(org.eclipse.swt.graphics.Image)
   */
  void setBackgroundImage(Image image);

  /**
   * @param mode
   * @see org.eclipse.swt.widgets.Composite#setBackgroundMode(int)
   */
  void setBackgroundMode(int mode);

  /**
   * @param x
   * @param y
   * @param width
   * @param height
   * @see org.eclipse.swt.widgets.Control#setBounds(int, int, int, int)
   */
  void setBounds(int x, int y, int width, int height);

  /**
   * @param rect
   * @see org.eclipse.swt.widgets.Control#setBounds(org.eclipse.swt.graphics.Rectangle)
   */
  void setBounds(Rectangle rect);

  /**
   * @param capture
   * @see org.eclipse.swt.widgets.Control#setCapture(boolean)
   */
  void setCapture(boolean capture);

  /**
   * @param caret
   * @see org.eclipse.swt.widgets.Canvas#setCaret(org.eclipse.swt.widgets.Caret)
   */
  void setCaret(Caret caret);

  /**
   * @param cursor
   * @see org.eclipse.swt.widgets.Control#setCursor(org.eclipse.swt.graphics.Cursor)
   */
  void setCursor(Cursor cursor);

  /**
   * @param description
   * @see org.eclipse.ui.forms.widgets.Section#setDescription(java.lang.String)
   */
  void setDescription(String description);

  /**
   * @param dragDetect
   * @see org.eclipse.swt.widgets.Control#setDragDetect(boolean)
   */
  void setDragDetect(boolean dragDetect);

  /**
   * @param enabled
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setEnabled(boolean)
   */
  void setEnabled(boolean enabled);

  /**
   * @return
   * @see org.eclipse.swt.widgets.Composite#setFocus()
   */
  boolean setFocus();

  /**
   * @param font
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setFont(org.eclipse.swt.graphics.Font)
   */
  void setFont(Font font);

  /**
   * @param fg
   * @see org.eclipse.ui.forms.widgets.Section#setForeground(org.eclipse.swt.graphics.Color)
   */
  void setForeground(Color fg);

  /**
   * @param menu
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setMenu(org.eclipse.swt.widgets.Menu)
   */
  void setMenu(Menu menu);

  /**
   * @param width
   * @param height
   * @see org.eclipse.swt.widgets.Control#setSize(int, int)
   */
  void setSize(int width, int height);

  /**
   * @param size
   * @see org.eclipse.swt.widgets.Control#setSize(org.eclipse.swt.graphics.Point)
   */
  void setSize(Point size);

  /**
   * @param title
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setText(java.lang.String)
   */
  void setText(String title);

  /**
   * @param color
   * @see org.eclipse.ui.forms.widgets.Section#setTitleBarBackground(org.eclipse.swt.graphics.Color)
   */
  void setTitleBarBackground(Color color);

  /**
   * @param color
   * @see org.eclipse.ui.forms.widgets.Section#setTitleBarBorderColor(org.eclipse.swt.graphics.Color)
   */
  void setTitleBarBorderColor(Color color);

  /**
   * @param color
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setTitleBarForeground(org.eclipse.swt.graphics.Color)
   */
  void setTitleBarForeground(Color color);

  /**
   * @param color
   * @see org.eclipse.ui.forms.widgets.Section#setTitleBarGradientBackground(org.eclipse.swt.graphics.Color)
   */
  void setTitleBarGradientBackground(Color color);

  /**
   * @param c
   * @see org.eclipse.ui.forms.widgets.ExpandableComposite#setToggleColor(org.eclipse.swt.graphics.Color)
   */
  void setToggleColor(Color c);

  /**
   * @param string
   * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
   */
  void setToolTipText(String string);

  /**
   * @param x
   * @param y
   * @return
   * @see org.eclipse.swt.widgets.Control#toControl(int, int)
   */
  Point toControl(int x, int y);

  /**
   * @param point
   * @return
   * @see org.eclipse.swt.widgets.Control#toControl(org.eclipse.swt.graphics.Point)
   */
  Point toControl(Point point);

  /**
   * @param x
   * @param y
   * @return
   * @see org.eclipse.swt.widgets.Control#toDisplay(int, int)
   */
  Point toDisplay(int x, int y);

  /**
   * @param point
   * @return
   * @see org.eclipse.swt.widgets.Control#toDisplay(org.eclipse.swt.graphics.Point)
   */
  Point toDisplay(Point point);

  /**
   * @param traversal
   * @return
   * @see org.eclipse.swt.widgets.Control#traverse(int)
   */
  boolean traverse(int traversal);

  /**
   * @see org.eclipse.swt.widgets.Control#update()
   */
  void update();

  /**
   * reflows the form associated with this section
   */
  void reflow();

}
