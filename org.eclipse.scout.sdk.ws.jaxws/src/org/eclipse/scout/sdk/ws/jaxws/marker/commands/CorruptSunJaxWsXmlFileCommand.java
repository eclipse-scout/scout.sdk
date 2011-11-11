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
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.operation.SunJaxWsFileCreateOperation;

public class CorruptSunJaxWsXmlFileCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;

  public CorruptSunJaxWsXmlFileCommand(IScoutBundle bundle) {
    super(new Path(JaxWsConstants.PATH_SUN_JAXWS).lastSegment());
    m_bundle = bundle;
    setSolutionDescription("Create a new '" + JaxWsConstants.PATH_SUN_JAXWS + "' file");
  }

  @Override
  public void execute(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    SunJaxWsFileCreateOperation op = new SunJaxWsFileCreateOperation(m_bundle);
    op.run(monitor, workingCopyManager);
  }
}
