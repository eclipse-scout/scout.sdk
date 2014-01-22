package sample.server.services.lookup;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.services.lookup.AbstractLookupService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

import sample.shared.services.lookup.ICountryLookupService;

public class CountryLookupService extends AbstractLookupService<Long> implements ICountryLookupService {

  @Override
  public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) throws ProcessingException {
    return null;
  }

  @Override
  public List<? extends ILookupRow<Long>> getDataByText(ILookupCall<Long> call) throws ProcessingException {
    return null;
  }

  @Override
  public List<? extends ILookupRow<Long>> getDataByAll(ILookupCall<Long> call) throws ProcessingException {
    return null;
  }

  @Override
  public List<? extends ILookupRow<Long>> getDataByRec(ILookupCall<Long> call) throws ProcessingException {
    return null;
  }
}
