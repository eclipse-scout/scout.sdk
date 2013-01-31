package formdata.client.ui.forms.replace;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

import formdata.client.ui.forms.replace.BaseForm.MainBox.CloseButton;
import formdata.client.ui.forms.replace.BaseForm.MainBox.GroupBox;
import formdata.client.ui.forms.replace.BaseForm.MainBox.GroupBox.NameField;
import formdata.client.ui.forms.replace.BaseForm.MainBox.GroupBox.SmartField;
import formdata.client.ui.forms.replace.BaseForm.MainBox.IgnoringGroupBox;
import formdata.client.ui.forms.replace.BaseForm.MainBox.IgnoringGroupBox.IgnoringGroupBoxField;
import formdata.shared.services.process.replace.BaseFormData;
import formdata.shared.services.process.replace.TestingCodeType;
import formdata.shared.services.process.replace.UsingFormFieldData;

@FormData(value = BaseFormData.class, sdkCommand = SdkCommand.CREATE)
public class BaseForm extends AbstractForm {

  public BaseForm() throws ProcessingException {
    super();
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public GroupBox getGroupBox() {
    return getFieldByClass(GroupBox.class);
  }

  public NameField getNameField() {
    return getFieldByClass(NameField.class);
  }

  public SmartField getSmartField() {
    return getFieldByClass(SmartField.class);
  }

  public IgnoringGroupBox getIgnoringGroupBox() {
    return getFieldByClass(IgnoringGroupBox.class);
  }

  public IgnoringGroupBoxField getIgnoringGroupBoxField() {
    return getFieldByClass(IgnoringGroupBoxField.class);
  }

  public CloseButton getCloseButton() {
    return getFieldByClass(CloseButton.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class NoneFormDataFieldsGroupBox extends AbstractGroupBox {
      @Order(10)
      public class SdkCommandNoneField extends AbstractStringField {
      }
    }

    @Order(20)
    public class CreatingFormDataFieldsGroupBox extends AbstractGroupBox {
      @Order(10)
      @FormData(sdkCommand = SdkCommand.CREATE)
      public class SdkCommandCreateField extends AbstractStringField {
      }
    }

    @Order(30)
    public class UsingFormDataFieldsGroupBox extends AbstractGroupBox {
      @Order(10)
      @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
      public class SdkCommandUseField extends AbstractStringField {
      }
    }

    @Order(40)
    public class IgnoringFormDataFieldsGroupBox extends AbstractGroupBox {
      @Order(10)
      @FormData(sdkCommand = SdkCommand.IGNORE)
      public class SdkCommandIgnoreField extends AbstractStringField {
      }
    }

    @Order(50.0)
    public class GroupBox extends AbstractGroupBox {

      @Order(10.0)
      public class NameField extends AbstractStringField {

        @Override
        protected boolean getConfiguredMandatory() {
          return true;
        }

        @Override
        protected int getConfiguredMaxLength() {
          return 60;
        }
      }

      @Order(20.0)
      public class SmartField extends AbstractSmartField<Long> {
        @Override
        protected Class<? extends ICodeType<?>> getConfiguredCodeType() {
          return TestingCodeType.class;
        }
      }

      @Order(30.0)
      public class LookupField extends AbstractLookupField {
      }
    }

    @Order(20.0)
    @FormData(sdkCommand = SdkCommand.IGNORE)
    public class IgnoringGroupBox extends AbstractGroupBox {

      @Order(10.0)
      public class IgnoringGroupBoxField extends AbstractStringField {
      }
    }

    @Order(100.0)
    public class CloseButton extends AbstractCloseButton {
    }
  }
}
