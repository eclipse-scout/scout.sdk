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
import java.util.List;

import junit.framework.Assert;

import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.project.CreateClientBundleOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateUiSwingPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateUiSwtPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillSharedPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillUiSwingPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillUiSwtPluginOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 *
 */
public final class ScoutProjectHelper {
  public static IScoutProject setupNewProject(String projectName, boolean client, boolean shared, boolean server) throws Exception {
    return setupNewProject(projectName, client, shared, server, false, false);

  }

  public static IScoutProject setupNewProject(String projectName, boolean client, boolean shared, boolean server, boolean uiSwt, boolean uiSwing) throws Exception {
    TemplateVariableSet vars = TemplateVariableSet.createNew(projectName, "", "alias");
    List<IOperation> createOps = new ArrayList<IOperation>();
    CreateClientBundleOperation clientOp = null;
    CreateSharedPluginOperation sharedOp = null;
    CreateServerPluginOperation serverOp = null;
    CreateUiSwtPluginOperation swtOp = null;
    CreateUiSwingPluginOperation swingOp = null;
    if (client) {
      clientOp = new CreateClientBundleOperation(vars);
      createOps.add(clientOp);
    }
    if (shared) {
      sharedOp = new CreateSharedPluginOperation(vars);
      createOps.add(sharedOp);
    }
    if (server) {
      serverOp = new CreateServerPluginOperation(vars);
      createOps.add(serverOp);
    }
    if (uiSwt) {
      swtOp = new CreateUiSwtPluginOperation(vars);
      createOps.add(swtOp);
    }
    if (uiSwing) {
      swingOp = new CreateUiSwingPluginOperation(vars);
      createOps.add(swingOp);
    }
    OperationJob createJob = new OperationJob(createOps);
    createJob.schedule();
    createJob.join();
    List<IOperation> fillOps = new ArrayList<IOperation>();
    if (clientOp != null) {
      FillClientPluginOperation fillClientOp = new FillClientPluginOperation(clientOp.getCreatedProject(), vars);
      fillOps.add(fillClientOp);
    }
    if (sharedOp != null) {
      FillSharedPluginOperation fillSharedOp = new FillSharedPluginOperation(sharedOp.getCreatedProject(), vars);
      fillOps.add(fillSharedOp);
    }
    if (serverOp != null) {
      FillServerPluginOperation fillServerOp = new FillServerPluginOperation(serverOp.getCreatedProject(), vars);
      fillOps.add(fillServerOp);
    }
    if (swtOp != null) {
      FillUiSwtPluginOperation fillSwtOp = new FillUiSwtPluginOperation(swtOp.getCreatedProject(), vars);
      fillOps.add(fillSwtOp);
    }
    if (swingOp != null) {
      FillUiSwingPluginOperation fillSwingOp = new FillUiSwingPluginOperation(swingOp.getCreatedProject(), vars);
      fillOps.add(fillSwingOp);
    }
    OperationJob fillJob = new OperationJob(fillOps);
    fillJob.schedule();
    fillJob.join();
    AbstractScoutSdkTest.buildWorkspace();
    IScoutProject[] rootProjects = ScoutSdkCore.getScoutWorkspace().getRootProjects();
    Assert.assertEquals(1, rootProjects.length);
    IScoutProject scoutProject = rootProjects[0];
    return scoutProject;
  }
}
