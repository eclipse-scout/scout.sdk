/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.client.ui.forms.replace;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.platform.Replace;

import formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData;

@FormData(value = TableFieldWithIgnoredColumnsDefaultExFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldWithIgnoredColumnsDefaultExForm extends TableFieldWithIgnoredColumnsBaseForm {

  @Replace
  public class TableDefaultExField extends TableFieldWithIgnoredColumnsBaseForm.MainBox.TableBaseField {

    public TableDefaultExField(TableFieldWithIgnoredColumnsBaseForm.MainBox container) {
      container.super();
    }

    public class DefaultExTable extends Table {

      public DefaultDefaultColumn getDefaultDefaultColumn() {
        return getColumnSet().getColumnByClass(DefaultDefaultColumn.class);
      }

      public CreateDefaultColumn getCreateDefaultColumn() {
        return getColumnSet().getColumnByClass(CreateDefaultColumn.class);
      }

      public IgnoreDefaultColumn getIgnoreDefaultColumn() {
        return getColumnSet().getColumnByClass(IgnoreDefaultColumn.class);
      }

      @Replace
      public class DefaultDefaultColumn extends DefaultColumn {
      }

      @Replace
      public class CreateDefaultColumn extends CreateColumn {
      }

      @Replace
      public class IgnoreDefaultColumn extends IgnoreColumn {
      }
    }
  }
}
