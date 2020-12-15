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

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@SuppressWarnings("ALL")
@ClassId("49409d96-e2fe-4b9e-8fde-67d1a4e5b135")
public class TestTableFieldForm extends AbstractForm {

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public MainBox.TestField getTestField() {
    return getFieldByClass(MainBox.TestField.class);
  }

  @Order(1000)
  @ClassId("c803d2d9-e45d-4126-ad29-a2d831fcfb54")
  public class MainBox extends AbstractGroupBox {
    @Order(1000)
    @ClassId("7c2d7499-d6fe-446a-9cef-3602c2d9760c")
    public class TestField extends AbstractTableField<TestField.Table> {
      @ClassId("378a6d21-6bc3-485a-a529-33c31a74d0d3")
      public class Table extends AbstractTable {
        <caret>
      }
    }
  }
}
