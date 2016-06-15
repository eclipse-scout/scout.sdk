/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.sdk.s2e.internal.dto.DtoDerivedResourceHandlerFactory;
import org.eclipse.scout.sdk.s2e.internal.trigger.DerivedResourceManager;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class S2ESdkActivator extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.s2e";

  private static volatile S2ESdkActivator plugin;

  private DerivedResourceManager m_derivedResourceManager;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    plugin = this;

    // DTO auto update
    m_derivedResourceManager = new DerivedResourceManager();
    m_derivedResourceManager.addDerivedResourceHandlerFactory(new DtoDerivedResourceHandlerFactory());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_derivedResourceManager.dispose();
    m_derivedResourceManager = null;

    plugin = null;
    super.stop(context);
  }

  public static S2ESdkActivator getDefault() {
    return plugin;
  }

  public DerivedResourceManager getDerivedResourceManager() {
    return m_derivedResourceManager;
  }
}
