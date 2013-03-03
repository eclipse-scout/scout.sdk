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
package org.eclipse.scout.sdk.internal.workspace;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelDelta;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.IScoutWorkspace;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

@SuppressWarnings("restriction")
public final class ScoutWorkspace implements IScoutWorkspace {

  private final static ScoutWorkspace INSTANCE = new ScoutWorkspace();

  private final EventListenerList m_eventListeners;
  private final ScoutBundleGraph m_bundleGraph;

  private ScoutWorkspace() {
    m_eventListeners = new EventListenerList();
    m_bundleGraph = new ScoutBundleGraph();

    PDECore.getDefault().getModelManager().addPluginModelListener(new P_PluginModelListener());
    m_bundleGraph.build();

    for (String detectedIssue : m_bundleGraph.getDependencyIssues()) {
      ScoutSdk.logWarning(detectedIssue);
    }

    fireWorkspaceEvent(new ScoutWorkspaceEvent(this, ScoutWorkspaceEvent.TYPE_WORKSPACE_INITIALIZED, null));
  }

  public static ScoutWorkspace getInstance() {
    return INSTANCE;
  }

  @Override
  public void addWorkspaceListener(IScoutWorkspaceListener listener) {
    m_eventListeners.add(IScoutWorkspaceListener.class, listener);
  }

  @Override
  public void removeWorkspaceListener(IScoutWorkspaceListener listener) {
    m_eventListeners.remove(IScoutWorkspaceListener.class, listener);
  }

  @Override
  public IScoutBundleGraph getBundleGraph() {
    return m_bundleGraph;
  }

  public ScoutBundleGraph getBundleGraphInternal() {
    return m_bundleGraph;
  }

  public void rebuildGraph() {
    ScoutWorkspaceEventList eventCollector = new ScoutWorkspaceEventList(this);
    m_bundleGraph.build(eventCollector);
    fireWorkspaceEvents(eventCollector);
  }

  private void fireWorkspaceEvents(ScoutWorkspaceEventList events) {
    for (ScoutWorkspaceEvent e : events.getAllEvents()) {
      fireWorkspaceEvent(e);
    }
  }

  private void fireWorkspaceEvent(ScoutWorkspaceEvent e) {
    for (IScoutWorkspaceListener l : m_eventListeners.getListeners(IScoutWorkspaceListener.class)) {
      try {
        l.workspaceChanged(e);
      }
      catch (Throwable t) {
        ScoutSdk.logError("error during listener notification.", t);
      }
    }
  }

  private final class P_PluginModelListener implements IPluginModelListener {
    @Override
    public void modelsChanged(PluginModelDelta delta) {
      try {
        if (containsInterestingProjects(delta)) {
          rebuildGraph();
        }
      }
      catch (CoreException e) {
        ScoutSdk.logError(e);
      }
    }

    private boolean containsInterestingProjects(PluginModelDelta delta) throws CoreException {
      return delta.getAddedEntries().length > 0 || delta.getChangedEntries().length > 0 || delta.getRemovedEntries().length > 0;
    }
  }
}
