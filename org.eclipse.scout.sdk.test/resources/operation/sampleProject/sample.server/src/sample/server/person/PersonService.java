package sample.server.person;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.service.AbstractService;

import sample.shared.person.CreatePersonPermission;
import sample.shared.person.IPersonService;
import sample.shared.person.PersonFormData;
import sample.shared.person.ReadPersonPermission;
import sample.shared.person.UpdatePersonPermission;

public class PersonService extends AbstractService implements IPersonService {

  @Override
  public PersonFormData prepareCreate(PersonFormData formData) throws ProcessingException {
    if (!ACCESS.check(new CreatePersonPermission())) {
      throw new VetoException(TEXTS.get("AuthorizationFailed"));
    }
    //TODO [aho] business logic here
    return formData;
  }

  @Override
  public PersonFormData create(PersonFormData formData) throws ProcessingException {
    if (!ACCESS.check(new CreatePersonPermission())) {
      throw new VetoException(TEXTS.get("AuthorizationFailed"));
    }
    //TODO [aho] business logic here.
    return formData;
  }

  @Override
  public PersonFormData load(PersonFormData formData) throws ProcessingException {
    if (!ACCESS.check(new ReadPersonPermission())) {
      throw new VetoException(TEXTS.get("AuthorizationFailed"));
    }
    //TODO [aho] business logic here
    return formData;
  }

  @Override
  public PersonFormData store(PersonFormData formData) throws ProcessingException {
    if (!ACCESS.check(new UpdatePersonPermission())) {
      throw new VetoException(TEXTS.get("AuthorizationFailed"));
    }
    //TODO [aho] business logic here
    return formData;
  }
}
