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
package org.eclipse.scout.sdk.ui.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.axis.AxisWebServiceServerSetupOperation;
import org.eclipse.scout.sdk.operation.axis.JavaToWsdlOperation;
import org.eclipse.scout.sdk.operation.axis.WsdlToJavaOperation;
import org.eclipse.scout.sdk.pde.BuildProperties;
import org.eclipse.scout.sdk.pde.PdeUtility;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.axis.util.FileListDialog;
import org.eclipse.scout.sdk.util.ApacheAxisUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.widgets.Shell;

public class AxisWebServiceProviderPublishAction extends Action {

  private final Shell m_parentShell;
  private final IType m_implementationType;
  private final IType m_interfaceType;

  public AxisWebServiceProviderPublishAction(Shell parentShell, IType implementationType, IType interfaceType) {
    m_parentShell = parentShell;
    m_implementationType = implementationType;
    m_interfaceType = interfaceType;
    setText("Publish as WebService...");
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceAdd));
  }

  @Override
  public void run() {
    try {
      IScoutBundle implementationScoutBundle = ScoutSdk.getScoutWorkspace().getScoutBundle(getImplementationType().getJavaProject().getProject());

      AxisWebServiceServerSetupOperation setupOp = new AxisWebServiceServerSetupOperation(implementationScoutBundle);
      OperationJob setupJob = new OperationJob(setupOp);
      setupJob.schedule();
      try {
        setupJob.join();
      }
      catch (InterruptedException e) {
        ScoutSdkUi.logWarning("interrupted by waiting for setting up the web service publish operation.", e);
      }
      //
      final IProject project = implementationScoutBundle.getProject();
      String packageName = getImplementationType().getPackageFragment().getElementName();
      String serviceName = getImplementationType().getElementName();
      File tmpDir = null;
      try {
        // wsdl 2 java
        tmpDir = File.createTempFile("wsdl2java", "");
        tmpDir.delete();
        tmpDir.mkdirs();
        File wsdlFile = new File(tmpDir, serviceName + ".wsdl");
        wsdlFile.deleteOnExit();
        File sourceDir = new File(tmpDir, "src");
        sourceDir.mkdirs();
        String publishWsdlUrl = "http://localhost:8080/bsicrm/services/" + serviceName.toLowerCase();

        JavaToWsdlOperation java2wsdlOp = new JavaToWsdlOperation();
        java2wsdlOp.setNamespace("http://" + ApacheAxisUtility.reversePackageName(packageName));
        java2wsdlOp.setProject(project);
        java2wsdlOp.setPublishUrl(publishWsdlUrl);
        java2wsdlOp.setServiceClassname(packageName + "." + serviceName);
        java2wsdlOp.setWsdlAbsolutePath(wsdlFile.getAbsolutePath());
        OperationJob java2wsdlJob = new OperationJob(java2wsdlOp);
        java2wsdlJob.schedule();
        try {
          java2wsdlJob.join();
        }
        catch (InterruptedException e) {
          ScoutSdkUi.logWarning("java to wsdl interrupted.", e);
        }

        WsdlToJavaOperation wsdl2java = new WsdlToJavaOperation();
        wsdl2java.setImplClassQName(getImplementationType().getFullyQualifiedName());
        wsdl2java.setProject(project);
        wsdl2java.setSourceDir(sourceDir);
        wsdl2java.setWsdlUri(wsdlFile.toURI());
        OperationJob wsdl2JavaJob = new OperationJob(wsdl2java);
        wsdl2JavaJob.schedule();
        try {
          wsdl2JavaJob.join();
        }
        catch (InterruptedException e) {
          ScoutSdkUi.logWarning("wsdl to java interrupted.", e);
        }
        // import generated files if necessary
        final List<IFile> inputList = new ArrayList<IFile>();
        HashMap<IPath, File> pathToTmpFile = new HashMap<IPath, File>();
        String deployPackagePath = null;
        for (File f : IOUtility.listFilesInSubtree(sourceDir, null)) {
          String pckName = ApacheAxisUtility.createPackageName(sourceDir, f);
          String pckPath = ApacheAxisUtility.createPackagePath(pckName);
          if (f.getName().equalsIgnoreCase("deploy.wsdd")) {
            deployPackagePath = pckPath;
            IFile ifile = project.getFile("src/" + pckPath + "/" + serviceName + "-deploy.wsdd");
            inputList.add(ifile);
            pathToTmpFile.put(ifile.getFullPath(), f);
          }
          else if (f.getName().equalsIgnoreCase("undeploy.wsdd")) {
            deployPackagePath = pckPath;
            IFile ifile = project.getFile("src/" + pckPath + "/" + serviceName + "-undeploy.wsdd");
            inputList.add(ifile);
            pathToTmpFile.put(ifile.getFullPath(), f);
          }
          else if (f.getName().endsWith(".java")) {
            if (pckName.startsWith(implementationScoutBundle.getScoutProject().getProjectName())) {
              // ignore
            }
            else if (pckName.startsWith("com.bsiag.")) {
              // ignore
            }
            else {
              IType type = ScoutSdk.getType(pckName + "." + f.getName().substring(0, f.getName().length() - 5));
              if (!TypeUtility.exists(type)) {
                IFile ifile = project.getFile("src/" + pckPath + "/" + f.getName());
                inputList.add(ifile);
                pathToTmpFile.put(ifile.getFullPath(), f);
              }
            }
          }
        }
        if (deployPackagePath == null) {
          // failure
          throw new CoreException(new ScoutStatus("Could not find neither a generated deploy.wsdd nor undeploy.wsdd"));
        }

        IFile deployFile = project.getFile("src/" + deployPackagePath + "/" + serviceName + "-deploy.wsdd");
        IFile contentDescriptorFile = project.getFile("src/" + deployPackagePath + "/" + serviceName + "-server.properties");
        // ask user
        List<IFile> outputList = FileListDialog.showThreadSafe(
            "Publish Webservice",
            "Importing generated source files",
            "The following source files will be added (if checked) to the project " + project.getName() + ". Existing files are being overwritten. Please review this list.",
            inputList
            );
        if (outputList == null) {
          // user cancelled
          throw new CoreException(new ScoutStatus("Publishing cancelled", new InterruptedException()));
        }
        // create a content description file for this service
        StringBuilder buf = new StringBuilder();
        int fileIndex = 0;
        for (IFile ifile : outputList) {
          if (ifile.getName().endsWith(".java")) {
            IPath path = ifile.getFullPath();
            path = path.removeFirstSegments(path.matchingFirstSegments(project.getFullPath()));
            buf.append("file" + fileIndex + "=" + path.toString() + "\n");
            fileIndex++;
          }
        }
        PdeUtility.createFile(contentDescriptorFile, new ByteArrayInputStream(buf.toString().getBytes()), new NullProgressMonitor());
        // copy files
        for (IFile ifile : outputList) {
          File tmpFile = pathToTmpFile.get(ifile.getFullPath());
          FileInputStream in = new FileInputStream(tmpFile);
          try {
            PdeUtility.createFile(ifile, in, new NullProgressMonitor());
          }
          finally {
            in.close();
          }
        }
        // add server-config.wsdd to build.properties
        PdeUtility.addBuildPropertiesFiles(project, BuildProperties.PROP_BIN_INCLUDES, new String[]{"server-config.wsdd"});
        ApacheAxisUtility.runDeploy(deployFile.getLocation().toFile(), "server", project, new NullProgressMonitor());
      }
      finally {
        // refresh project
        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        // clear tmp folder
        if (tmpDir != null) IOUtility.deleteDirectory(tmpDir);
      }
      //
    }
    catch (Exception e) {
      ScoutSdkUi.logError(e);
    }
  }

  public IType getImplementationType() {
    return m_implementationType;
  }

  public IType getInterfaceType() {
    return m_interfaceType;
  }
}
