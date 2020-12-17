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
package formdata.client.ui.forms;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.text.TEXTS;

import formdata.client.ui.forms.TableFieldForm.MainBox.CompanyTableField;
import formdata.client.ui.forms.TableFieldForm.MainBox.PersonTableField;
import formdata.client.ui.forms.TableFieldForm.MainBox.TableFieldWithExternalTableField;
import formdata.client.ui.template.formfield.AbstractCompanyTableField;
import formdata.client.ui.template.formfield.AbstractLoremTableField;
import formdata.client.ui.template.formfield.AbstractTableWithExtKey;
import formdata.shared.services.process.TableFieldFormData;

@FormData(value = TableFieldFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class TableFieldForm extends AbstractForm {

  public TableFieldWithExternalTableField getTableFieldWithExternalTableField() {
    return getFieldByClass(TableFieldWithExternalTableField.class);
  }

  public CompanyTableField getCompanyTableField() {
    return getFieldByClass(CompanyTableField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public PersonTableField getPersonTableField() {
    return getFieldByClass(PersonTableField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class PersonTableField extends AbstractTableField<PersonTableField.Table> {

      @Order(10.0)
      public class Table extends AbstractTable {

        public AnObjectColumn getAnObjectColumn() {
          return getColumnSet().getColumnByClass(AnObjectColumn.class);
        }

        public NameColumn getNameColumn() {
          return getColumnSet().getColumnByClass(NameColumn.class);
        }

        public PersonNrColumn getPersonNrColumn() {
          return getColumnSet().getColumnByClass(PersonNrColumn.class);
        }

        public SmartLongColumn getSmartLongColumn() {
          return getColumnSet().getColumnByClass(SmartLongColumn.class);
        }

        public CustomColumn getCustomColumn() {
          return getColumnSet().getColumnByClass(CustomColumn.class);
        }

        @Order(10.0)
        public class PersonNrColumn extends AbstractLongColumn {
        }

        @Order(20.0)
        public class NameColumn extends AbstractStringColumn {
        }

        @Order(30.0)
        public class AnObjectColumn extends AbstractObjectColumn {
        }

        @Order(40.0)
        public class SmartLongColumn extends AbstractSmartColumn<Long> {
        }

        @Order(50.0)
        public class CustomColumn extends AbstractColumn<Set<Map<String, Integer>>> {
        }
      }
    }

    @Order(20.0)
    public class CompanyTableField extends AbstractCompanyTableField {

    }

    @Order(400.0)
    public class ConcreteTableField extends AbstractTableField<ConcreteTableField.Table> {

      @Order(10.0)
      public class Table extends AbstractTableWithExtKey<Integer> {

        public NameColumn getNameColumn() {
          return getColumnSet().getColumnByClass(NameColumn.class);
        }

        @Order(20.0)
        public class NameColumn extends AbstractStringColumn {

          @Override
          protected String getConfiguredHeaderText() {
            return TEXTS.get("Name");
          }
        }
      }
    }

    @Order(2000.0)
    public class TableFieldWithExternalTableField extends AbstractLoremTableField {
    }
  }
}
