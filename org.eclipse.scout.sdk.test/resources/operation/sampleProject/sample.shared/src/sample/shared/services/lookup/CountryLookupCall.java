package sample.shared.services.lookup;

import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import sample.shared.services.lookup.ICountryLookupService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

public class CountryLookupCall extends LookupCall{

  private static final long serialVersionUID = 1L;

  @Override
  protected Class<? extends ILookupService> getConfiguredService() {
    return ICountryLookupService.class;
  }
}
