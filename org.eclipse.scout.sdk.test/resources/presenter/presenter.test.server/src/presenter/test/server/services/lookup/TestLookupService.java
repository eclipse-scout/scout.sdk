package presenter.test.server.services.lookup;

import org.eclipse.scout.rt.server.services.lookup.AbstractSqlLookupService;
import presenter.test.shared.services.lookup.ITestLookupService;

public class TestLookupService extends AbstractSqlLookupService implements ITestLookupService{

  @Override
  public String getConfiguredSqlSelect(){
    return ""; //TODO [mvi] write select statement here.
  }
}
