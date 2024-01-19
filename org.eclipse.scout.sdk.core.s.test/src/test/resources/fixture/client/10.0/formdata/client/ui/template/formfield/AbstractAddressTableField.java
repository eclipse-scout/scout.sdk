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

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.services.process.AbstractAddressTableFieldData;

@FormData(value = AbstractAddressTableFieldData.class, sdkCommand = SdkCommand.CREATE)
public abstract class AbstractAddressTableField extends AbstractTableField<AbstractAddressTableField.Table> {

  public class Table extends AbstractTable {

    public AddressIdColumn getAddressIdColumn() {
      return getColumnSet().getColumnByClass(AddressIdColumn.class);
    }

    public StreetColumn getStreetColumn() {
      return getColumnSet().getColumnByClass(StreetColumn.class);
    }

    public PoBoxAddressColumn getPoBoxAddressColumn() {
      return getColumnSet().getColumnByClass(PoBoxAddressColumn.class);
    }

    @Order(10)
    public class AddressIdColumn extends AbstractStringColumn {
    }

    @Order(20)
    public class StreetColumn extends AbstractStringColumn {
    }

    @Order(30)
    public class PoBoxAddressColumn extends AbstractBooleanColumn {
    }
  }
}
