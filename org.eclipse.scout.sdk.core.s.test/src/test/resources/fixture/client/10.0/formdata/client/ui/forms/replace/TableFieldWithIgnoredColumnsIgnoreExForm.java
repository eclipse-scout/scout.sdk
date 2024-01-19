/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.forms.replace;

import org.eclipse.scout.rt.client.dto.ColumnData;
import org.eclipse.scout.rt.client.dto.ColumnData.SdkColumnCommand;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.platform.Replace;

import formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData;

@FormData(value = TableFieldWithIgnoredColumnsIgnoreExFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldWithIgnoredColumnsIgnoreExForm extends TableFieldWithIgnoredColumnsBaseForm {

  @Replace
  public class TableIgnoreExField extends TableFieldWithIgnoredColumnsBaseForm.MainBox.TableBaseField {

    public TableIgnoreExField(TableFieldWithIgnoredColumnsBaseForm.MainBox container) {
      container.super();
    }

    public class IgnoreExTable extends Table {

      public DefaultIgnoreColumn getDefaultIgnoreColumn() {
        return getColumnSet().getColumnByClass(DefaultIgnoreColumn.class);
      }

      public CreateIgnoreColumn getCreateIgnoreColumn() {
        return getColumnSet().getColumnByClass(CreateIgnoreColumn.class);
      }

      public IgnoreIgnoreColumn getIgnoreIgnoreColumn() {
        return getColumnSet().getColumnByClass(IgnoreIgnoreColumn.class);
      }

      @Replace
      @ColumnData(SdkColumnCommand.IGNORE)
      public class DefaultIgnoreColumn extends DefaultColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.IGNORE)
      public class CreateIgnoreColumn extends CreateColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.IGNORE)
      public class IgnoreIgnoreColumn extends IgnoreColumn {
      }
    }
  }
}
