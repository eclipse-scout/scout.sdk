/**
 * 
 */
package sample.shared.nls;

import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.service.IService;

/**
 *  @author Andreas Hoegger
 */
@InputValidation(IValidationStrategy.PROCESS.class)
public interface INlsTranslationTestService extends IService {
}
