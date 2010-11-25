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
package org.eclipse.scout.sdk.operation.axis;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.pde.BuildProperties;
import org.eclipse.scout.sdk.pde.PdeUtility;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class AxisWebServiceClientSetupOperation implements IOperation {

  private IScoutBundle m_serverBundle;

  public AxisWebServiceClientSetupOperation(IScoutBundle serverBundle) {
    m_serverBundle = serverBundle;
  }

  public IScoutBundle getProject() {
    return m_serverBundle;
  }

  public String getOperationName() {
    return "Setup/Repair Webservice Consumer Environment";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    // TODO Auto-generated method stub

  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      m_serverBundle.getProject().refreshLocal(IResource.DEPTH_ONE, monitor);
      TemplateVariableSet varSet = TemplateVariableSet.createNew(m_serverBundle);
      varSet.setVariable(TemplateVariableSet.VAR_BUNDLE_SERVER_NAME, m_serverBundle.getBundleName());
      if (!m_serverBundle.getProject().getFile("log4j.properties").exists()) {
        new InstallTextFileOperation("templates/server/log4j.properties", "log4j.properties", m_serverBundle.getProject(), varSet).run(monitor, workingCopyManager);
      }
      if (!m_serverBundle.getProject().getFile("META-INF/services/org.apache.axis.EngineConfigurationFactory").exists()) {
        new InstallTextFileOperation("templates/server/META-INF/services/org.apache.axis.EngineConfigurationFactory", "META-INF/services/org.apache.axis.EngineConfigurationFactory", m_serverBundle.getProject(), varSet).run(monitor, workingCopyManager);
      }
      // add libs
      String[] additionalLibs = new String[]{
          "lib/axis/axis-ant.jar",
          "lib/axis/org.apache.axis_1.4.0.jar",
          "lib/axis/org.apache.axis.patch_1.4.0.jar",
          "lib/axis/commons-discovery-0.2.jar",
          "lib/axis/commons-logging-1.0.4.jar",
          "lib/axis/jaxrpc.jar",
          "lib/axis/log4j-1.2.8.jar",
          "lib/axis/saaj.jar",
          "lib/axis/wsdl4j-1.5.1.jar"
          };
      for (String s : additionalLibs) {
        if (!m_serverBundle.getProject().getFile(s).exists()) {
          new InstallBinaryFileOperation("templates/server/" + s, s, m_serverBundle.getProject()).run(monitor, workingCopyManager);
        }
      }
      PdeUtility.addBundleClasspaths(m_serverBundle.getProject(), additionalLibs);
      PdeUtility.addProjectClasspaths(m_serverBundle.getProject(), additionalLibs);
      // add java files
      new InstallJavaFileOperation("templates/server/src/AxisEngineConfigurationFactory.java", "AxisEngineConfigurationFactory.java", m_serverBundle, varSet).run(monitor, workingCopyManager);
      // add libs and log4j.properties to build.properties
      PdeUtility.addBuildPropertiesFiles(m_serverBundle.getProject(), BuildProperties.PROP_BIN_INCLUDES, additionalLibs);
      PdeUtility.addBuildPropertiesFiles(m_serverBundle.getProject(), BuildProperties.PROP_BIN_INCLUDES, new String[]{"log4j.properties"});
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

}
