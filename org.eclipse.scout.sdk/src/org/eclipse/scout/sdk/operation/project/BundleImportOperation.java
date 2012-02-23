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
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class BundleImportOperation implements IOperation {

  private String m_projectId;
  private IPluginModelBase m_pluginModel;

  @Override
  public String getOperationName() {
    return "Import '" + m_pluginModel.getBundleDescription().getName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getProjectId())) {
      throw new IllegalArgumentException("Project ID can not be null");
    }
    if (getPluginModel() == null) {
      throw new IllegalArgumentException("Plugin can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

    PluginModelHelper h = new PluginModelHelper(getPluginModel());
    // remove legacy
    h.Manifest.removeEntry("BsiCase-ProjectGroupId");
    h.Manifest.removeEntry("BsiCase-Alias");
    h.Manifest.removeEntry("BsiCase-BundleType");
    h.save();

    // nature
    IProject project = getPluginModel().getUnderlyingResource().getProject();
    IProjectDescription description = project.getDescription();
    String[] existingNatures = description.getNatureIds();
    ArrayList<String> newNatures = new ArrayList<String>(Arrays.asList(existingNatures));
    // remove legacy
    for (Iterator<String> it = newNatures.iterator(); it.hasNext();) {
      String visitNature = it.next();
      if (visitNature.equals("com.bsiag.bsicase.bsiCaseProjectNature")) {
        it.remove();
      }
    }
    newNatures.add(ScoutSdk.NATURE_ID);
    description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
    project.setDescription(description, monitor);
  }

  public void setProjectId(String projectId) {
    m_projectId = projectId;
  }

  public String getProjectId() {
    return m_projectId;
  }

  public void setPluginModel(IPluginModelBase pluginModel) {
    m_pluginModel = pluginModel;
  }

  public IPluginModelBase getPluginModel() {
    return m_pluginModel;
  }

}
