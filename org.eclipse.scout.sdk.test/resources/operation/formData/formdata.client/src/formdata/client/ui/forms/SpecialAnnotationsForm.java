package formdata.client.ui.forms;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;

import formdata.client.ui.forms.SpecialAnnotationsForm.MainBox.CancelButton;
import formdata.client.ui.forms.SpecialAnnotationsForm.MainBox.OkButton;
import formdata.client.ui.forms.SpecialAnnotationsForm.MainBox.WrappedFormFormField;
import formdata.shared.services.process.SimpleFormData;
import formdata.shared.services.process.SpecialAnnotationsFormData;

@FormData(value = SpecialAnnotationsFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class SpecialAnnotationsForm extends AbstractForm {

  public SpecialAnnotationsForm() throws ProcessingException {
    super();
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public void startModify() throws ProcessingException {
    startInternal(new SpecialAnnotationsForm.ModifyHandler());
  }

  public void startNew() throws ProcessingException {
    startInternal(new SpecialAnnotationsForm.NewHandler());
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public WrappedFormFormField getWrappedFormFormField() {
    return getFieldByClass(WrappedFormFormField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    @FormData(value = SimpleFormData.class, sdkCommand = SdkCommand.USE)
    public class WrappedFormFormField extends AbstractWrappedFormField<SimpleForm> {
    }

    @Order(20.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(30.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {
  }

  public class NewHandler extends AbstractFormHandler {
  }
}
