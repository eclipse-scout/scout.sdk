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
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;

import formdata.client.ui.forms.SimpleTableForm;
import formdata.client.ui.forms.SimpleTableForm.MainBox.TestTableField.Table;
import formdata.shared.extension.SimpleTableFormExtensionData;

@Data(SimpleTableFormExtensionData.class)
public class SimpleTableFormExtension extends AbstractTableExtension<SimpleTableForm.MainBox.TestTableField.Table> {

  public SimpleTableFormExtension(Table owner) {
    super(owner);
  }

  public class MyExtensionColumn extends AbstractBigDecimalColumn {
  }
}
