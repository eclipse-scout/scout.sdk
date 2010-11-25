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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.axis.AxisWebServiceClientSetupOperation;
import org.eclipse.scout.sdk.operation.axis.AxisWebServiceConsumerNewOperation;
import org.eclipse.scout.sdk.operation.axis.WsdlToJavaOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.AxisWebServiceConsumerDialog;
import org.eclipse.scout.sdk.ui.dialog.FileSelectionDialog;
import org.eclipse.scout.sdk.util.ApacheAxisUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.widgets.Shell;

public class AxisWebServiceConsumerNewAction extends Action {

  private final IScoutBundle m_serverBundle;
  private String m_wsdlUrl;
  private final Shell m_parentShell;

  public AxisWebServiceConsumerNewAction(Shell parentShell, IScoutBundle serverBundle) {
    m_parentShell = parentShell;
    m_serverBundle = serverBundle;
    setText("New Webservice Consumer...");
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_ADD));
  }

  @Override
  public void run() {
    AxisWebServiceConsumerDialog wsdlDialog = new AxisWebServiceConsumerDialog(m_parentShell);
    wsdlDialog.setWsdlUrl(getWsdlUrl());
    if (wsdlDialog.open() == IDialogConstants.OK_ID) {
      String username = wsdlDialog.getUsername();
      setWsdlUrl(wsdlDialog.getWsdlUrl());
      String password = wsdlDialog.getPassword();

      try {
        AxisWebServiceClientSetupOperation setupOp = new AxisWebServiceClientSetupOperation(getServerBundle());
        OperationJob setupJob = new OperationJob(setupOp);
        setupJob.schedule();
        setupJob.join();
        // create stubs
        URL wsdlUrl = new URL(getWsdlUrl());
        //
        File tmpDir = null;
        try {
          // wsdl 2 java
          tmpDir = File.createTempFile("wsdl2java", "");
          tmpDir.delete();
          tmpDir.mkdirs();
          File sourceDir = new File(tmpDir, "src");
          sourceDir.mkdirs();

          WsdlToJavaOperation wsdlToJavaOp = new WsdlToJavaOperation();
          wsdlToJavaOp.setWsdlUri(wsdlUrl.toURI());
          wsdlToJavaOp.setProject(getServerBundle().getProject());
          wsdlToJavaOp.setSourceDir(sourceDir);
          wsdlToJavaOp.setUsername(username);
          wsdlToJavaOp.setPassword(password);
          OperationJob wsdlToJavaJob = new OperationJob(wsdlToJavaOp);
          wsdlToJavaJob.schedule();
          wsdlToJavaJob.join();

          // import generated files if necessary
          final List<IFile> inputList = new ArrayList<IFile>();
          HashMap<IPath, File> pathToTmpFile = new HashMap<IPath, File>();
          String deployPackagePath = null;
          for (File f : IOUtility.listFilesInSubtree(sourceDir, null)) {
            String pckName = ApacheAxisUtility.createPackageName(sourceDir, f);
            String pckPath = ApacheAxisUtility.createPackagePath(pckName);
            if (f.getName().equalsIgnoreCase("deploy.wsdd")) {
              deployPackagePath = pckPath;
              IFile ifile = getServerBundle().getProject().getFile("src/" + pckPath + "/" + f.getName());
              inputList.add(ifile);
              pathToTmpFile.put(ifile.getFullPath(), f);
            }
            else if (f.getName().equalsIgnoreCase("undeploy.wsdd")) {
              deployPackagePath = pckPath;
              IFile ifile = getServerBundle().getProject().getFile("src/" + pckPath + "/" + f.getName());
              inputList.add(ifile);
              pathToTmpFile.put(ifile.getFullPath(), f);
            }
            else if (f.getName().endsWith(".java")) {
              if (pckName.startsWith(getServerBundle().getScoutProject().getProjectName())) {
                // accept
                IFile ifile = getServerBundle().getProject().getFile("src/" + pckPath + "/" + f.getName());
                inputList.add(ifile);
                pathToTmpFile.put(ifile.getFullPath(), f);
              }
              else if (pckName.startsWith("com.bsiag.")) {
                // ignore
              }
              else {
                IType type = ScoutSdk.getType(pckName + "." + f.getName().substring(0, f.getName().length() - 5));
                if (!TypeUtility.exists(type)) {
                  // accept
                  IFile ifile = getServerBundle().getProject().getFile("src/" + pckPath + "/" + f.getName());
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
          IFile deployFile = getServerBundle().getProject().getFile("src/" + deployPackagePath + "/deploy.wsdd");
          IFile contentDescriptorFile = getServerBundle().getProject().getFile("src/" + deployPackagePath + "/client.properties");

          FileSelectionDialog dialog = new FileSelectionDialog(m_parentShell, "Create Webservice Client", "Importing generated source files");
          dialog.setFiles(inputList.toArray(new IFile[inputList.size()]));
          dialog.setSelectedFiles(inputList.toArray(new IFile[inputList.size()]));
          dialog.addPropertyChangeListener(new P_FileDialogPropertyListener(dialog));
          if (dialog.open() == IDialogConstants.OK_ID) {
            IFile[] outputFiles = dialog.getSelectedFiles();
            AxisWebServiceConsumerNewOperation newOp = new AxisWebServiceConsumerNewOperation();
            newOp.setContentDescFile(contentDescriptorFile);
            newOp.setDeployFile(deployFile);
            newOp.setFiles(outputFiles);
            newOp.setPathToTempDir(pathToTmpFile);
            newOp.setServerProject(getServerBundle().getProject());
            OperationJob newJob = new OperationJob(newOp);
            newJob.schedule();
            newJob.join();
          }

        }
        finally {
          // clear tmp folder
          if (tmpDir != null) IOUtility.deleteDirectory(tmpDir);
        }
      }
      catch (Exception e) {
        ScoutSdkUi.logError(e);
      }
    }
  }

  public IScoutBundle getServerBundle() {
    return m_serverBundle;
  }

  public String getWsdlUrl() {
    return m_wsdlUrl;
  }

  public void setWsdlUrl(String wsdlUrl) {
    m_wsdlUrl = wsdlUrl;
  }

  private class P_FileDialogPropertyListener implements PropertyChangeListener {
    private final FileSelectionDialog m_dialog;

    private P_FileDialogPropertyListener(FileSelectionDialog dialog) {
      m_dialog = dialog;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(FileSelectionDialog.PROP_SELECTED_FILES)) {
        m_dialog.getOkButton().setEnabled(((IFile[]) evt.getNewValue()).length > 0);
      }
    }
  }

}
