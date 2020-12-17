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
package formdata.client.ui.forms.replace;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

import formdata.client.ui.template.formfield.AbstractPersonTableField;
import formdata.shared.services.process.replace.TableFieldExFormData;

/**
 *
 */
@FormData(value = TableFieldExFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldExForm extends TableFieldBaseForm {

  @Replace
  public class TableExtendedField extends TableFieldBaseForm.MainBox.TableField {

    public TableExtendedField(TableFieldBaseForm.MainBox container) {
      container.super();
    }

    public class TableEx extends Table {

      @Order(30)
      @Replace
      public class FirstExtendedColumn extends FirstColumn {

      }

      @Order(40)
      public class BooleanColumn extends AbstractBooleanColumn {

      }
    }
  }

  @Replace
  public class EmptyTableExtendedField extends TableFieldBaseForm.MainBox.EmptyTableField {

    public EmptyTableExtendedField(TableFieldBaseForm.MainBox container) {
      container.super();
    }

    public class TableEx extends Table {

      @Order(10)
      public class SingleColumn extends AbstractStringColumn {

      }
    }
  }

  @Replace
  public class NoTableExtendedField extends TableFieldBaseForm.MainBox.NoTableField {

    public NoTableExtendedField(TableFieldBaseForm.MainBox container) {
      container.super();
    }

    public class Table extends AbstractTable {

      @Order(10)
      public class NewColumn extends AbstractStringColumn {

      }
    }
  }

  @Replace
  public class ExtendedAddressField extends TableFieldBaseForm.MainBox.AddressTableField {

    public ExtendedAddressField(TableFieldBaseForm.MainBox container) {
      container.super();
    }

    public class Table extends TableFieldBaseForm.MainBox.AddressTableField.Table {

      public StateColumn getStateColumn() {
        return getColumnSet().getColumnByClass(StateColumn.class);
      }

      @Order(40)
      public class StateColumn extends AbstractStringColumn {

      }
    }
  }

  @Replace
  public class ExtendedPersonTableField extends TableFieldBaseForm.MainBox.PersonTableField {

    public ExtendedPersonTableField(TableFieldBaseForm.MainBox container) {
      container.super();
    }

    public class Table extends AbstractPersonTableField.Table {

      @Order(40)
      public class LastName extends AbstractStringColumn {
      }
    }
  }
}
