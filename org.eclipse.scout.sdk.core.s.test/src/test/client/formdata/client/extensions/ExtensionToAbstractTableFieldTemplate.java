/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.extensions;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.AbstractTableFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

import formdata.client.ui.template.formfield.AbstractAddressTableField;
import formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData;

@Data(ExtensionToAbstractTableFieldTemplateData.class)
public class ExtensionToAbstractTableFieldTemplate extends AbstractTableFieldExtension<AbstractAddressTableField.Table, AbstractAddressTableField> {
  public ExtensionToAbstractTableFieldTemplate(AbstractAddressTableField owner) {
    super(owner);
  }

  public class CustomAbstractAdvisorTableFieldTableExtension extends AbstractTableExtension<AbstractAddressTableField.Table> {

    public CustomAbstractAdvisorTableFieldTableExtension(AbstractAddressTableField.Table owner) {
      super(owner);
    }

    public AddedColumn getAddedColumn() {
      return getOwner().getColumnSet().getColumnByClass(AddedColumn.class);
    }

    @Order(1000)
    @ClassId("99e71e0a-57de-4415-bc7f-7d9d9ed0ac4c")
    public class AddedColumn extends AbstractBooleanColumn {
    }
  }
}
