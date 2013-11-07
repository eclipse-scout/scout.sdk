/**
 *
 */
package sample.server.nls;

import org.eclipse.scout.service.AbstractService;

import sample.shared.nls.INlsTranslationTestService;

/**
 *  @author Andreas Hoegger
 */
public class NlsTranslationTestService extends AbstractService implements INlsTranslationTestService {
  public String getNlsKey() {
    return "NlsCounter01Key";
  }
}
