package formdata.client.ui.template.formfield.replace.levels;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

import formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData;

@FormData(value = AbstractMainBoxData.class, sdkCommand = FormData.SdkCommand.CREATE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractMainBox extends AbstractGroupBox {
  public FirstLevel getFirstLevel() {
    return getFieldByClass(FirstLevel.class);
  }

  public class FirstLevel extends AbstractTemplateField<Number> {
  }
}
