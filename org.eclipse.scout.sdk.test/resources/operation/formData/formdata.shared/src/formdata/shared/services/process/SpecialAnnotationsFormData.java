package formdata.shared.services.process;

import formdata.shared.services.process.SimpleFormData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

public class SpecialAnnotationsFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public SpecialAnnotationsFormData() {
  }

  public WrappedFormForm getWrappedFormForm() {
    return getFieldByClass(WrappedFormForm.class);
  }

  public class WrappedFormForm extends SimpleFormData {
    private static final long serialVersionUID = 1L;

    public WrappedFormForm() {
    }

  }
}
