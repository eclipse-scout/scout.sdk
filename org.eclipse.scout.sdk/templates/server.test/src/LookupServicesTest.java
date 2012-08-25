/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
