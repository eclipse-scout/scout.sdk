package formdata.client.scope.field;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.scope.field.AbstractScopeTestGroupBoxData;

@FormData(value = AbstractScopeTestGroupBoxData.class, sdkCommand = SdkCommand.CREATE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractScopeTestGroupBox extends AbstractGroupBox {
  @Order(20)
  public class ProcessField extends AbstractTreeBox<Long> {
  }
}
