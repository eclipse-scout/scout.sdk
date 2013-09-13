package sample.shared.empty;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.service.IService2;

@InputValidation(IValidationStrategy.PROCESS.class)
public interface IEmptyService extends IService2 {

  EmptyFormData prepareCreate(EmptyFormData formData) throws ProcessingException;

  EmptyFormData create(EmptyFormData formData) throws ProcessingException;

  EmptyFormData load(EmptyFormData formData) throws ProcessingException;

  EmptyFormData store(EmptyFormData formData) throws ProcessingException;
}
