package sample.server.services.lookup;

import sample.shared.services.lookup.ICountryLookupService;
import org.eclipse.scout.rt.server.services.lookup.AbstractLookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public class CountryLookupService extends AbstractLookupService implements ICountryLookupService{

  @Override
  public LookupRow[] getDataByAll(LookupCall call) throws ProcessingException {
    //TODO [aho] Auto-generated method stub
    return null;
  }

  @Override
  public LookupRow[] getDataByKey(LookupCall call) throws ProcessingException{
    //TODO [aho] Auto-generated method stub
    return null;
  }

  @Override
  public LookupRow[] getDataByRec(LookupCall call) throws ProcessingException{
    //TODO [aho] Auto-generated method stub
    return null;
  }

  @Override
  public LookupRow[] getDataByText(LookupCall call) throws ProcessingException{
    //TODO [aho] Auto-generated method stub
    return null;
  }
}
