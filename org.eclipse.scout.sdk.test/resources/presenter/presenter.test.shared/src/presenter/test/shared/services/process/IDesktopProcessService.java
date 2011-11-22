package presenter.test.shared.services.process;

import org.eclipse.scout.service.IService;
import presenter.test.shared.services.process.DesktopFormData;
import org.eclipse.scout.commons.exception.ProcessingException;

public interface IDesktopProcessService extends IService{

  public DesktopFormData load(DesktopFormData formData) throws ProcessingException;
}
