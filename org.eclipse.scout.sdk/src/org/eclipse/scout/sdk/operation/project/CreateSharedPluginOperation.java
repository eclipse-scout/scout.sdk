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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class CreateSharedPluginOperation extends AbstractCreateScoutBundleOperation {
  public static final String PROP_BUNDLE_SHARED_NAME = "BUNDLE_SHARED_NAME";
  public static final String PROP_TEXT_SERVICE_NAME = "TEXT_PROV_SVC_NAME";
  public static final String PROP_DOC_TEXT_SERVICE_NAME = "DOC_TEXT_PROV_SVC_NAME";

  public static final String SHARED_PROJECT_NAME_SUFFIX = ".shared";
  public static final String BUNDLE_ID = "org.eclipse.scout.sdk.ui.SharedBundle";

  @Override
  public String getOperationName() {
    return "Create Shared Plugin";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateSharedPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    setSymbolicName(getPluginName(SHARED_PROJECT_NAME_SUFFIX));
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    getProperties().setProperty(PROP_BUNDLE_SHARED_NAME, getSymbolicName());
    Map<String, String> props = getStringProperties();
    new InstallTextFileOperation("templates/shared/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, props).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/build.properties", "build.properties", project, props).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/plugin.xml", "plugin.xml", project, props).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/resources/texts/Texts.properties", "resources/texts/Texts.properties", project, props).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/resources/docs/Docs.properties", "resources/docs/Docs.properties", project, props).run(monitor, workingCopyManager);
  }
}
