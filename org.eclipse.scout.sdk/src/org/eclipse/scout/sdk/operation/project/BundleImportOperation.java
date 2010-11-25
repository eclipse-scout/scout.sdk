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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.pde.Manifest;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class BundleImportOperation implements IOperation {

  private String m_projectId;
  private IPluginModelBase m_pluginModel;
  private int m_bundleType;

  @Override
  public String getOperationName() {
    return "Import '" + m_pluginModel.getBundleDescription().getName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getProjectId())) {
      throw new IllegalArgumentException("Project ID can not be null");
    }
    if ((getBundleType() & (ScoutIdeProperties.BUNDLE_TYPE_CLIENT | ScoutIdeProperties.BUNDLE_TYPE_CLIENT_APPLICATION | ScoutIdeProperties.BUNDLE_TYPE_SERVER |
        ScoutIdeProperties.BUNDLE_TYPE_SERVER_APPLICATION | ScoutIdeProperties.BUNDLE_TYPE_SHARED | ScoutIdeProperties.BUNDLE_TYPE_TEST_CLIENT |
        ScoutIdeProperties.BUNDLE_TYPE_UI_SWING | ScoutIdeProperties.BUNDLE_TYPE_UI_SWT | ScoutIdeProperties.BUNDLE_TYPE_UI_SWT_APPLICATION)) == 0) {
      throw new IllegalArgumentException("Unknown bundle type.");
    }
    if (getPluginModel() == null) {
      throw new IllegalArgumentException("Plugin can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

    Manifest manifest = new Manifest();
    manifest.read((IFile) getPluginModel().getUnderlyingResource());
    // remove legacy
    manifest.removeAttribute("BsiCase-ProjectGroupId");
    manifest.removeAttribute("BsiCase-Alias");
    manifest.removeAttribute("BsiCase-BundleType");
    // add new
    manifest.setAttribute(Manifest.SCOUT_PROJECT, getProjectId());
    String bundleType = null;
    switch (getBundleType()) {
      case ScoutIdeProperties.BUNDLE_TYPE_CLIENT:
        bundleType = Manifest.SCOUT_BUNDLE_TYPE_CLIENT;
        break;
      case ScoutIdeProperties.BUNDLE_TYPE_SERVER:
        bundleType = Manifest.SCOUT_BUNDLE_TYPE_SERVER;
        break;
      case ScoutIdeProperties.BUNDLE_TYPE_SHARED:
        bundleType = Manifest.SCOUT_BUNDLE_TYPE_SHARED;
        break;
    }
    if (!StringUtility.isNullOrEmpty(bundleType)) {
      manifest.setAttribute(Manifest.SCOUT_BUNDLE_TYPE, bundleType);
    }
    try {
      manifest.write((IFile) getPluginModel().getUnderlyingResource());
    }
    catch (IOException e) {
      ScoutSdk.logError("could not write manifest.mf of '" + getPluginModel().getBundleDescription().getName() + "'.");
    }
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

  public void setBundleType(int bundleType) {
    m_bundleType = bundleType;
  }

  public int getBundleType() {
    return m_bundleType;
  }

  public void setPluginModel(IPluginModelBase pluginModel) {
    m_pluginModel = pluginModel;
  }

  public IPluginModelBase getPluginModel() {
    return m_pluginModel;
  }

}
