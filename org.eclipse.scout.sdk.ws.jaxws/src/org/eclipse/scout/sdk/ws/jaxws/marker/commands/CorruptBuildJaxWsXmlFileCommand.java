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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.operation.BuildJaxWsFileCreateOperation;

public class CorruptBuildJaxWsXmlFileCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;

  public CorruptBuildJaxWsXmlFileCommand(IScoutBundle bundle) {
    super(JaxWsConstants.PATH_BUILD_JAXWS.lastSegment());
    m_bundle = bundle;
    setSolutionDescription("Create a new '" + JaxWsConstants.PATH_BUILD_JAXWS + "' file");
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    BuildJaxWsFileCreateOperation op = new BuildJaxWsFileCreateOperation(m_bundle);
    op.run(monitor, workingCopyManager);
  }
}
