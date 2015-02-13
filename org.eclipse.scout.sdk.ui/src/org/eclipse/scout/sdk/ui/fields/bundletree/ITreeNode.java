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

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;

/**
 * <h3>ITreeNode</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 30.01.2010
 */
public interface ITreeNode {

  /**
   * @return
   */
  Object getData();

  /**
   * a node type id used to find a set of nodes in the tree.
   *
   * @return
   */
  String getType();

  /**
   * the display text for the current node.
   *
   * @param text
   */
  void setText(String text);

  /**
   * @return
   */
  String getText();

  /**
   * @return
   */
  ImageDescriptor getImage();

  /**
   * @param parent
   */
  void setParent(ITreeNode parent);

  /**
   * @return
   */
  ITreeNode getParent();

  /**
   * @param childNode
   */
  void addChild(ITreeNode childNode);

  /**
   * @param childNode
   * @return
   */
  boolean removeChild(ITreeNode childNode);

  /**
   * @return
   */
  List<ITreeNode> getChildren();

  /**
   * @param filter
   * @return
   */
  List<ITreeNode> getChildren(ITreeNodeFilter filter);

  /**
   * @param visible
   */
  void setVisible(boolean visible);

  /**
   * @return
   */
  boolean isVisible();

  /**
   * @param bold
   */
  void setBold(boolean bold);

  /**
   * @return
   */
  boolean isBold();

  /**
   * @return
   */
  boolean isCheckable();

  boolean isEnabled();

  void setEnabled(boolean enabled);

  /**
   * @param checkable
   */
  void setCheckable(boolean checkable);

  /**
   * @return the foreground color of this node
   */
  Color getForeground();

  /**
   * @param foreground
   *          the new foreground
   */
  void setForeground(Color foreground);

  /**
   * @return the background color of this node
   */
  Color getBackground();

  /**
   * @param background
   *          the new background color of this node
   */
  void setBackground(Color background);

  /**
   * @return the order of this node
   */
  int getOrderNr();

  /**
   * @param orderNr
   *          the new order of this node.
   */
  void setOrderNr(int orderNr);

  /**
   * removes all children from this tree node.
   */
  void clearChildren();

}
