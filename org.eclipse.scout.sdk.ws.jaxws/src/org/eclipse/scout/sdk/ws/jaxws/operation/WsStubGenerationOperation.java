/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.operation;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.preferences.IPreferenceConstants;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IOperationFinishedListener;

@SuppressWarnings("restriction")
public class WsStubGenerationOperation implements IOperation {

  private IScoutBundle m_bundle;
  private String m_alias;
  private String m_wsdlFileName;
  private Map<String, List<String>> m_properties;
  private IFolder m_wsdlFolder;

  private Set<IOperationFinishedListener> m_operationFinishedListeners;

  public WsStubGenerationOperation() {
    m_operationFinishedListeners = new HashSet<>();
  }

  public void addOperationFinishedListener(IOperationFinishedListener listener) {
    m_operationFinishedListeners.add(listener);
  }

  public void removeOperationFinishedListener(IOperationFinishedListener listener) {
    m_operationFinishedListeners.remove(listener);
  }

  @Override
  public void validate() {
    if (m_bundle == null) {
      throw new IllegalArgumentException("bundle not set");
    }
    if (m_alias == null) {
      throw new IllegalArgumentException("alias not set");
    }
    if (m_wsdlFolder == null || !m_wsdlFolder.exists()) {
      throw new IllegalArgumentException("wsdl folder not set");
    }
    if (m_wsdlFileName == null) {
      throw new IllegalArgumentException("wsdlFileName not set");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JaxWsSdk.getDefault().getMarkerQueueManager().suspend();
    try {
      // ensure stub folder created
      JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.STUB_FOLDER, true);

      // launch JAX-WS Stub generator
      String launcherName = m_alias + " (JAX-WS stub generation)";

      // ensure WSDL folder to be registered in build.xml
      JaxWsSdkUtility.ensureFolderAccessibleAndRegistered(getWsdlFolder(), true);

      IFile jarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, m_properties, m_wsdlFileName);
      // remove JAR file to ensure successful JAR generation
      if (jarFile.exists()) {
        try {
          jarFile.delete(true, monitor);
          JaxWsSdkUtility.registerJarLib(m_bundle, jarFile, true, monitor);
        }
        catch (Exception e) {
          JaxWsSdk.logError("failed to delete jar file", e);
          notifyListeners(false, new Exception("Failed to delete stub JAR-file '" + jarFile.getProjectRelativePath().toString() + "'. The file might be locked because the server is running.", e));
          return;
        }
      }

      // assemble arguments to generate stub
      String args = null;
      String jarFilePath = PathNormalizer.toJarPath(jarFile.getProjectRelativePath().toString());
      args = StringUtility.join("\n", args, jarFilePath);
      args = StringUtility.join("\n", args, getWsdlFolder().getProjectRelativePath().append(m_wsdlFileName).toString());
      args = StringUtility.join("\n", args, "1"); // apply patches

      if (m_properties != null && m_properties.size() > 0) {
        for (Entry<String, List<String>> property : m_properties.entrySet()) {
          String propName = property.getKey();
          if (!StringUtility.hasText(propName)) {
            continue;
          }
          // skip non JAX-WS options
          if (propName.equals(JaxWsConstants.OPTION_JAR)) {
            continue;
          }

          List<String> propValues = property.getValue();
          if (propValues == null || propValues.size() == 0) {
            String directive = propName;
            args = StringUtility.join("\n", args, directive);
          }
          else {
            for (String value : propValues) {
              // escape paths containing whitespace characters
              if ("b".equals(propName) && StringUtility.hasText(value) && value.matches(".*\\s.*") && !value.startsWith("\\")) {
                value = "\"" + value + "\"";
              }
              String directive = StringUtility.join("=", propName, value);
              args = StringUtility.join("\n", args, directive);
            }
          }
        }
      }

      // run stub generation
      Throwable exception = null;
      try {
        launchJavaApplicationSync(launcherName,
            m_bundle.getProject().getName(),
            TypeUtility.getType(JaxWsRuntimeClasses.JaxWsStubGenerator).getFullyQualifiedName(),
            args,
            monitor);
      }
      catch (Exception e) {
        exception = e;
      }
      finally {
        // refresh folder to be reflected in SDK
        JaxWsSdkUtility.refreshLocal(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.STUB_FOLDER, false), IResource.DEPTH_INFINITE);
      }

      if (exception == null) {
        // check whether artifacts were created. This is necessary as not all failures are propagated
        jarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, m_properties, m_wsdlFileName);
        if (JaxWsSdkUtility.exists(jarFile)) {
          // register stub JAR
          if (!JaxWsSdkUtility.registerJarLib(m_bundle, jarFile, false, monitor)) {
            exception = new Exception("Stub generation was successfull but registration of JAR file '" + jarFilePath + "' failed in .classpath / MANIFEST.MF / build.properties");
          }
        }
        else {
          exception = new CoreException(new ScoutStatus("Failed to generate webservice artifacts. Probably a corrupt WSDL file, non-standardized WSDL file or a binding file problem."));
        }
      }

      if (exception == null) {
        notifyListeners(true, null);
      }
      else {
        notifyListeners(false, exception);
      }
    }
    finally {
      JaxWsSdk.getDefault().getMarkerQueueManager().resume();
    }
  }

  @Override
  public String getOperationName() {
    return WsStubGenerationOperation.class.getName();
  }

  private void launchJavaApplicationSync(String launchName, String projectName, String javaMainTypeName, String argumentList, IProgressMonitor monitor) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
    ILaunchConfigurationWorkingCopy wc = type.newInstance(null, launchName);
    wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, javaMainTypeName);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, argumentList);
    wc.setAttribute(org.eclipse.debug.internal.core.LaunchConfiguration.ATTR_MAPPED_RESOURCE_TYPES, Arrays.asList(new String[]{Integer.toString(IResource.PROJECT)}));
    wc.setAttribute(org.eclipse.debug.internal.core.LaunchConfiguration.ATTR_MAPPED_RESOURCE_PATHS, Arrays.asList(new String[]{"/" + projectName}));
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, getClasspathEntries());
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
    wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
    ILaunchConfiguration lc = wc.doSave();

    String mode;
    boolean debugMode = JaxWsSdk.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.PREF_STUB_GENERATION_DEBUG_MODE);
    if (debugMode) {
      mode = ILaunchManager.DEBUG_MODE;
    }
    else {
      mode = ILaunchManager.RUN_MODE;
    }

    String continueWithCompileErros = DebugUIPlugin.getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR);
    boolean changedDebugPref = false;
    try {
      if (!CompareUtility.equals(MessageDialogWithToggle.ALWAYS, continueWithCompileErros)) {
        // prevent PDE dialog asking whether to continue in case of compile errors
        DebugUIPlugin.getDefault().getPreferenceStore().setValue(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, MessageDialogWithToggle.ALWAYS);
        changedDebugPref = true;
      }
      ILaunch launch = lc.launch(mode, monitor, true, true);

      // wait for stub generation to be finished
      while (!launch.isTerminated()) {
        if (monitor.isCanceled()) {
          launch.terminate();
          break;
        }
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
        }
      }

      boolean keepLaunchConfiguration = JaxWsSdk.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.PREF_STUB_GENERATION_KEEP_LAUNCH_CONFIG);
      if (!keepLaunchConfiguration) {
        lc.delete();
      }

      // validate exit code of processes
      for (IProcess process : launch.getProcesses()) {
        if (process.getExitValue() != 0) {
          throw new CoreException(new ScoutStatus("Stub generation failed."));
        }
      }
    }
    finally {
      if (changedDebugPref) {
        DebugUIPlugin.getDefault().getPreferenceStore().setValue(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, continueWithCompileErros);
      }
    }
  }

  private void notifyListeners(boolean success, Throwable exception) {
    for (IOperationFinishedListener listener : m_operationFinishedListeners.toArray(new IOperationFinishedListener[m_operationFinishedListeners.size()])) {
      try {
        listener.operationFinished(success, exception);
      }
      catch (Exception e) {
        JaxWsSdk.logError("failed to notify listener", e);
      }
    }
  }

  private List<String> getClasspathEntries() throws CoreException {
    List<String> mementoList = new LinkedList<>();

    // add runtime classes to the classpath
    for (IRuntimeClasspathEntry classpathEntry : JavaRuntime.computeUnresolvedRuntimeClasspath(m_bundle.getJavaProject())) {
      mementoList.add(classpathEntry.getMemento());
    }

    // ensure that the file 'WsImport' is on classpath (required to build the webservice)
    IType wsImportType = TypeUtility.getType(JaxWsRuntimeClasses.WsImportType);
    if (wsImportType == null || !wsImportType.exists() || !TypeUtility.isOnClasspath(wsImportType, m_bundle.getJavaProject())) {
      // manually add the 'tools.jar' to the classpath of the launch-configuration
      File toolsJarFile = findToolsJarInVm();
      if (toolsJarFile == null || !toolsJarFile.exists() || !toolsJarFile.isFile()) {
        throw new CoreException(new ScoutStatus("Failed to locate the Java library 'tools.jar'. Please ensure the 'tools.jar' to be on the classpath or in the project's JDK."));
      }
      mementoList.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(toolsJarFile.getAbsolutePath())).getMemento());
    }

    return mementoList;
  }

  /**
   * Tries to find the 'tools.jar' Java library. The precedence of the search is as following:
   * <ol>
   * <li>project VM</li>
   * <li>default VM</li>
   * <li>Eclipse IDE VM</li>
   * </ol>
   *
   * @return the 'tools.jar' or <code>null</code> if not found.
   */
  private File findToolsJarInVm() {
    // Project VM
    IJavaProject project = m_bundle.getJavaProject();
    try {
      IVMInstall projectVm = JavaRuntime.getVMInstall(project);
      File toolsJar = findToolsJarInVm(projectVm.getInstallLocation());
      if (toolsJar != null) {
        return toolsJar;
      }
    }
    catch (CoreException e) {
      JaxWsSdk.logError(String.format("Failed to find the 'tools.jar' in the JDK that is assigned to the project '%s'", m_bundle.getProject().getName()), e);
    }

    // Default VM
    IVMInstall defaultVm = JavaRuntime.getDefaultVMInstall();
    File toolsJar = findToolsJarInVm(defaultVm.getInstallLocation());
    if (toolsJar != null) {
      return toolsJar;
    }

    // Eclipse IDE VM
    return findToolsJarInVm(new File(System.getProperty("java.home")));
  }

  private File findToolsJarInVm(File vmRootFolder) {
    if (vmRootFolder == null) {
      return null;
    }
    if ("jre".equalsIgnoreCase(vmRootFolder.getName())) {
      vmRootFolder = vmRootFolder.getParentFile();
    }

    File toolsJar = new File(vmRootFolder, "lib/tools.jar");
    if (toolsJar.exists()) {
      return toolsJar;
    }
    return null;
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public String getAlias() {
    return m_alias;
  }

  public void setAlias(String alias) {
    m_alias = alias;
  }

  public String getWsdlFileName() {
    return m_wsdlFileName;
  }

  public IFolder getWsdlFolder() {
    return m_wsdlFolder;
  }

  public void setWsdlFolder(IFolder wsdlFolder) {
    m_wsdlFolder = wsdlFolder;
  }

  public void setWsdlFileName(String wsdlFileName) {
    m_wsdlFileName = wsdlFileName;
  }

  public void setProperties(Map<String, List<String>> properties) {
    m_properties = properties;
  }
}
