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

import formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData;

@FormData(value = TableFieldWithIgnoredColumnsDefaultCreateExFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldWithIgnoredColumnsDefaultCreateExForm extends TableFieldWithIgnoredColumnsBaseForm {

  @Replace
  public class TableDefaultCreateExField extends TableFieldWithIgnoredColumnsBaseForm.MainBox.TableBaseField {

    public TableDefaultCreateExField(TableFieldWithIgnoredColumnsBaseForm.MainBox container) {
      container.super();
    }

    public class DefaultCreateExTable extends Table {

      public DefaultDefaultCreateColumn getDefaultDefaultCreateColumn() {
        return getColumnSet().getColumnByClass(DefaultDefaultCreateColumn.class);
      }

      public CreateDefaultCreateColumn getCreateDefaultCreateColumn() {
        return getColumnSet().getColumnByClass(CreateDefaultCreateColumn.class);
      }

      public IgnoreDefaultCreateColumn getIgnoreDefaultCreateColumn() {
        return getColumnSet().getColumnByClass(IgnoreDefaultCreateColumn.class);
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class DefaultDefaultCreateColumn extends DefaultColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class CreateDefaultCreateColumn extends CreateColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class IgnoreDefaultCreateColumn extends IgnoreColumn {
      }
    }
  }
}
