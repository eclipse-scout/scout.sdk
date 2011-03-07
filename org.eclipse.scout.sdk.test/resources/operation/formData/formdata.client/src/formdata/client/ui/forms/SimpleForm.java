package formdata.client.ui.forms;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

import formdata.client.ui.forms.SimpleForm.MainBox.CancelButton;
import formdata.client.ui.forms.SimpleForm.MainBox.OkButton;
import formdata.client.ui.forms.SimpleForm.MainBox.SampleComposerField;
import formdata.client.ui.forms.SimpleForm.MainBox.SampleDoubleField;
import formdata.client.ui.forms.SimpleForm.MainBox.SampleSmartField;
import formdata.client.ui.forms.SimpleForm.MainBox.SampleStringField;
import formdata.shared.services.process.SimpleFormData;

@FormData(value = SimpleFormData.class, sdkCommand = SdkCommand.CREATE)
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

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public SampleComposerField getSampleComposerField() {
    return getFieldByClass(SampleComposerField.class);
  }

  public SampleDoubleField getSampleDoubleField() {
    return getFieldByClass(SampleDoubleField.class);
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
    public class SampleDoubleField extends AbstractDoubleField {
    }

    @Order(30.0)
    public class SampleSmartField extends AbstractSmartField<Long> {
    }

    @Order(40.0)
    public class SampleComposerField extends AbstractComposerField {
    }

    @Order(50.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(60.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }
}
