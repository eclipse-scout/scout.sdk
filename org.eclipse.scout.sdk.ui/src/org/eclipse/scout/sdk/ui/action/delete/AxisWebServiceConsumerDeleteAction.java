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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.sdk.NamingUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.axis.AxisWebServiceConsumerDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.FileSelectionDialog;
import org.eclipse.scout.sdk.util.ApacheAxisUtility;
import org.eclipse.swt.widgets.Shell;

public class AxisWebServiceConsumerDeleteAction extends Action {
  private IType m_serviceLocator;
  private final Shell m_parentShell;

  public AxisWebServiceConsumerDeleteAction(Shell parentShell) {
    m_parentShell = parentShell;
    setText("Delete...");
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_DELETE));
  }

  @Override
  public void run() {
    try {
      // collect files
      String deployPackagePath = ApacheAxisUtility.createPackagePath(getServiceLocator().getPackageFragment().getElementName());
      IProject serverProject = getServiceLocator().getJavaProject().getProject();
      IFile deployFile = serverProject.getFile("src/" + deployPackagePath + "/deploy.wsdd");
      IFile undeployFile = serverProject.getFile("src/" + deployPackagePath + "/undeploy.wsdd");
      IFile contentDescriptorFile = serverProject.getFile("src/" + deployPackagePath + "/client.properties");
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
            IFile ifile = serverProject.getFile(m.group(1));
            if (ifile.exists()) {
              inputList.add(ifile);
            }
            else {
              // maybe the file moved
              if (ifile.getName().endsWith(".java")) {
                for (IType type : ScoutSdk.resolveTypes(NamingUtility.removeSuffixes(ifile.getName(), ".java"))) {
                  IResource res = type.getCompilationUnit().getResource();
                  if (res != null && res.getType() == IResource.FILE && serverProject.exists(res.getProjectRelativePath())) {
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
      // ask user
      FileSelectionDialog dialog = new FileSelectionDialog(m_parentShell, "Remove Webservice Client", "Deleting the following source files");
      dialog.setFiles(inputList.toArray(new IFile[inputList.size()]));
      dialog.setSelectedFiles(inputList.toArray(new IFile[inputList.size()]));
      dialog.addPropertyChangeListener(new P_FileDialogPropertyListener(dialog));
      if (dialog.open() == IDialogConstants.OK_ID) {
        IFile[] outputFiles = dialog.getSelectedFiles();
        if (outputFiles.length > 0) {
          AxisWebServiceConsumerDeleteOperation delOp = new AxisWebServiceConsumerDeleteOperation();
          delOp.setFiles(outputFiles);
          delOp.setUndeployFile(undeployFile);
          delOp.setServerProject(getServiceLocator().getJavaProject().getProject());
          new OperationJob(delOp).schedule();
        }
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("Error during executing '" + AxisWebServiceConsumerDeleteAction.class.getName() + "'.", e);
    }

  }

  public void setServiceLocator(IType serviceLocator) {
    m_serviceLocator = serviceLocator;
  }

  public IType getServiceLocator() {
    return m_serviceLocator;
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
