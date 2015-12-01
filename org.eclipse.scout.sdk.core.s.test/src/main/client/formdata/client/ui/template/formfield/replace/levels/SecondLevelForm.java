package formdata.client.ui.template.formfield.replace.levels;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

import formdata.client.ui.template.formfield.replace.levels.FirstLevelForm.MainBox.FirstInnerBox;
import formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData;

@FormData(value = SecondLevelFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class SecondLevelForm extends FirstLevelForm {

  public SecondLevelForm() throws ProcessingException {
    super();
  }

  @Replace
  public class SecondInnerBox extends FirstInnerBox {
    public SecondInnerBox(FirstLevelForm.MainBox m) {
      m.super();
    }

    @Replace
    public class SecondLevel extends FirstLevelForm.MainBox.FirstInnerBox.FirstLevel {
      public SecondLevel(FirstLevelForm.MainBox.FirstInnerBox m) {
        m.super();
      }
    }
  }

}
