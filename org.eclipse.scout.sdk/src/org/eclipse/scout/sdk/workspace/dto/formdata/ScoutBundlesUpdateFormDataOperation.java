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
package org.eclipse.scout.sdk.workspace.dto.formdata;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link ScoutBundlesUpdateFormDataOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 23.02.2013
 */
public class ScoutBundlesUpdateFormDataOperation implements IOperation {
  private final IScoutBundle m_project;

  public ScoutBundlesUpdateFormDataOperation(IScoutBundle project) {
    m_project = project;
  }

  @Override
  public String getOperationName() {
    return "Update form data of '" + getProject().getSymbolicName() + "'...";
  }

  @Override
  public void validate() {
    if (getProject() == null) {
      throw new IllegalArgumentException("Scout project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    processProject(monitor, workingCopyManager);
  }

  private void processProject(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    Set<IScoutBundle> childClientBundles = getProject().getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), true);
    for (IScoutBundle client : childClientBundles) {
      ClientBundleUpdateFormDataOperation updateOp = new ClientBundleUpdateFormDataOperation(client);
      updateOp.validate();
      try {
        updateOp.run(monitor, workingCopyManager);
      }
      catch (Exception e) {
        ScoutSdk.logError("could not update form data of bundle '" + client.getSymbolicName() + "'.", e);
      }
    }
  }

  /**
   * @return the project
   */
  public IScoutBundle getProject() {
    return m_project;
  }
}
