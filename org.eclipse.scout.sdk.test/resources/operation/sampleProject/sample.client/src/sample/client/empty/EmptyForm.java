package sample.client.empty;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.shared.TEXTS;

import sample.client.empty.EmptyForm.MainBox.CancelButton;
import sample.client.empty.EmptyForm.MainBox.OkButton;
import sample.shared.empty.EmptyFormData;

@FormData(value = EmptyFormData.class, sdkCommand = SdkCommand.CREATE)
public class EmptyForm extends AbstractForm {

  public EmptyForm() throws ProcessingException {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Empty");
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(20.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }

}
