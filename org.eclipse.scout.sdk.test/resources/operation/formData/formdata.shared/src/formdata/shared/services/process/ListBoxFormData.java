package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

public class ListBoxFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public ListBoxFormData() {
  }

  public ListBox getListBox() {
    return getFieldByClass(ListBox.class);
  }

  public class ListBox extends AbstractValueFieldData {
    private static final long serialVersionUID = 1L;

    public ListBox() {
    }

  }
}
