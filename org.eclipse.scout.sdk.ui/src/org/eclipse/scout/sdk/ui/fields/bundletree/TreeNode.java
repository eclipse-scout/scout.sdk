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
 * <h3>TreeNode</h3>
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
  public List<ITreeNode> getChildren(ITreeNodeFilter filter) {
    List<ITreeNode> children = new ArrayList<>();
    for (ITreeNode child : getChildren()) {
      if (filter.accept(child)) {
        children.add(child);
      }
    }
    return children;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_background == null) ? 0 : m_background.hashCode());
    result = prime * result + (m_bold ? 1231 : 1237);
    result = prime * result + (m_checkable ? 1231 : 1237);
    result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
    result = prime * result + (m_enabled ? 1231 : 1237);
    result = prime * result + ((m_foreground == null) ? 0 : m_foreground.hashCode());
    result = prime * result + ((m_image == null) ? 0 : m_image.hashCode());
    result = prime * result + m_orderNr;
    result = prime * result + ((m_parent == null) ? 0 : m_parent.hashCode());
    result = prime * result + ((m_text == null) ? 0 : m_text.hashCode());
    result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
    result = prime * result + (m_visible ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TreeNode)) {
      return false;
    }
    TreeNode other = (TreeNode) obj;
    if (m_background == null) {
      if (other.m_background != null) {
        return false;
      }
    }
    else if (!m_background.equals(other.m_background)) {
      return false;
    }
    if (m_bold != other.m_bold) {
      return false;
    }
    if (m_checkable != other.m_checkable) {
      return false;
    }
    if (m_data == null) {
      if (other.m_data != null) {
        return false;
      }
    }
    else if (!m_data.equals(other.m_data)) {
      return false;
    }
    if (m_enabled != other.m_enabled) {
      return false;
    }
    if (m_foreground == null) {
      if (other.m_foreground != null) {
        return false;
      }
    }
    else if (!m_foreground.equals(other.m_foreground)) {
      return false;
    }
    if (m_image == null) {
      if (other.m_image != null) {
        return false;
      }
    }
    else if (!m_image.equals(other.m_image)) {
      return false;
    }
    if (m_orderNr != other.m_orderNr) {
      return false;
    }
    if (m_parent == null) {
      if (other.m_parent != null) {
        return false;
      }
    }
    else if (!m_parent.equals(other.m_parent)) {
      return false;
    }
    if (m_text == null) {
      if (other.m_text != null) {
        return false;
      }
    }
    else if (!m_text.equals(other.m_text)) {
      return false;
    }
    if (m_type == null) {
      if (other.m_type != null) {
        return false;
      }
    }
    else if (!m_type.equals(other.m_type)) {
      return false;
    }
    if (m_visible != other.m_visible) {
      return false;
    }
    return true;
  }
}
