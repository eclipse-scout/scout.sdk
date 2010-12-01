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
package org.eclipse.scout.sdk.ui.action.delete;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.sdk.NamingUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.axis.WebServiceUndeployOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.FileSelectionDialog;
import org.eclipse.scout.sdk.util.ApacheAxisUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.undo.DeleteResourcesOperation;

public class AxisWebServiceUndeployAction extends Action {

  private final Shell m_parentShell;
  private final IScoutBundle m_serverBundle;
  private final IType m_implementationType;

  public AxisWebServiceUndeployAction(Shell parentShell, String text, IScoutBundle serverBundle, IType implementationType) {
    m_parentShell = parentShell;
    m_serverBundle = serverBundle;
    m_implementationType = implementationType;
    setText(text);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceRemove));
  }

  @Override
  public void run() {

    try {
      IProject project = getServerBundle().getProject();
      String packageName = getImplementationType().getPackageFragment().getElementName();
      String serviceName = getImplementationType().getElementName();
      //
      String deployPackagePath = ApacheAxisUtility.createPackagePath(packageName);
      IFile deployFile = project.getFile("src/" + deployPackagePath + "/" + serviceName + "-deploy.wsdd");
      IFile undeployFile = project.getFile("src/" + deployPackagePath + "/" + serviceName + "-undeploy.wsdd");
      IFile contentDescriptorFile = project.getFile("src/" + deployPackagePath + "/" + serviceName + "-server.properties");
      // collect files and ask user
      ArrayList<IFile> inputList = new ArrayList<IFile>();
      if (contentDescriptorFile.exists()) {
        inputList.add(contentDescriptorFile);
        String[] lines;
        try {
          lines = new String(IOUtility.getContent(contentDescriptorFile.getContents(true))).split("[\\n\\r]+");
        }
        catch (ProcessingException e) {
          throw new CoreException(new ScoutStatus(e));
        }
        Pattern pat = Pattern.compile("file[0-9]+=(.*)");
        for (String line : lines) {
          line = line.trim();
          Matcher m = pat.matcher(line);
          if (m.matches()) {
            IFile ifile = project.getFile(m.group(1));
            if (ifile.exists()) {
              inputList.add(ifile);
            }
            else {
              // maybe the file moved
              if (ifile.getName().endsWith(".java")) {
                for (IType bctype : ScoutSdk.resolveTypes(NamingUtility.removeSuffixes(ifile.getName(), ".java"))) {
                  IResource res = bctype.getCompilationUnit().getResource();
                  if (res != null && res.getType() == IResource.FILE && project.exists(res.getProjectRelativePath())) {
                    inputList.add((IFile) res);
                  }
                }
              }
            }
          }
        }
      }
      if (deployFile.exists()) {
        inputList.add(deployFile);
      }
      if (undeployFile.exists()) {
        inputList.add(undeployFile);
      }
      FileSelectionDialog dialog = new FileSelectionDialog(m_parentShell, "Remove Webservice Client", "Deleting the following source files");
      dialog.setFiles(inputList.toArray(new IFile[inputList.size()]));
      dialog.setSelectedFiles(inputList.toArray(new IFile[inputList.size()]));
      dialog.addPropertyChangeListener(new P_FileDialogPropertyListener(dialog));
      if (dialog.open() == IDialogConstants.OK_ID) {
        if (undeployFile != null && undeployFile.exists()) {
          WebServiceUndeployOperation undeployOp = new WebServiceUndeployOperation();
          undeployOp.setProject(getServerBundle().getProject());
          undeployOp.setRole("server");
          undeployOp.setUndeployFile(undeployFile);
          OperationJob undeployJob = new OperationJob(undeployOp);
          undeployJob.schedule();
          try {
            undeployJob.join();
          }
          catch (InterruptedException e) {
            ScoutSdkUi.logWarning("could not wait for undeploy '" + undeployFile.getFullPath() + "' job.", e);
          }
        }
        IFile[] outputFiles = dialog.getSelectedFiles();
        if (outputFiles.length > 0) {
          P_ResourceDeleteOperation deleteFilesOp = new P_ResourceDeleteOperation(outputFiles);
          OperationJob deleteFilesJob = new OperationJob(deleteFilesOp);
          deleteFilesJob.schedule();
          try {
            deleteFilesJob.join();
          }
          catch (InterruptedException e) {
            ScoutSdkUi.logWarning("could not wait for files delete job.", e);
          }
        }
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("web service undeploy failed.", e);
    }
  }

  public IScoutBundle getServerBundle() {
    return m_serverBundle;
  }

  public IType getImplementationType() {
    return m_implementationType;
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
  } // end class P_FileDialogPropertyListener

  private class P_ResourceDeleteOperation implements IOperation {
    private final IFile[] m_files;

    public P_ResourceDeleteOperation(IFile[] files) {
      m_files = files;

    }

    @Override
    public String getOperationName() {
      return "Delete resources... ";
    }

    @Override
    public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      try {
        new DeleteResourcesOperation(m_files, "Delete wsdl Files...", true).execute(monitor, null);
      }
      catch (ExecutionException e) {
        ScoutSdkUi.logError("Could not delete wsdl files.", e);
      }
      finally {
        getServerBundle().getProject().refreshLocal(IResource.DEPTH_ONE, monitor);
      }
    }

    @Override
    public void validate() throws IllegalArgumentException {
      // TODO Auto-generated method stub

    }
  } // P_ResourceDeleteOperation
}
