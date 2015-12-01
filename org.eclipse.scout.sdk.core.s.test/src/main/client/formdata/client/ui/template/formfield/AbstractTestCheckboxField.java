package formdata.client.ui.template.formfield;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.checkbox.AbstractCheckBox;

import formdata.shared.services.process.AbstractTestCheckboxFieldData;

@FormData(value = AbstractTestCheckboxFieldData.class, sdkCommand = SdkCommand.CREATE)
public abstract class AbstractTestCheckboxField extends AbstractCheckBox {
}
