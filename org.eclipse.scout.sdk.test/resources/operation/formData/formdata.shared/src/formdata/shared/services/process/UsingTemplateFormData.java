package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

public class UsingTemplateFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public UsingTemplateFormData() {
  }

  public InternalHtml getInternalHtml() {
    return getFieldByClass(InternalHtml.class);
  }

  public class InternalHtml extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public InternalHtml() {
    }

  }
}
