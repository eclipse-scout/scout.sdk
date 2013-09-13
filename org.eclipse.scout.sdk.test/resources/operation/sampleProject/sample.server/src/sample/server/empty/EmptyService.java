package sample.server.empty;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.service.AbstractService;

import sample.shared.empty.CreateEmptyPermission;
import sample.shared.empty.EmptyFormData;
import sample.shared.empty.IEmptyService;
import sample.shared.empty.ReadEmptyPermission;
import sample.shared.empty.UpdateEmptyPermission;

public class EmptyService extends AbstractService implements IEmptyService {

  @Override
  public EmptyFormData prepareCreate(EmptyFormData formData) throws ProcessingException {
    if (!ACCESS.check(new CreateEmptyPermission())) {
      throw new VetoException(TEXTS.get("AuthorizationFailed"));
    }
    //TODO [aho] business logic here
    return formData;
  }

  @Override
  public EmptyFormData create(EmptyFormData formData) throws ProcessingException {
    if (!ACCESS.check(new CreateEmptyPermission())) {
      throw new VetoException(TEXTS.get("AuthorizationFailed"));
    }
    //TODO [aho] business logic here.
    return formData;
  }

  @Override
  public EmptyFormData load(EmptyFormData formData) throws ProcessingException {
    if (!ACCESS.check(new ReadEmptyPermission())) {
      throw new VetoException(TEXTS.get("AuthorizationFailed"));
    }
    //TODO [aho] business logic here
    return formData;
  }

  @Override
  public EmptyFormData store(EmptyFormData formData) throws ProcessingException {
    if (!ACCESS.check(new UpdateEmptyPermission())) {
      throw new VetoException(TEXTS.get("AuthorizationFailed"));
    }
    //TODO [aho] business logic here
    return formData;
  }
}
