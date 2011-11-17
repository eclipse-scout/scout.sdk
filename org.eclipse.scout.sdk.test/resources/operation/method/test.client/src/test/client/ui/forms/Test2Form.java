/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package test.client.ui.forms;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.TEXTS;

import test.client.ui.forms.Test2Form.MainBox.ListboxField;
import test.client.ui.forms.Test2Form.MainBox.SmartField;
import test.client.ui.forms.Test2Form.MainBox.StringField;
import test.client.ui.forms.Test2Form.MainBox.TableField;

@FormData
public class Test2Form extends AbstractForm {

  private Long m_test2Nr;

  public Test2Form() throws ProcessingException {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Test2");
  }

  @FormData
  public Long getTest2Nr() {
    return m_test2Nr;
  }

  @FormData
  public void setTest2Nr(Long test2Nr) {
    m_test2Nr = test2Nr;
  }

  public void startNew() throws ProcessingException {
    startInternal(new NewHandler());
  }

  public ListboxField getListboxField() {
    return getFieldByClass(ListboxField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public SmartField getSmartField() {
    return getFieldByClass(SmartField.class);
  }

  public StringField getStringField() {
    return getFieldByClass(StringField.class);
  }

  public TableField getTableField() {
    return getFieldByClass(TableField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class StringField extends AbstractStringField {
    }

    @Order(20.0)
    public class SmartField extends AbstractSmartField<Long> {
    }

    @Order(30.0)
    public class ListboxField extends AbstractListBox<Long> {
    }

    @Order(40.0)
    public class TableField extends AbstractTableField<TableField.Table> {

      @Order(10.0)
      public class Table extends AbstractTable {
      }
    }
  }

  @Order(10.0)
  public class NewHandler extends AbstractFormHandler {
  }
}
