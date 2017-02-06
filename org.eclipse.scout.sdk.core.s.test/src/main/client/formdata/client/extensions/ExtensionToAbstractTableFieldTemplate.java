/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
