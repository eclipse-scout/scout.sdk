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

import formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData;

@FormData(value = TableFieldWithIgnoredColumnsCreateExFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldWithIgnoredColumnsCreateExForm extends TableFieldWithIgnoredColumnsBaseForm {

  @Replace
  public class TableCreateExField extends TableFieldWithIgnoredColumnsBaseForm.MainBox.TableBaseField {

    public TableCreateExField(TableFieldWithIgnoredColumnsBaseForm.MainBox container) {
      container.super();
    }

    public class CreateExTable extends Table {

      public DefaultCreateColumn getDefaultCreateColumn() {
        return getColumnSet().getColumnByClass(DefaultCreateColumn.class);
      }

      public CreateCreateColumn getCreateCreateColumn() {
        return getColumnSet().getColumnByClass(CreateCreateColumn.class);
      }

      public IgnoreCreateColumn getIgnoreCreateColumn() {
        return getColumnSet().getColumnByClass(IgnoreCreateColumn.class);
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class DefaultCreateColumn extends DefaultColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class CreateCreateColumn extends CreateColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class IgnoreCreateColumn extends IgnoreColumn {
      }
    }
  }
}
