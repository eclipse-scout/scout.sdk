package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

public abstract class AbstractExternalGroupBoxData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  public AbstractExternalGroupBoxData() {
  }

  public ExternalString getExternalString() {
    return getFieldByClass(ExternalString.class);
  }

  public class ExternalString extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public ExternalString() {
    }

  }
}
