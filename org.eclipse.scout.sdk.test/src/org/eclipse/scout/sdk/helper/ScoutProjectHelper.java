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

import java.util.HashSet;

import junit.framework.Assert;

import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateUiSwingPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateUiSwtPluginOperation;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.ScoutProjectNewOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 *
 */
public final class ScoutProjectHelper {
  public static IScoutProject setupNewProject(String projectName, boolean client, boolean shared, boolean server, PropertyMap properties) throws Exception {
    return setupNewProject(projectName, client, shared, server, false, false, properties);
  }

  public static IScoutProject setupNewProject(String projectName, boolean client, boolean shared, boolean server) throws Exception {
    return setupNewProject(projectName, client, shared, server, false, false);
  }

  public static IScoutProject setupNewProject(String projectName, boolean client, boolean shared, boolean server, boolean uiSwt, boolean uiSwing) throws Exception {
    return setupNewProject(projectName, client, shared, server, uiSwt, uiSwing, new PropertyMap());
  }

  public static IScoutProject setupNewProject(String projectName, boolean client, boolean shared, boolean server, boolean uiSwt, boolean uiSwing, PropertyMap properties) throws Exception {
    // define settings what to create
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_NAME, projectName);
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_NAME_POSTFIX, "");
    properties.setProperty(IScoutProjectNewOperation.PROP_PROJECT_ALIAS, "alias");
    properties.setProperty(IScoutProjectNewOperation.PROP_TARGET_PLATFORM_VERSION, PlatformVersionUtility.getPlatformVersion());
    HashSet<String> nodesToCreate = new HashSet<String>();
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
    OperationJob job = new OperationJob(mainOperation);
    job.schedule();
    job.join();

    // build and wait for silent workspace
    AbstractScoutSdkTest.buildWorkspace();

    // get scout workspace
    IScoutProject[] rootProjects = ScoutSdkCore.getScoutWorkspace().getRootProjects();
    Assert.assertEquals(1, rootProjects.length);
    IScoutProject scoutProject = rootProjects[0];
    return scoutProject;
  }
}
