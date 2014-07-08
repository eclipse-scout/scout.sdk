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
package org.eclipse.scout.sdk.ui.fields.bundletree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;

/**
 * <h3>TreeNode</h3> ...
 *
 * @author Andreas Hoegger
 * @since 1.0.8 30.01.2010
 */
public class TreeNode implements ITreeNode {

  private final String m_type;
  private Object m_data;
  private String m_text;
  private boolean m_visible = true;
  private boolean m_checkable = false;
  private boolean m_enabled = true;
  private boolean m_bold = false;
  private ImageDescriptor m_image;
  private int m_orderNr = 0;
  private Color m_foreground;
  private Color m_background;
  private ITreeNode m_parent;

  private final List<ITreeNode> m_children = new ArrayList<ITreeNode>();

  public TreeNode(String type, String text) {
    this(type, text, null);
  }

  public TreeNode(String type, String text, Object data) {
    m_type = type;
    m_data = data;
    m_text = text;
  }

  public TreeNode(ITreeNode node) {
    m_data = node.getData();
    m_bold = node.isBold();
    m_checkable = node.isCheckable();
    m_image = node.getImage();
    m_text = node.getText();
    m_type = node.getType();
    m_visible = node.isVisible();
  }

  /**
   * @return the type
   */
  @Override
  public String getType() {
    return m_type;
  }

  /**
   * @param text
   *          the text to set
   */
  @Override
  public void setText(String text) {
    m_text = text;
  }

  /**
   * @return the text
   */
  @Override
  public String getText() {
    return m_text;
  }

  public void setData(Object data) {
    m_data = data;
  }

  /**
   * @return the data
   */
  @Override
  public Object getData() {
    return m_data;
  }

  @Override
  public void setParent(ITreeNode parent) {
    m_parent = parent;
  }

  @Override
  public ITreeNode getParent() {
    return m_parent;
  }

  @Override
  public void addChild(ITreeNode childNode) {
    m_children.add(childNode);
  }

  @Override
  public boolean removeChild(ITreeNode childNode) {
    return m_children.remove(childNode);
  }

  @Override
  public void clearChildren() {
    m_children.clear();
  }

  @Override
  public List<ITreeNode> getChildren() {
    return m_children;
  }

  @Override
  public ITreeNode[] getChildren(ITreeNodeFilter filter) {
    ArrayList<ITreeNode> children = new ArrayList<ITreeNode>();
    for (ITreeNode child : getChildren()) {
      if (filter.accept(child)) {
        children.add(child);
      }
    }
    return children.toArray(new ITreeNode[children.size()]);
  }

  /**
   * @param visible
   *          the visible to set
   */
  @Override
  public void setVisible(boolean visible) {
    m_visible = visible;
  }

  /**
   * @return the visible
   */
  @Override
  public boolean isVisible() {
    return m_visible;
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }

  /**
   * @param bold
   *          the bold to set
   */
  @Override
  public void setBold(boolean bold) {
    m_bold = bold;
  }

  /**
   * @return the bold
   */
  @Override
  public boolean isBold() {
    return m_bold;
  }

  /**
   * @param image
   *          the image to set
   */
  public void setImage(ImageDescriptor image) {
    m_image = image;
  }

  /**
   * @return the image
   */
  @Override
  public ImageDescriptor getImage() {
    return m_image;
  }

  /**
   * @param checkable
   *          the checkable to set
   */
  @Override
  public void setCheckable(boolean checkable) {
    m_checkable = checkable;
  }

  /**
   * @return the checkable
   */
  @Override
  public boolean isCheckable() {
    return m_checkable;
  }

  @Override
  public void setOrderNr(int orderNr) {
    m_orderNr = orderNr;
  }

  @Override
  public int getOrderNr() {
    return m_orderNr;
  }

  @Override
  public Color getForeground() {
    return m_foreground;
  }

  @Override
  public void setForeground(Color foreground) {
    m_foreground = foreground;
  }

  @Override
  public Color getBackground() {
    return m_background;
  }

  @Override
  public void setBackground(Color background) {
    m_background = background;
  }

}
