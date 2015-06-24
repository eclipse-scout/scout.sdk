package formdata.client.ui.template.formfield.replace.levels;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

import formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData;

@FormData(value = FirstLevelFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class FirstLevelForm extends AbstractForm {

  public FirstLevelForm() throws ProcessingException {
    super();
  }

  @Order(1000.0)
  public class MainBox extends AbstractGroupBox {
    public class FirstInnerBox extends AbstractMainBox {

    }
  }
}
