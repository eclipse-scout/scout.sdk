package formdata.client.ui.template.formfield.replace.levels;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;

import formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData;

@FormData(value = ThirdLevelFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ThirdLevelForm extends SecondLevelForm {

  public ThirdLevelForm() throws ProcessingException {
    super();
  }

  @Replace
  public class ThirdInnerBox extends SecondInnerBox {
    public ThirdInnerBox(FirstLevelForm.MainBox m) {
      super(m);
    }

    @Replace
    public class ThirdLevel extends SecondLevelForm.SecondInnerBox.SecondLevel {
      public ThirdLevel(FirstLevelForm.MainBox.FirstInnerBox m) {
        super(m);
      }
    }
  }
}
