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

import org.eclipse.swt.graphics.Image;

/**
 * <h3>ITreeNode</h3> ...
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
  int getType();

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
  Image getImage();

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
  ITreeNode[] getChildren(ITreeNodeFilter filter);

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
   * @return
   */
  long getOrderNr();

  /**
   * @param checkable
   */
  void setCheckable(boolean checkable);

}
