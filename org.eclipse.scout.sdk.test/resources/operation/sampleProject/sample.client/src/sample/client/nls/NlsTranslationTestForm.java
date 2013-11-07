/**
 * 
 */
package sample.client.nls;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;

import sample.client.nls.NlsTranslationTestForm.MainBox.CancelButton;
import sample.client.nls.NlsTranslationTestForm.MainBox.OkButton;
import sample.client.nls.NlsTranslationTestForm.MainBox.String01Field;
import sample.client.nls.NlsTranslationTestForm.MainBox.String02Field;

/**
 *  @author Andreas Hoegger
 */
public class NlsTranslationTestForm extends AbstractForm {

  /**
   * @throws org.eclipse.scout.commons.exception.ProcessingException
   */
  public NlsTranslationTestForm() throws ProcessingException {
    super();
  }

  /**
   * @throws org.eclipse.scout.commons.exception.ProcessingException
   */
  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  /**
   * @throws org.eclipse.scout.commons.exception.ProcessingException
   */
  public void startNew() throws ProcessingException {
    startInternal(new NewHandler());
  }

  /**
   * @return the CancelButton
   */
  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  /**
   * @return the MainBox
   */
  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  /**
   * @return the OkButton
   */
  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  /**
   * @return the String01Field
   */
  public String01Field getString01Field() {
    return getFieldByClass(String01Field.class);
  }

  /**
   * @return the String02Field
   */
  public String02Field getString02Field() {
    return getFieldByClass(String02Field.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected String getConfiguredLabel() {
      return TEXTS.get("NlsCounter01Key");
    }

    @Order(10.0)
    public class String01Field extends AbstractStringField {
    }

    @Order(20.0)
    public class String02Field extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("NlsCounter02Key");
      }
    }

    @Order(30.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(40.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {
  }

  public class NewHandler extends AbstractFormHandler {
  }
}
