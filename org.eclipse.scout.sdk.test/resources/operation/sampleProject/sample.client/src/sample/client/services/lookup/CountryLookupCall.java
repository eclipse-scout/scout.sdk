package sample.client.services.lookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.lookup.AbstractCodeTypeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class CountryLookupCall extends AbstractCodeTypeLookupCall {

  private static final long serialVersionUID = 1L;

  @Override
  protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
    ArrayList<LookupRow> rows = new ArrayList<LookupRow>();
    //TODO [aho] create lookup rows here.
    return rows;
  }
}
