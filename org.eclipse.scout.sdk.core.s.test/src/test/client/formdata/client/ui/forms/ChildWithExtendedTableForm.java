/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

import formdata.shared.services.ChildWithExtendedTableFormData;

/**
 *
 */
@FormData(value = ChildWithExtendedTableFormData.class, sdkCommand = SdkCommand.CREATE)
public class ChildWithExtendedTableForm extends BaseWithExtendedTableForm {

  @Order(10.0)
  public class MainBox extends formdata.client.ui.forms.BaseWithExtendedTableForm.MainBox {
    @Order(10.0)
    public class ChildTableField extends TableInFormField {
      public class InnerTab extends formdata.client.ui.forms.BaseWithExtendedTableForm.MainBox.TableInFormField.Table {
        @Order(-20.0)
        public class Col1InChildForm extends AbstractStringColumn {

        }

        @Replace
        public class Col2InChildFormReplacing extends ColInDesktopForm {

        }
      }
    }
  }
}
