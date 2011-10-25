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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * creates plugins like for example
 * com.google.rcp.client
 */
public class CreateSharedPluginOperation extends AbstractCreateScoutBundleOperation {

  private final ITemplateVariableSet m_templateBindings;

  public CreateSharedPluginOperation(ITemplateVariableSet templateBindings) {
    setSymbolicName(templateBindings.getVariable(ITemplateVariableSet.VAR_BUNDLE_SHARED_NAME));
    m_templateBindings = templateBindings;
  }

  @Override
  public String getOperationName() {
    return "Create Shared Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);
    new InstallTextFileOperation("templates/shared/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/build.properties", "build.properties", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/plugin.xml", "plugin.xml", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/resources/texts/Texts.properties", "resources/texts/Texts.properties", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/resources/texts/Texts_de.properties", "resources/texts/Texts_de.properties", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/resources/texts/Texts_fr.properties", "resources/texts/Texts_fr.properties", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/shared/resources/texts/Texts_it.properties", "resources/texts/Texts_it.properties", project, bindings).run(monitor, workingCopyManager);
  }
}
