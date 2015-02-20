package jdt.test.client;

import jdt.test.client.TestForm.MainBox.DateField;
import jdt.test.client.TestForm.MainBox.DoubleField;
import jdt.test.client.TestForm.MainBox.GroupBox;
import jdt.test.client.TestForm.MainBox.HtmlField;
import jdt.test.client.TestForm.MainBox.IntegerField;
import jdt.test.client.TestForm.MainBox.LabelField;
import jdt.test.client.TestForm.MainBox.ListBoxField;
import jdt.test.client.TestForm.MainBox.RadioButtonGroup;
import jdt.test.client.TestForm.MainBox.SmartField;
import jdt.test.client.TestForm.MainBox.StringField;
import jdt.test.client.TestForm.MainBox.TableField;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

public class TestForm extends AbstractForm {

  public TestForm() throws ProcessingException {
    super();
  }

  public DateField getDateField() {
    return getFieldByClass(DateField.class);
  }

  public DoubleField getDoubleField() {
    return getFieldByClass(DoubleField.class);
  }

  public GroupBox getGroupBox() {
    return getFieldByClass(GroupBox.class);
  }

  public HtmlField getHtmlField() {
    return getFieldByClass(HtmlField.class);
  }

  public IntegerField getIntegerField() {
    return getFieldByClass(IntegerField.class);
  }

  public LabelField getLabelField() {
    return getFieldByClass(LabelField.class);
  }

  public ListBoxField getListBoxField() {
    return getFieldByClass(ListBoxField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public RadioButtonGroup getRadioButtonGroup() {
    return getFieldByClass(RadioButtonGroup.class);
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
    public class DateField extends AbstractDateField {
    }

    @Order(30.0)
    public class DoubleField extends AbstractDoubleField {
    }

    @Order(40.0)
    public class GroupBox extends AbstractGroupBox {
    }

    @Order(50.0)
    public class HtmlField extends AbstractHtmlField {
    }

    @Order(60.0)
    public class IntegerField extends AbstractIntegerField {
    }

    @Order(70.0)
    public class LabelField extends AbstractLabelField {
    }

    @Order(80.0)
    public class ListBoxField extends AbstractListBox<Long> {
    }

    @Order(90.0)
    public class RadioButtonGroup extends AbstractRadioButtonGroup<Long> {
    }

    @Order(100.0)
    public class SmartField extends AbstractSmartField<Long> {
    }

    @Order(110.0)
    public class TableField extends AbstractTableField<TableField.Table> {

      @Order(10.0)
      public class Table extends AbstractTable {
      }
    }

  }
}
