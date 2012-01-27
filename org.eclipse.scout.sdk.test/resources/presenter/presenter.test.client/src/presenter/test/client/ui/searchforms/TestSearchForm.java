package presenter.test.client.ui.searchforms;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractSearchForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

import presenter.test.shared.services.process.TestSearchFormData;

@FormData(value = TestSearchFormData.class, sdkCommand = SdkCommand.CREATE)
public class TestSearchForm extends AbstractSearchForm {

  public TestSearchForm() throws ProcessingException {
    super();
  }

  @Override
  public void startSearch() throws ProcessingException {
    startInternal(new SearchHandler());
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {
  }

  public class SearchHandler extends AbstractFormHandler {
  }
}
