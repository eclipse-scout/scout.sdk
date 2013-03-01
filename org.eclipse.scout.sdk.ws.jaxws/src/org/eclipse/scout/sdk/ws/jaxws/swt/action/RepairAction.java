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
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerUtility;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.IMarkerCommand;
import org.eclipse.scout.sdk.ws.jaxws.operation.CommandExecutionOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.CommandExecutionDialog;
import org.eclipse.swt.widgets.Shell;

public class RepairAction extends AbstractLinkAction {

  private IScoutBundle m_bundle;
  private String m_markerGroupUUID;

  public RepairAction() {
    super(Texts.get("Repair"), null);
    setLeadingText(Texts.get("presenterRepair"));
    setLinkText(Texts.get("repairTools"));
    setToolTip(Texts.get("ClickToFixTheProblems"));
  }

  public void init(String markerGroupUUID, IScoutBundle bundle) {
    m_markerGroupUUID = markerGroupUUID;
    m_bundle = bundle;

    IMarkerCommand[] markerCommands = MarkerUtility.getMarkerCommands(m_markerGroupUUID, m_bundle);
    int severity = IMarker.SEVERITY_INFO;
    for (IMarkerCommand cmd : markerCommands) {
      severity = Math.max(cmd.getMarker().getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO), severity);
      if (severity == IMarker.SEVERITY_ERROR) {
        break;
      }
    }

    switch (severity) {
      case IMarker.SEVERITY_INFO: {
        setImage(JaxWsSdk.getImageDescriptor(JaxWsIcons.RepairInfo));
        break;
      }
      case IMarker.SEVERITY_WARNING: {
        setImage(JaxWsSdk.getImageDescriptor(JaxWsIcons.RepairWarning));
        break;
      }
      default: {
        setImage(JaxWsSdk.getImageDescriptor(JaxWsIcons.RepairError));
        break;
      }
    }
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    IMarkerCommand[] markerCommands = MarkerUtility.getMarkerCommands(m_markerGroupUUID, m_bundle);

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
