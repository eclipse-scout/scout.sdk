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
package org.eclipse.scout.sdk.operation;

import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.scout.commons.ListUtility;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.pde.ClasspathEntry;
import org.eclipse.scout.sdk.pde.ClasspathXml;
import org.eclipse.scout.sdk.pde.ProductXml;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

@SuppressWarnings("restriction")
public class ProductLaunchOperation implements IOperation {

  private IFile m_productFile;
  private IFile m_configIniFile;
  private boolean m_debugMode = false;
  private final String m_name;

  public ProductLaunchOperation(String name) {
    m_name = name;

  }

  @Override
  public String getOperationName() {
    return "launching" + m_name + "...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_productFile == null || !m_productFile.exists()) {
      throw new IllegalArgumentException("product file does not exist.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    WorkspaceProductModel productModel = new WorkspaceProductModel(m_productFile, false);
    productModel.load();

    if (m_configIniFile == null) {
      m_configIniFile = getProductFile().getParent().getFile(new Path("config.ini"));
    }
    HashSet<String> allWorkspacePlugins = new HashSet<String>();
    for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      allWorkspacePlugins.add(project.getName());
    }
    HashSet<String> selectedTargetPlugins = new HashSet<String>();
    HashSet<String> selectedWorkspacePlugins = new HashSet<String>();
    ProductXml xml = new ProductXml(getProductFile());
    for (String s : xml.getPlugins()) {
      if (allWorkspacePlugins.contains(s)) {
        selectedWorkspacePlugins.add(s);
      }
      else {
        selectedTargetPlugins.add(s);
      }
    }
    String selectedTargetPluginsText = ListUtility.format(selectedTargetPlugins, ",");
    String selectedWorkspacePluginsText = ListUtility.format(selectedWorkspacePlugins, ",");
    // see if product file defines an execution environment
    String execEnv = xml.getVm();
    if (execEnv == null) {
      // see if .classpath file defines an execution environment
      ClasspathXml cpXml = new ClasspathXml(getProductFile().getProject());
      ClasspathEntry cpEntry = cpXml.getEntryByPathPrefix(ClasspathEntry.KIND_CON, IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH);
      if (cpEntry != null) {
        execEnv = cpEntry.getPath();
      }
    }
    //
    try {
      ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
      ILaunchConfigurationType type = lm.getLaunchConfigurationType("org.eclipse.pde.ui.RuntimeWorkbench");
      String launchName = "Scout - " + getProductFile().getName();
      ILaunchConfigurationWorkingCopy w = type.newInstance(null, launchName);
      w.setAttribute("append.args", true);
      w.setAttribute("askclear", true);
      w.setAttribute("automaticAdd", false);
      w.setAttribute("automaticValidate", true);
      w.setAttribute("clearws", false);
      w.setAttribute("clearwslog", false);
      w.setAttribute("clearConfig", true);
      w.setAttribute("default", false);
      w.setAttribute("location", "${workspace_loc}/../runtime-" + getProductFile().getName());
      if (execEnv != null) {
        // jre container example: "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6"
        w.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, execEnv);
      }
      // w.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, " -console -consoleLog");
      w.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.pde.ui.workbenchClasspathProvider");
      // w.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, StringUtility.emptyIfNull(xml.getVmArgs()).replaceAll("[\n\r]+", " "));
      w.setAttribute("pde.version", "3.3");
      w.setAttribute("product", xml.getProductId());
      w.setAttribute("productFile", xml.getFile().getFullPath().toOSString());
      w.setAttribute("selected_target_plugins", selectedTargetPluginsText);
      w.setAttribute("selected_workspace_plugins", selectedWorkspacePluginsText);
      if (m_configIniFile != null && m_configIniFile.exists()) {
        w.setAttribute("templateConfig", m_configIniFile.getLocation().toOSString());
        w.setAttribute("useDefaultConfig", false);
      }
      else {
        w.setAttribute("useDefaultConfig", true);
      }
      w.setAttribute("usefeatures", false);
      w.setAttribute("useProduct", true);
      // stop old
      for (ILaunch l : lm.getLaunches()) {
        ILaunchConfiguration lc = l.getLaunchConfiguration();
        if (launchName.equals(lc.getName())) {
          l.terminate();
          for (int i = 0; i < 50; i++) {
            if (l.isTerminated()) {
              break;
            }
            else {
              Thread.sleep(100);
            }
          }
          break;
        }
      }
      // start
      ILaunchConfiguration lc = w.doSave();
      lc.launch(isDebugMode() ? ILaunchManager.DEBUG_MODE : ILaunchManager.RUN_MODE, monitor, true, true);
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  public void setProductFile(IFile productFile) {
    m_productFile = productFile;
  }

  public IFile getProductFile() {
    return m_productFile;
  }

  public void setConfigIniFile(IFile configIniFile) {
    m_configIniFile = configIniFile;
  }

  public IFile getConfigIniFile() {
    return m_configIniFile;
  }

  public void setDebugMode(boolean debugMode) {
    m_debugMode = debugMode;
  }

  public boolean isDebugMode() {
    return m_debugMode;
  }

}
