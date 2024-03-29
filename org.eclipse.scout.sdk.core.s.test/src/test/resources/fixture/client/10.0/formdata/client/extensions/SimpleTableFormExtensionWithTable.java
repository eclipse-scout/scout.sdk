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
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.ui.forms.SimpleTableForm;
import formdata.client.ui.forms.SimpleTableForm.MainBox.TestTableField;
import formdata.shared.extension.SimpleTableFormExtensionWithTableData;

@Data(SimpleTableFormExtensionWithTableData.class)
public class SimpleTableFormExtensionWithTable extends AbstractFormExtension<SimpleTableForm> {

  public SimpleTableFormExtensionWithTable(SimpleTableForm ownerForm) {
    super(ownerForm);
  }

  public class TestTableFieldExtension extends AbstractTableExtension<TestTableField.Table> {
    public TestTableFieldExtension(TestTableField.Table owner) {
      super(owner);
    }

    @Order(1000.0)
    public class ContributedColumn extends AbstractBigDecimalColumn {
    }
  }
}
