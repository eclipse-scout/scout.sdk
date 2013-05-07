/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.testing.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SdkTestingApi implements BundleActivator {

  private static BundleContext context;

  public static BundleContext getContext() {
    return context;
  }

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    SdkTestingApi.context = bundleContext;
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    SdkTestingApi.context = null;
  }
}
