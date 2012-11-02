package presenter.test.client.ui.forms;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.service.SERVICES;

import presenter.test.client.ui.forms.DesktopForm.MainBox.BooleanPresenterTestField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.CodeTypePresenterTestField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.ColorPresenterTestBox;
import presenter.test.client.ui.forms.DesktopForm.MainBox.DisplayStylePresenterTestButton;
import presenter.test.client.ui.forms.DesktopForm.MainBox.DoublePresenterTestBox;
import presenter.test.client.ui.forms.DesktopForm.MainBox.FontPresenterTestBox;
import presenter.test.client.ui.forms.DesktopForm.MainBox.HorizontalAlignmentPresenterTestBox;
import presenter.test.client.ui.forms.DesktopForm.MainBox.IconPresenterField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.IntegerPresenterTestBox;
import presenter.test.client.ui.forms.DesktopForm.MainBox.LabelHorizontalAlignmentTestField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.LabelPositionPresenterTestField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.LongPresenterTestField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.MasterFieldPresenterTestField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.NlsTextProposalPresenterTestField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.StringPresenterTestField;
import presenter.test.client.ui.forms.DesktopForm.MainBox.SystemTypePresenterTestButton;
import presenter.test.client.ui.forms.DesktopForm.MainBox.VerticalAlignmentPresenterTestBox;
import presenter.test.shared.Icons;
import presenter.test.shared.services.code.TestCodeType;
import presenter.test.shared.services.lookup.TestLookupCall;
import presenter.test.shared.services.process.DesktopFormData;
import presenter.test.shared.services.process.IDesktopProcessService;

@FormData(value = DesktopFormData.class, sdkCommand = SdkCommand.CREATE)
public class DesktopForm extends AbstractForm {

  public DesktopForm() throws ProcessingException {
    super();
  }

  @Override
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_VIEW;
  }

  public BooleanPresenterTestField getBooleanPresenterTestField() {
    return getFieldByClass(BooleanPresenterTestField.class);
  }

  public MasterFieldPresenterTestField getMasterFieldPresenterTestField() {
    return getFieldByClass(MasterFieldPresenterTestField.class);
  }

  public NlsTextProposalPresenterTestField getNlsTextProposalPresenterTestField() {
    return getFieldByClass(NlsTextProposalPresenterTestField.class);
  }

  public StringPresenterTestField getStringPresenterTestField() {
    return getFieldByClass(StringPresenterTestField.class);
  }

  public SystemTypePresenterTestButton getSystemTypePresenterTestButton() {
    return getFieldByClass(SystemTypePresenterTestButton.class);
  }

  public VerticalAlignmentPresenterTestBox getVerticalAlignmentPresenterTestBox() {
    return getFieldByClass(VerticalAlignmentPresenterTestBox.class);
  }

  public CodeTypePresenterTestField getCodeTypePresenterField() {
    return getFieldByClass(CodeTypePresenterTestField.class);
  }

  public ColorPresenterTestBox getColorPresenterTestBox() {
    return getFieldByClass(ColorPresenterTestBox.class);
  }

  public DisplayStylePresenterTestButton getDisplayStylePresenterTestButton() {
    return getFieldByClass(DisplayStylePresenterTestButton.class);
  }

  public DoublePresenterTestBox getDoublePresenterTestBox() {
    return getFieldByClass(DoublePresenterTestBox.class);
  }

  public FontPresenterTestBox getFontPresenterTestBox() {
    return getFieldByClass(FontPresenterTestBox.class);
  }

  public HorizontalAlignmentPresenterTestBox getHorizontalAlignmentPresenterTestBox() {
    return getFieldByClass(HorizontalAlignmentPresenterTestBox.class);
  }

  public IconPresenterField getIconPresenterField() {
    return getFieldByClass(IconPresenterField.class);
  }

  public IntegerPresenterTestBox getIntegerPresenterTestBox() {
    return getFieldByClass(IntegerPresenterTestBox.class);
  }

  public LabelHorizontalAlignmentTestField getLabelHorizontalAlignmentTestField() {
    return getFieldByClass(LabelHorizontalAlignmentTestField.class);
  }

  public LabelPositionPresenterTestField getLabelPositionPresenterTestField() {
    return getFieldByClass(LabelPositionPresenterTestField.class);
  }

  public LongPresenterTestField getLongPresenterTestField() {
    return getFieldByClass(LongPresenterTestField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class BooleanPresenterTestField extends AbstractStringField {
      @Override
      protected boolean getConfiguredMandatory() {
        return true;
      }
    }

    @Order(20.0)
    public class CodeTypePresenterTestField extends AbstractSmartField<Long> {

      @Override
      protected Class<? extends ICodeType<Long>> getConfiguredCodeType() {
        return TestCodeType.class;
      }
    }

    @Order(30.0)
    public class LookupCallProposalPresenterTestField extends AbstractSmartField<Long> {

      @Override
      protected Class<? extends LookupCall> getConfiguredLookupCall() {
        return TestLookupCall.class;
      }
    }

    @Order(40.0)
    public class IconPresenterField extends AbstractSmartField<Long> {
      @Override
      protected String getConfiguredIconId() {
        return Icons.Eye;
      }
    }

    @Order(50.0)
    public class VerticalAlignmentPresenterTestBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredVerticalAlignment() {
        return 0;
      }
    }

    @Order(60.0)
    public class HorizontalAlignmentPresenterTestBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return 0;
      }
    }

    @Order(70.0)
    public class ColorPresenterTestBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredBackgroundColor() {
        return "000080";
      }
    }

    @Order(80.0)
    public class FontPresenterTestBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredFont() {
        return "Courier New";
      }
    }

    @Order(90.0)
    public class NlsTextProposalPresenterTestField extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("Attribute");
      }
    }

    @Order(100.0)
    public class DoublePresenterTestBox extends AbstractGroupBox {

      @Override
      protected double getConfiguredGridWeightX() {
        return 10.1;
      }
    }

    @Order(110.0)
    public class IntegerPresenterTestBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredGridColumnCount() {
        return 8;
      }
    }

    @Order(120.0)
    public class StringPresenterTestField extends AbstractDoubleField {

      @Override
      protected String getConfiguredFormat() {
        return "aa";
      }
    }

    @Order(130.0)
    public class LongPresenterTestField extends AbstractLongField {

      @Override
      protected Long getConfiguredMaximumValue() {
        return 100L;
      }
    }

    @Order(140.0)
    public class MasterFieldPresenterTestField extends AbstractSmartField<Long> {

      @Override
      protected Class<? extends IValueField> getConfiguredMasterField() {
        return CodeTypePresenterTestField.class;
      }
    }

    @Order(150.0)
    public class DisplayStylePresenterTestButton extends AbstractButton {
      @Override
      protected int getConfiguredDisplayStyle() {
        return DISPLAY_STYLE_LINK;
      }
    }

    @Order(160.0)
    public class SystemTypePresenterTestButton extends AbstractButton {

      @Override
      protected int getConfiguredSystemType() {
        return SYSTEM_TYPE_RESET;
      }
    }

    @Order(170.0)
    public class LabelPositionPresenterTestField extends AbstractStringField {

      @Override
      protected int getConfiguredLabelPosition() {
        return LABEL_POSITION_ON_FIELD;
      }
    }

    @Order(180.0)
    public class LabelHorizontalAlignmentTestField extends AbstractStringField {

      @Override
      protected int getConfiguredLabelHorizontalAlignment() {
        return 0;
      }
    }
  }

  public class ViewHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      IDesktopProcessService service = SERVICES.getService(IDesktopProcessService.class);
      DesktopFormData formData = new DesktopFormData();
      exportFormData(formData);
      formData = service.load(formData);
      importFormData(formData);

    }
  }

  public void startView() throws ProcessingException {
    startInternal(new ViewHandler());
  }
}
