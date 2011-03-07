package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

public class FormWithGroupBoxesFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public FormWithGroupBoxesFormData() {
  }

  public FlatString getFlatString() {
    return getFieldByClass(FlatString.class);
  }

  public InnerInteger getInnerInteger() {
    return getFieldByClass(InnerInteger.class);
  }

  public class FlatString extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public FlatString() {
    }

  }

  public class InnerInteger extends AbstractValueFieldData<Integer> {
    private static final long serialVersionUID = 1L;

    public InnerInteger() {
    }

  }
}
