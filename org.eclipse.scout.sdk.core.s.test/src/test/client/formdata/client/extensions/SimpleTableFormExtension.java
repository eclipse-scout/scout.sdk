/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
