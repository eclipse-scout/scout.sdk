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

import formdata.shared.services.process.AbstractPersonTableFieldData;

@FormData(value = AbstractPersonTableFieldData.class, sdkCommand = SdkCommand.CREATE)
public abstract class AbstractPersonTableField extends AbstractTableField<AbstractPersonTableField.Table> {

  public class Table extends AbstractTable {

    public PersonIdColumn getAddressIdColumn() {
      return getColumnSet().getColumnByClass(PersonIdColumn.class);
    }

    public NameColumn getStreetColumn() {
      return getColumnSet().getColumnByClass(NameColumn.class);
    }

    public FemaleColumn getPoBoxAddressColumn() {
      return getColumnSet().getColumnByClass(FemaleColumn.class);
    }

    public BooleanColumn getBooleanColumn() {
      return getColumnSet().getColumnByClass(BooleanColumn.class);
    }

    public AssertColumn getAssertColumn() {
      return getColumnSet().getColumnByClass(AssertColumn.class);
    }

    public SwitchColumn getSwitchColumn() {
      return getColumnSet().getColumnByClass(SwitchColumn.class);
    }

    @Order(10)
    public class PersonIdColumn extends AbstractStringColumn {
    }

    @Order(20)
    public class NameColumn extends AbstractStringColumn {
    }

    @Order(30)
    public class FemaleColumn extends AbstractBooleanColumn {
    }

    @Order(40)
    public class BooleanColumn extends AbstractBooleanColumn {
    }

    @Order(50)
    public class AssertColumn extends AbstractBooleanColumn {
    }

    @Order(60)
    public class SwitchColumn extends AbstractBooleanColumn {
    }
  }
}
