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

import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.util.SdkLog;
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

    Job j = new Job("register scout archetype") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        registerScoutArchetype();
        return Status.OK_STATUS;
      }
    };
    j.setUser(false);
    j.setSystem(true);
    j.schedule(1000);
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

  private static void registerScoutArchetype() {
    try {
      MavenPluginActivator mavenPlugin = MavenPluginActivator.getDefault();
      if (mavenPlugin == null) {
        return;
      }
      org.eclipse.m2e.core.internal.archetype.ArchetypeManager archetypeManager = mavenPlugin.getArchetypeManager();
      if (archetypeManager == null) {
        return;
      }
      ArchetypeManager archetyper = archetypeManager.getArchetyper();
      if (archetyper == null) {
        return;
      }

      Archetype archetype = new Archetype();
      archetype.setGroupId(ScoutProjectNewHelper.SCOUT_ARCHETYPES_GROUP_ID);
      archetype.setArtifactId(ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID);
      archetype.setVersion(ScoutProjectNewHelper.SCOUT_ARCHETYPES_VERSION);
      archetype.setDescription("Creates a new Scout helloworld application. Instead of using the 'Maven Project' wizard you may also use the 'Scout Project' wizard which already prefills all properties with correct values.");
      archetype.setRepository("http://repo1.maven.org/maven2/");
      archetyper.updateLocalCatalog(archetype);
    }
    catch (Exception e) {
      SdkLog.info("Unable to register Scout HelloWorld archetype.", e);
    }
  }
}
