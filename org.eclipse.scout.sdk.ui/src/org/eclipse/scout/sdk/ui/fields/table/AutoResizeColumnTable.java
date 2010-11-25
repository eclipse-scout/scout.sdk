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
package org.eclipse.scout.sdk.ui.fields.table;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class AutoResizeColumnTable extends Table {

  public static final String COLUMN_WEIGHT = "columnWeight";
  private boolean m_autoResizeColumns;
  private Listener m_autoResizeListener;

  public AutoResizeColumnTable(Composite parent, int style) {
    super(parent, style);
    m_autoResizeListener = new Listener() {
      public void handleEvent(Event event) {
        getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            handleAutoSizeColumns();
          }
        });
      }
    };
    setAutoResizeColumns(true);
  }

  @Override
  protected void checkSubclass() {
  }

  public void setAutoResizeColumns(boolean autoResizeColumns) {
    m_autoResizeColumns = autoResizeColumns;
    if (autoResizeColumns) {
      addListener(SWT.Resize, m_autoResizeListener);
    }
    else {
      removeListener(SWT.Resize, m_autoResizeListener);
    }
  }

  public boolean isAutoResizeColumns() {
    return m_autoResizeColumns;
  }

  /**
   * Distributes the table width to the columns considered column weights of
   * model. Empty space will be distributed weighted.
   */
  protected void handleAutoSizeColumns() {
    if (!isDisposed()) {
      int totalWidth = getClientArea().width;
      if (getVerticalBar() != null && getVerticalBar().getVisible()) {
        // totalWidth -= getSwtField().getVerticalBar().getSize().x;
      }
      int totalWeight = 0;
      HashMap<TableColumn, Integer> columnWeights = new HashMap<TableColumn, Integer>();
      for (TableColumn col : getColumns()) {
        if (col == null || col.isDisposed()) {
          continue;
        }
        Object colWeightData = col.getData(COLUMN_WEIGHT);
        if (colWeightData == null) {
          ScoutSdkUi.logWarning("auto resizable table, contains columns with no weights setted!");
          continue;
        }
        Integer colWeight = (Integer) colWeightData;
        columnWeights.put(col, colWeight);
        totalWeight += colWeight;
      }
      double factor = (double) totalWidth / (double) totalWeight;
      if (factor < 1) {
        factor = 1;
      }

      int i = 0;
      for (Entry<TableColumn, Integer> entry : columnWeights.entrySet()) {
        if (i < columnWeights.size() - 1) {
          int width = (int) (factor * entry.getValue().intValue());
          entry.getKey().setWidth(width);
          totalWidth -= width;
        }
        else {
          entry.getKey().setWidth(totalWidth);
        }
      }

    }
  }

}
