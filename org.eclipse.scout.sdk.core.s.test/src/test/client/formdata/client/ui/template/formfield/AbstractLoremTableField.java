/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
