/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.marker.commands;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SeparatorType;

public class MissingClasspathEntryForJarFileCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private IFile m_stubJarFile;

  public MissingClasspathEntryForJarFileCommand(IScoutBundle bundle, String alias, IFile stubJarFile) {
    super("Stub JAR file '" + stubJarFile.getName() + "' is not on project classpath");
    m_bundle = bundle;
    m_stubJarFile = stubJarFile;
    setSolutionDescription("By using this task, the JAR file of the WS '" + alias + "' is registered on the project classpath.");
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String jarFilePath = JaxWsSdkUtility.normalizePath(m_stubJarFile.getProjectRelativePath().toPortableString(), SeparatorType.None);
    PluginModelHelper h = new PluginModelHelper(m_bundle.getProject());
    h.Manifest.addClasspathEntry(jarFilePath);
    h.save();
  }
}
