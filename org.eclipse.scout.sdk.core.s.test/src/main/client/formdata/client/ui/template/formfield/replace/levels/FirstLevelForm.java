package formdata.client.ui.template.formfield.replace.levels;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

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
