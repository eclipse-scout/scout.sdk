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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public interface IMarkerCommand {

  boolean prepareForUi() throws CoreException;

  void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException;

  void setDoExecute(boolean doExecute);

  boolean isDoExecute();

  boolean isExecutable();

  String getProblemName();

  String getSolutionDescription();

  IMarker getMarker();

  void setMarker(IMarker marker);
}
