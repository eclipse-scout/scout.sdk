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
package formdata.client.ui.template.formfield.replace;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.ui.template.formfield.AbstractBeanTableField;
import formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData;

/**
 * @since 3.10.0-M5
 */
@FormData(value = AbstractTemplateForReplaceBoxData.class, sdkCommand = SdkCommand.CREATE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractTemplateForReplaceBox extends AbstractGroupBox {

  public TemplateStringField getTemplateStringField() {
    return getFieldByClass(TemplateStringField.class);
  }

  public TemplateBox getTemplateBox() {
    return getFieldByClass(TemplateBox.class);
  }

  public TemplateTableField getTemplateTableField() {
    return getFieldByClass(TemplateTableField.class);
  }

  @Order(10)
  public class TemplateStringField extends AbstractStringField {
  }

  @Order(20)
  public class TemplateBox extends AbstractGroupBox {

    @Order(10)
    public class TemplateBoxStringField extends AbstractStringField {
    }
  }

  @Order(30)
  public class TemplateTableField extends AbstractBeanTableField<TemplateTableField.Table> {

    public class Table extends AbstractTable {

      public FirstColumn getFirstColumn() {
        return getColumnSet().getColumnByClass(FirstColumn.class);
      }

      public SecondColumn getSecondColumn() {
        return getColumnSet().getColumnByClass(SecondColumn.class);
      }

      public class FirstColumn extends AbstractStringColumn {
      }

      public class SecondColumn extends AbstractStringColumn {
      }
    }
  }
}
