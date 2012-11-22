package @@BUNDLE_SERVER_TEST_NAME@@;

import org.eclipse.scout.rt.server.services.common.test.DefaultLookupServicesTest;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public class LookupServicesTest extends DefaultLookupServicesTest{

  @Override
  protected LookupCall createLookupCall(ILookupService s, String methodName) throws Throwable{
    LookupCall call=super.createLookupCall(s, methodName);
    //special services
    //...
    return call;
  }
}
