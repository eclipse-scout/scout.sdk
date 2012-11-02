package presenter.test.server.services.process;

import org.eclipse.scout.service.AbstractService;
import presenter.test.shared.services.process.IDesktopProcessService;
import presenter.test.shared.services.process.DesktopFormData;
import org.eclipse.scout.commons.exception.ProcessingException;

public class DesktopProcessService extends AbstractService implements IDesktopProcessService{

  @Override
  public DesktopFormData load(DesktopFormData formData) throws ProcessingException {
    //TODO [mvi] Auto-generated method stub.
    return formData;
  
  }
}
