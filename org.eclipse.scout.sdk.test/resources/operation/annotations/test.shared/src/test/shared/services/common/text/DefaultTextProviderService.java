package test.shared.services.common.text;

import org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService;
import org.eclipse.scout.service.IService;

public class DefaultTextProviderService extends AbstractDynamicNlsTextProviderService implements IService {

  @Override
  protected String getDynamicNlsBaseName(){
    return "resources.texts.Texts";
  }
}
