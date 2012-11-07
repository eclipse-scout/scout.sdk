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
package org.eclipse.scout.sdk.ui.internal.fields.table;

import java.util.EventListener;

import org.eclipse.jface.viewers.ColumnViewer;

/**
 *
 */
public interface IColumnProvider {

  /**
   * @return
   */
  int getColumnCount();

  /**
   * @return
   */
  ColumnViewer getViewer();

  /**
   * @param element
   * @param columnIndex
   * @return
   */
  String getCellText(Object element, int columnIndex);

  /**
   * @param node
   * @return
   */
  Object[] getChildren(Object node);

  /**
   * @return
   */
  int[] getColumnWidths();

  /**
   * @return
   */
  int getXOffset();

  /**
   *
   */
  void dispose();

  void addLayoutUpdateListener(LayoutUpdateListener listener);

  void removeLayoutUpdateListener(LayoutUpdateListener listener);

  public static interface LayoutUpdateListener extends EventListener {
    void updateLayout();
  }

  /**
   *
   */
  void expandAll();

}
