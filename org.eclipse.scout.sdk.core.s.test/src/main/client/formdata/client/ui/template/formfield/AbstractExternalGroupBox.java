package formdata.client.ui.template.formfield;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.services.process.AbstractExternalGroupBoxData;

@FormData(value = AbstractExternalGroupBoxData.class, sdkCommand = SdkCommand.CREATE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractExternalGroupBox extends AbstractGroupBox {

  public ExternalStringField getExternalStringField() {
    return getFieldByClass(ExternalStringField.class);
  }

  @Order(10.0)
  public class ExternalStringField extends AbstractStringField {
  }

}
