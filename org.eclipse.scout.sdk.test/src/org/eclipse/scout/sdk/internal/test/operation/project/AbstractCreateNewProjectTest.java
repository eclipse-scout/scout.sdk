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
package org.eclipse.scout.sdk.internal.test.operation.project;

import junit.framework.Assert;

import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.project.CreateClientBundleOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateUiSwtPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillSharedPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillUiSwtPluginOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 *
 */
public class AbstractCreateNewProjectTest extends AbstractScoutSdkTest {

  IScoutProject scoutProject = null;

  protected IScoutProject setupNewProject(String projectName, String projectNamePostfix, String alias) throws Exception {
    TemplateVariableSet vars = TemplateVariableSet.createNew(projectName, projectNamePostfix, alias);
    CreateClientBundleOperation clientOp = new CreateClientBundleOperation(vars);
    CreateSharedPluginOperation sharedOp = new CreateSharedPluginOperation(vars);
    CreateServerPluginOperation serverOp = new CreateServerPluginOperation(vars);
    CreateUiSwtPluginOperation swtOp = new CreateUiSwtPluginOperation(vars);
    OperationJob createJob = new OperationJob(clientOp, sharedOp, serverOp, swtOp);
    createJob.schedule();
    createJob.join();
    FillClientPluginOperation fillClientOp = new FillClientPluginOperation(clientOp.getCreatedProject(), vars);
    FillSharedPluginOperation fillSharedtOp = new FillSharedPluginOperation(sharedOp.getCreatedProject(), vars);
    FillServerPluginOperation fillServerOp = new FillServerPluginOperation(serverOp.getCreatedProject(), vars);
    FillUiSwtPluginOperation fillSwtOp = new FillUiSwtPluginOperation(swtOp.getCreatedProject(), vars);
    OperationJob fillJob = new OperationJob(fillClientOp, fillSharedtOp, fillServerOp, fillSwtOp);
    fillJob.schedule();
    fillJob.join();
    buildWorkspace();
    IScoutProject[] rootProjects = ScoutSdkCore.getScoutWorkspace().getRootProjects();
    Assert.assertEquals(1, rootProjects.length);
    scoutProject = rootProjects[0];
    return scoutProject;
  }

}
