package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

public class IgnoredFieldsFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public IgnoredFieldsFormData() {
  }

  public NotIgnored getNotIgnored() {
    return getFieldByClass(NotIgnored.class);
  }

  public class NotIgnored extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public NotIgnored() {
    }

  }
}
