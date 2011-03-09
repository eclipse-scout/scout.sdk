package formdata.shared.services.process;

import formdata.shared.services.process.AbstractExternalGroupBoxData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import formdata.shared.services.process.AbstractTestCheckboxFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.commons.annotations.FormDataChecksum;

@FormDataChecksum(2975728837l)
public class UsingTemplateFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public UsingTemplateFormData() {
  }

  public ExternalGroupBox getExternalGroupBox() {
    return getFieldByClass(ExternalGroupBox.class);
  }

  public InternalHtml getInternalHtml() {
    return getFieldByClass(InternalHtml.class);
  }

  public TestCheckbox getTestCheckbox() {
    return getFieldByClass(TestCheckbox.class);
  }

  public class ExternalGroupBox extends AbstractExternalGroupBoxData {
    private static final long serialVersionUID = 1L;

    public ExternalGroupBox() {
    }

  }

  public class InternalHtml extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public InternalHtml() {
    }

  }

  public class TestCheckbox extends AbstractTestCheckboxFieldData {
    private static final long serialVersionUID = 1L;

    public TestCheckbox() {
    }

  }
}
