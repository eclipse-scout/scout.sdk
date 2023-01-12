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

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.ui.template.formfield.AbstractLoremTableField.Table;

/**
 *
 */
public abstract class AbstractLoremTableField extends AbstractTableField<Table> {
  public class Table extends AbstractLoremTable {

    public XxColumn getXxColumn() {
      return getColumnSet().getColumnByClass(XxColumn.class);
    }

    @Order(1000.0)
    public class XxColumn extends AbstractStringColumn {
    }

  }
}
