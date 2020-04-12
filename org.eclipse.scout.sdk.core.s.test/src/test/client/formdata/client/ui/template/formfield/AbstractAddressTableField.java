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
