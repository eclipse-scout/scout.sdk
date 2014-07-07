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
package org.eclipse.scout.sdk.ws.jaxws.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.IMarkerCommand;

public class CommandExecutionOperation implements IOperation {

  private IMarkerCommand[] m_commands;

  public CommandExecutionOperation(IMarkerCommand... command) {
    m_commands = command;
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JaxWsSdk.getDefault().getMarkerQueueManager().suspend();
    try {
      for (IMarkerCommand command : m_commands) {
        if (command.isDoExecute()) {
          command.execute(monitor, workingCopyManager);
        }
      }
    }
    finally {
      JaxWsSdk.getDefault().getMarkerQueueManager().resume();
    }
  }

  @Override
  public String getOperationName() {
    return CommandExecutionOperation.class.getName();
  }
}
