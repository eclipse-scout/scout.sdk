package formdata.client.ui.forms;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

import formdata.client.IFormDataInterface01;
import formdata.client.ui.forms.SimpleForm.MainBox.CancelButton;
import formdata.client.ui.forms.SimpleForm.MainBox.DateField;
import formdata.client.ui.forms.SimpleForm.MainBox.DoubleField;
import formdata.client.ui.forms.SimpleForm.MainBox.MultiTypeArgsBox;
import formdata.client.ui.forms.SimpleForm.MainBox.OkButton;
import formdata.client.ui.forms.SimpleForm.MainBox.SampleComposerField;
import formdata.client.ui.forms.SimpleForm.MainBox.SampleDateField;
import formdata.client.ui.forms.SimpleForm.MainBox.SampleSmartField;
import formdata.client.ui.forms.SimpleForm.MainBox.SampleStringField;
import formdata.client.ui.template.formfield.AbstractGroupBoxWithMultipleTypeArgs;
import formdata.shared.IFormDataInterface02;
import formdata.shared.IFormDataInterface03;
import formdata.shared.TestRunnable;
import formdata.shared.services.process.SimpleFormData;

@FormData(value = SimpleFormData.class, sdkCommand = SdkCommand.CREATE, interfaces = {IFormDataInterface01.class, IFormDataInterface02.class, IFormDataInterface03.class})
public class SimpleForm extends AbstractForm {

  private Long simpleNr;

  public SimpleForm() throws ProcessingException {
    super();
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  @FormData
  public Long getSimpleNr() {
    return simpleNr;
  }

  @FormData
  public void setSimpleNr(Long simpleNr) {
    this.simpleNr = simpleNr;
  }

  public DateField getDateField() {
    return getFieldByClass(DateField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public MultiTypeArgsBox getMultiTypeArgsBox() {
    return getFieldByClass(MultiTypeArgsBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public SampleComposerField getSampleComposerField() {
    return getFieldByClass(SampleComposerField.class);
  }

  public SampleDateField getSampleDateField() {
    return getFieldByClass(SampleDateField.class);
  }

  public DoubleField getDoubleField() {
    return getFieldByClass(DoubleField.class);
  }

  public SampleSmartField getSampleSmartField() {
    return getFieldByClass(SampleSmartField.class);
  }

  public SampleStringField getSampleStringField() {
    return getFieldByClass(SampleStringField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class SampleStringField extends AbstractStringField {
    }

    @Order(20.0)
    public class DoubleField extends AbstractDoubleField {

      @Override
      protected Double getConfiguredMaxValue() {
        return -Double.MAX_VALUE;
      }

      @Override
      protected Double getConfiguredMinValue() {
        return 0.0;
      }
    }

    @Order(30.0)
    public class SampleSmartField extends AbstractSmartField<Long> {
    }

    @Order(40.0)
    public class SampleComposerField extends AbstractComposerField {
    }

    /**
     * Field that uses java.util.Date (simple name 'Date').
     */
    @Order(50.0)
    public class SampleDateField extends AbstractDateField {
    }

    /**
     * Field to ensure the simple name 'Date' is already used in the form data.
     */
    @Order(60.0)
    public class DateField extends AbstractIntegerField {
    }

    @Order(70.0)
    public class MultiTypeArgsBox extends AbstractGroupBoxWithMultipleTypeArgs<Long, TestRunnable> {
    }

    @Order(80.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(90.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }
}
