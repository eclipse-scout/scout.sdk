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
package org.eclipse.scout.sdk.help.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class ScoutSdkHelpActivator extends Plugin {

  private static ScoutSdkHelpActivator instance;

  public static ScoutSdkHelpActivator getInstance() {
    return instance;
  }

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    instance = this;
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    instance = null;
  }
}
