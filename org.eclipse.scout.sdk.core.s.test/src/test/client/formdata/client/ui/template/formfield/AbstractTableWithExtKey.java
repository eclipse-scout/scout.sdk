/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.template.formfield;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.platform.Order;

public class AbstractTableWithExtKey<KEY_TYPE> extends AbstractTable {

  public ExtKeyColumn getExtKeyColumn() {
    return getColumnSet().getColumnByClass(ExtKeyColumn.class);
  }

  @Order(100000.0)
  public class ExtKeyColumn extends AbstractColumn<KEY_TYPE> {

    @Override
    protected boolean getConfiguredDisplayable() {
      return false;
    }
  }
}
