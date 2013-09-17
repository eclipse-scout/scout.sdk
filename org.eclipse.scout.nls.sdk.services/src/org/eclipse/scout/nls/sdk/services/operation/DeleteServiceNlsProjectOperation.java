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
package org.eclipse.scout.nls.sdk.services.operation;

import java.util.LinkedList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.service.ServiceDeleteOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class DeleteServiceNlsProjectOperation implements IOperation {

  private final IType m_serviceClass;
  private final IScoutBundle m_scoutBundle;

  public DeleteServiceNlsProjectOperation(IType serviceType, IScoutBundle bundle) {
    m_serviceClass = serviceType;
    m_scoutBundle = bundle;
  }

  @Override
  public String getOperationName() {
    return "Delete NLS Service Project";
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    final NlsServiceType t = new NlsServiceType(getServiceClass());
    if (t.getJavaProject() != null && t.getTranslationsFolderName() != null) {
      IProject p = t.getJavaProject().getProject();
      final IFolder propertiesFolder = p.getFolder(t.getTranslationsFolderName());

      // ensure sync
      propertiesFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);

      // collect resources to delete
      final LinkedList<IResource> filesToDelete = new LinkedList<IResource>();
      if (propertiesFolder.exists()) {
        final boolean[] otherFilesExistInFolder = {false};
        propertiesFolder.accept(new IResourceVisitor() {
          @Override
          public boolean visit(IResource resource) throws CoreException {
            if (resource == propertiesFolder) {
              return true;
            }
            else {
              if (resource.getType() == IResource.FILE) {
                if (resource.getName().startsWith(t.getTranslationsPrefix())) {
                  filesToDelete.add(resource);
                }
                else {
                  otherFilesExistInFolder[0] = true;
                }
              }
              return false;
            }
          }
        }, IResource.DEPTH_ONE, IResource.NONE);

        if (!otherFilesExistInFolder[0]) {
          filesToDelete.add(propertiesFolder);
        }
      }

      ServiceDeleteOperation del = new ServiceDeleteOperation();
      del.setServiceImplementation(getServiceClass());
      del.setAdditionalResourcesToBeDeleted(filesToDelete.toArray(new IResource[filesToDelete.size()]));
      del.validate();
      del.run(monitor, workingCopyManager);
    }
    else {
      NlsCore.logWarning("Invalid Text Provider Service to be deleted. Cannot parse the resources.");
    }
  }

  public IScoutBundle getScoutBundle() {
    return m_scoutBundle;
  }

  public IType getServiceClass() {
    return m_serviceClass;
  }
}
