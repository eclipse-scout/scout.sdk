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
