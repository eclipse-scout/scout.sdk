/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.executor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerUtility;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.IMarkerCommand;
import org.eclipse.scout.sdk.ws.jaxws.operation.CommandExecutionOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.CommandExecutionDialog;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderCodeFirstNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link RepairExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class RepairExecutor extends AbstractExecutor {

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return isEditable(UiUtility.getScoutBundleFromSelection(selection));
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    IScoutBundle scoutBundle = UiUtility.getScoutBundleFromSelection(selection);
    Object selected = selection.getFirstElement();
    String markerGroupUUID = null;
    if (selected instanceof WebServiceConsumerNodePage) {
      markerGroupUUID = ((WebServiceConsumerNodePage) selected).getMarkerGroupUUID();
    }
    else if (selected instanceof WebServiceProviderCodeFirstNodePage) {
      markerGroupUUID = ((WebServiceProviderCodeFirstNodePage) selected).getMarkerGroupUUID();
    }
    else if (selected instanceof WebServiceProviderNodePage) {
      markerGroupUUID = ((WebServiceProviderNodePage) selected).getMarkerGroupUUID();
    }
    else {
      return null;
    }

    IMarkerCommand[] markerCommands = MarkerUtility.getMarkerCommands(markerGroupUUID, scoutBundle);

    CommandExecutionDialog dialog = new CommandExecutionDialog(Texts.get("RepairTools"));
    dialog.setHeaderMessage(Texts.get("TheFollowingProblemsWereEncountered"));
    dialog.setCommands(markerCommands);

    if (dialog.open() == Window.OK) {
      // prepare UI commands
      List<IMarkerCommand> commandsToBeExecuted = new LinkedList<IMarkerCommand>();
      for (IMarkerCommand command : markerCommands) {
        if (command.isDoExecute()) {
          try {
            if (command.prepareForUi()) {
              commandsToBeExecuted.add(command);
            }
          }
          catch (CoreException e) {
            JaxWsSdk.logError("Error occured while preparing command to be executed", e);
          }
        }
      }
      OperationJob job = new OperationJob(new CommandExecutionOperation(commandsToBeExecuted.toArray(new IMarkerCommand[commandsToBeExecuted.size()])));
      job.schedule();
    }
    return null;
  }

}
