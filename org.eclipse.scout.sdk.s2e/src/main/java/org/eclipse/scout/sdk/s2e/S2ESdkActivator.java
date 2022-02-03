/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStoreSupplier;
import org.eclipse.scout.sdk.core.s.nls.Translations;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.eclipse.scout.sdk.s2e.dataobject.EclipseDoContextResolver;
import org.eclipse.scout.sdk.s2e.derived.DerivedResourceManager;
import org.eclipse.scout.sdk.s2e.derived.DtoDerivedResourceHandlerFactory;
import org.eclipse.scout.sdk.s2e.derived.IDerivedResourceManager;
import org.eclipse.scout.sdk.s2e.nls.EclipseTranslationStoreSupplier;
import org.eclipse.scout.sdk.s2e.operation.MavenBuildOperation.M2eMavenRunner;
import org.osgi.framework.BundleContext;

public class S2ESdkActivator extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.s2e";

  private static volatile S2ESdkActivator plugin;
  private volatile ITranslationStoreSupplier m_nlsSupplier;
  private volatile DerivedResourceManager m_derivedResourceManager;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    plugin = this;

    // DTO auto update
    m_derivedResourceManager = new DerivedResourceManager();
    m_derivedResourceManager.addDerivedResourceHandlerFactory(new DtoDerivedResourceHandlerFactory());

    // TranslationStore supplier (NLS)
    m_nlsSupplier = new EclipseTranslationStoreSupplier();
    Translations.registerStoreSupplier(m_nlsSupplier);

    // DataObject-context resolver
    DoContextResolvers.set(new EclipseDoContextResolver());

    // Maven runner
    MavenRunner.set(new M2eMavenRunner());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    MavenRunner.set(null);

    DoContextResolvers.set(null);

    Translations.removeStoreSupplier(m_nlsSupplier);
    m_nlsSupplier = null;

    m_derivedResourceManager.dispose();
    m_derivedResourceManager = null;

    plugin = null;
    super.stop(context);
  }

  public static S2ESdkActivator getDefault() {
    return plugin;
  }

  public IDerivedResourceManager getDerivedResourceManager() {
    return m_derivedResourceManager;
  }
}
