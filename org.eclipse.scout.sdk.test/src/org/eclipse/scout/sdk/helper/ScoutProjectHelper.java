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
package org.eclipse.scout.sdk.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateUiSwingPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateUiSwtPluginOperation;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.ScoutProjectNewOperation;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.Assert;

/**
 *
 */
public final class ScoutProjectHelper {
  public static IScoutBundle setupNewProject(String projectName, boolean client, boolean shared, boolean server, PropertyMap properties) throws Exception {
    return setupNewProject(projectName, client, shared, server, false, false, properties);
  }

  public static IScoutBundle setupNewProject(String projectName, boolean client, boolean shared, boolean server) throws Exception {
    return setupNewProject(projectName, client, shared, server, false, false);
  }

  public static IScoutBundle setupNewProject(String projectName, boolean client, boolean shared, boolean server, boolean uiSwt, boolean uiSwing) throws Exception {
    return setupNewProject(projectName, client, shared, server, uiSwt, uiSwing, new PropertyMap());
  }

  public static IScoutBundle setupNewProject(String projectName, boolean client, boolean shared, boolean server, boolean uiSwt, boolean uiSwing, PropertyMap properties) throws Exception {
    // define settings what to create
    properties.setProperty(IScoutProjectNewOperation.PROP_CREATED_BUNDLES, new ArrayList<>());
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_NAME, projectName);
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_NAME_POSTFIX, "");
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_ALIAS, "alias");
    properties.setProperty(IScoutProjectNewOperation.PROP_USE_DEFAULT_JDT_PREFS, Boolean.FALSE);
    properties.setProperty(IScoutProjectNewOperation.PROP_KEEP_CURRENT_TARGET, Boolean.TRUE);
    properties.setProperty(IScoutProjectNewOperation.PROP_TARGET_PLATFORM_VERSION, JdtUtility.getTargetPlatformVersion());
    HashSet<String> nodesToCreate = new HashSet<>(5);
    if (client) {
      nodesToCreate.add(CreateClientPluginOperation.BUNDLE_ID);
    }
    if (shared) {
      nodesToCreate.add(CreateSharedPluginOperation.BUNDLE_ID);
    }
    if (server) {
      nodesToCreate.add(CreateServerPluginOperation.BUNDLE_ID);
    }
    if (uiSwt) {
      nodesToCreate.add(CreateUiSwtPluginOperation.BUNDLE_ID);
    }
    if (uiSwing) {
      nodesToCreate.add(CreateUiSwingPluginOperation.BUNDLE_ID);
    }
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_CHECKED_NODES, nodesToCreate);

    // execute scout project creation according to the properties defined
    ScoutProjectNewOperation mainOperation = new ScoutProjectNewOperation();
    mainOperation.setProperties(properties);
    TestWorkspaceUtility.executeAndBuildWorkspace(mainOperation);

    // build and wait for silent workspace
    TestWorkspaceUtility.buildWorkspace();

    // get scout workspace
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    bundleGraph.waitFor();
    JdtUtility.waitForIndexesReady();

    Set<IScoutBundle> rootProjects = bundleGraph.getBundles(ScoutBundleFilters.getRootBundlesFilter());
    Assert.assertEquals(1, rootProjects.size());
    IScoutBundle scoutProject = CollectionUtility.firstElement(rootProjects);
    return scoutProject;
  }
}
