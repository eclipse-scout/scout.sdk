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
package org.eclipse.scout.sdk.operation.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * <h3>NewBsiCaseGroupStep1Operation</h3> ...
 */
public class NewBsiCaseGroupStep1Operation implements IOperation {

  public static final String SUFFIX_CLIENT = ".client";
  public static final String SUFFIX_CLIENT_TEST = ".client.test";
  public static final String SUFFIX_CLIENT_SWT = ".ui.swt";
  public static final String SUFFIX_CLIENT_SWING = ".ui.swing";

  public static final String SUFFIX_SHARED = ".shared";
  public static final String SUFFIX_SERVER = ".server";
  // public static final String SUFFIX_SERVER_CORE_APP=".server.app";
  public static final String SUFFIX_SERVER_TEST = ".server.test";
  // public static final String SUFFIX_CORE_TARGET=".target";
  public static final String SUFFIX_PROJECTSETS = ".projectsets";

  // fields
  private boolean m_createUiSwing;
  private boolean m_createUiSwt;
  private boolean m_createClient;
  private boolean m_createShared;
  private boolean m_createServer;
  private String m_projectName;
  private String m_projectNamePostfix;
  private String m_projectAlias;

  // operation members
  private IProject m_sharedProject;
  private IProject m_clientProject;
  private IProject m_clientTestProject;
  private IProject m_serverProject;
  private IProject m_serverAppProject;
  private IProject m_serverTestProject;
  private IProject m_uiSwingProject;
  private IProject m_uiSwtProject;
  private IProject m_uiSwtAppProject;
  private IProject m_targetProject;
  private IProject m_projectSetProject;
  private final TemplateVariableSet m_templateBindings;

  public NewBsiCaseGroupStep1Operation(TemplateVariableSet templateBindings) {
    m_templateBindings = templateBindings;
  }

  @Override
  public String getOperationName() {
    return "Create Scout Project '" + getProjectName() + "'";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getProjectName())) {
      throw new IllegalArgumentException("project name can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getProjectAlias())) {
      throw new IllegalArgumentException("project alias can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    List<IProject> projects = new ArrayList<IProject>();

    // shared
    if (isCreateShared()) {
      CreateSharedPluginOperation op = new CreateSharedPluginOperation(m_templateBindings);
      op.run(monitor, workingCopyManager);
      m_sharedProject = op.getCreatedProject();
      projects.add(m_sharedProject);
    }
    // client
    if (isCreateClient()) {

      CreateClientBundleOperation op = new CreateClientBundleOperation(m_templateBindings);
      op.run(monitor, workingCopyManager);
      m_clientProject = op.getCreatedProject();
      projects.add(m_clientProject);
      //
      // CreateClientTestBundleOperation op3=new CreateClientTestBundleOperation(m_templateBindings);
      // op3.run(monitor, workingCopyManager);
      // m_clientTestProject=op3.getCreatedProject();
      // projects.add(m_clientTestProject);
    }
    // server
    if (isCreateServer()) {
      CreateServerPluginOperation op = new CreateServerPluginOperation(m_templateBindings);
      op.run(monitor, workingCopyManager);
      m_serverProject = op.getCreatedProject();
      projects.add(m_serverProject);

      //
      // CreateServerTestPluginOperation op3=new CreateServerTestPluginOperation(m_templateBindings);
      // op3.run(monitor, workingCopyManager);
      // m_serverTestProject=op3.getCreatedProject();
      // projects.add(m_serverTestProject);
    }

    // ui
    if (isCreateUiSwing()) {
      CreateUiSwingPluginOperation op = new CreateUiSwingPluginOperation(m_templateBindings);
      op.run(monitor, workingCopyManager);
      m_uiSwingProject = op.getCreatedProject();
      projects.add(m_uiSwingProject);
    }
    if (isCreateUiSwt()) {
      CreateUiSwtPluginOperation swtOp = new CreateUiSwtPluginOperation(m_templateBindings);
      swtOp.run(monitor, workingCopyManager);
      m_uiSwtProject = swtOp.getCreatedProject();
      projects.add(m_uiSwtProject);
    }

    // // target definitions
    // CreateTargetProjectOperation opTarget=new CreateTargetProjectOperation(getProjectName() + SUFFIX_CORE_TARGET, templateBindings);
    // opTarget.run(monitor, workingCopyManager);
    // m_targetProject=opTarget.getCreatedProject();
    // projects.add(m_targetProject);

    // project set definitions
    // CreateProjectSetsProjectOperation opPsf=new CreateProjectSetsProjectOperation(m_templateBindings);
    // opPsf.run(monitor, workingCopyManager);
    // m_projectSetProject=opPsf.getCreatedProject();
    // projects.add(m_projectSetProject);

    // refresh all
    for (IProject p : projects) {
      p.refreshLocal(IResource.DEPTH_INFINITE, null);
    }
  }

  public void setCreateUiSwing(boolean createUiSwing) {
    m_createUiSwing = createUiSwing;
  }

  public boolean isCreateUiSwing() {
    return m_createUiSwing;
  }

  public void setCreateUiSwt(boolean createUiSwt) {
    m_createUiSwt = createUiSwt;
  }

  public boolean isCreateUiSwt() {
    return m_createUiSwt;
  }

  public boolean isCreateClient() {
    return m_createClient;
  }

  public void setCreateClient(boolean createClient) {
    m_createClient = createClient;
  }

  public boolean isCreateShared() {
    return m_createShared;
  }

  public void setCreateShared(boolean createShared) {
    m_createShared = createShared;
  }

  public boolean isCreateServer() {
    return m_createServer;
  }

  public void setCreateServer(boolean createServer) {
    m_createServer = createServer;
  }

  public String getProjectName() {
    return m_projectName;
  }

  public void setProjectName(String projectName) {
    m_projectName = projectName;
  }

  public void setProjectNamePostfix(String projectNamePostfix) {
    m_projectNamePostfix = projectNamePostfix;
  }

  public String getProjectNamePostfix() {
    return m_projectNamePostfix;
  }

  public void setProjectAlias(String projectAlias) {
    m_projectAlias = projectAlias;
  }

  public String getProjectAlias() {
    return m_projectAlias;
  }

  public IProject getSharedProject() {
    return m_sharedProject;
  }

  public IProject getClientProject() {
    return m_clientProject;
  }

  public IProject getClientTestProject() {
    return m_clientTestProject;
  }

  public IProject getServerProject() {
    return m_serverProject;
  }

  public IProject getServerAppProject() {
    return m_serverAppProject;
  }

  public IProject getServerTestProject() {
    return m_serverTestProject;
  }

  public IProject getUiSwingProject() {
    return m_uiSwingProject;
  }

  public IProject getUiSwtProject() {
    return m_uiSwtProject;
  }

}
