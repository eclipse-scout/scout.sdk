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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateDelta;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.IStateDeltaListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelDelta;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.IScoutWorkspace;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

@SuppressWarnings("restriction")
public final class ScoutWorkspace implements IScoutWorkspace {

  static final String BUNDLE_GRAPH_REBUILD_JOB_FAMILY = "rebuildScoutBundleGraphJobFamily";
  private static final ScoutWorkspace INSTANCE = new ScoutWorkspace();

  private final ScoutBundleGraph m_bundleGraph;
  private final P_PluginModelListener m_pluginModelListener;

  private EventListenerList m_eventListeners;
  private boolean m_isInitialized;

  private ScoutWorkspace() {
    m_isInitialized = false;
    m_eventListeners = new EventListenerList();
    m_bundleGraph = new ScoutBundleGraph();
    m_pluginModelListener = new P_PluginModelListener();

    // bundle graph rebuild listener
    PDECore pdeCore = PDECore.getDefault();
    if (pdeCore != null) {
      pdeCore.getModelManager().addPluginModelListener(m_pluginModelListener);
      pdeCore.getModelManager().addStateDeltaListener(m_pluginModelListener);
    }

    // initialize bundle graph
    P_BundleGraphRebuildJob j = createBundleGraphRebuildJob();
    j.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        fireWorkspaceEvent(new ScoutWorkspaceEvent(ScoutWorkspace.this, ScoutWorkspaceEvent.TYPE_WORKSPACE_INITIALIZED, null));
        m_isInitialized = true;
      }
    });
    j.schedule();
  }

  public synchronized void dispose() {
    m_isInitialized = false;
    PDECore pdeCore = PDECore.getDefault();
    if (m_pluginModelListener != null && pdeCore != null) {
      pdeCore.getModelManager().removePluginModelListener(m_pluginModelListener);
      pdeCore.getModelManager().removeStateDeltaListener(m_pluginModelListener);
    }

    Job.getJobManager().cancel(BUNDLE_GRAPH_REBUILD_JOB_FAMILY);
    P_BundleGraphRebuildShutdownJob job = new P_BundleGraphRebuildShutdownJob();
    job.schedule();
    try {
      job.join(20000);
    }
    catch (InterruptedException e) {
    }

    m_bundleGraph.dispose();
    m_eventListeners = new EventListenerList(); // loose all old listeners
  }

  public static ScoutWorkspace getInstance() {
    return INSTANCE;
  }

  @Override
  public synchronized void addWorkspaceListener(IScoutWorkspaceListener listener) {
    m_eventListeners.add(IScoutWorkspaceListener.class, listener);
  }

  @Override
  public synchronized void removeWorkspaceListener(IScoutWorkspaceListener listener) {
    m_eventListeners.remove(IScoutWorkspaceListener.class, listener);
  }

  @Override
  public IScoutBundleGraph getBundleGraph() {
    return m_bundleGraph;
  }

  ScoutBundleGraph getBundleGraphInternal() {
    return m_bundleGraph;
  }

  synchronized int getNumBundleGraphRebuildJobs() {
    return Job.getJobManager().find(BUNDLE_GRAPH_REBUILD_JOB_FAMILY).length;
  }

  /**
   * Schedules a job that rebuilds the scout bundle graph (asynchronously).
   * After finishing, changes are reported using the {@link IScoutWorkspaceListener}
   *
   * @see IScoutWorkspace#addWorkspaceListener(IScoutWorkspaceListener)
   */
  public void rebuildGraph() {
    final P_BundleGraphRebuildJob j = createBundleGraphRebuildJob();
    j.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        if (event.getResult().isOK()) {
          fireWorkspaceEvents(j.getEventCollector());
        }
      }
    });

    synchronized (this) {
      Job.getJobManager().cancel(BUNDLE_GRAPH_REBUILD_JOB_FAMILY);
      j.schedule();
    }
  }

  private P_BundleGraphRebuildJob createBundleGraphRebuildJob() {
    P_BundleGraphRebuildJob j = new P_BundleGraphRebuildJob();
    j.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        if (event.getResult().isOK()) {
          for (String detectedIssue : m_bundleGraph.getDependencyIssues()) {
            ScoutSdk.logWarning(detectedIssue);
          }
        }
      }
    });
    return j;
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
      catch (Exception t) {
        ScoutSdk.logError("error during listener notification '" + l.getClass().getName() + "'.", t);
      }
    }
  }

  private static final class P_BundleGraphRebuildShutdownJob extends JobEx {

    private P_BundleGraphRebuildShutdownJob() {
      super("wait for bundle graph job to shutdown");
      setSystem(true);
      setUser(false);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      JdtUtility.waitForJobFamily(BUNDLE_GRAPH_REBUILD_JOB_FAMILY);
      return Status.CANCEL_STATUS;
    }
  }

  private final class P_BundleGraphRebuildJob extends JobEx {

    private final ScoutWorkspaceEventList m_eventCollector;

    private P_BundleGraphRebuildJob() {
      super(Texts.get("RebuildingScoutBundleGraph") + "...");
      setUser(false);
      // [mvi] Important: do not set a rule for this job:
      // When in this job a new type must be found, the search-engine is started.
      // This may cause the eclipse project to be touched (see org.eclipse.core.internal.resources.Project#touch()).
      // In this touch method a rule is specified which leads to an IllegalArgumentException in org.eclipse.core.internal.jobs.ThreadJob#push() when there is already a rule set.
      // If a rule must be added, ensure it contains all workspace projects (see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(ISchedulingRule rule)).
      m_eventCollector = new ScoutWorkspaceEventList(ScoutWorkspace.this);
    }

    @Override
    public boolean belongsTo(Object family) {
      return BUNDLE_GRAPH_REBUILD_JOB_FAMILY.equals(family);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        boolean completed = m_bundleGraph.build(getEventCollector(), monitor);
        if (completed) {
          return Status.OK_STATUS;
        }
        else {
          return Status.CANCEL_STATUS;
        }
      }
      catch (Exception t) {
        ScoutSdk.logError("Unable to build the Scout Bundle Graph.", t);
        return new ScoutStatus(t);
      }
    }

    public ScoutWorkspaceEventList getEventCollector() {
      return m_eventCollector;
    }
  }

  private final class P_PluginModelListener implements IPluginModelListener, IStateDeltaListener {
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
      return delta.getChangedEntries().length > 0 || delta.getAddedEntries().length > 0 || delta.getRemovedEntries().length > 0;
    }

    @Override
    public void stateResolved(StateDelta delta) {
    }

    @Override
    public void stateChanged(State newState) {
      rebuildGraph();
    }
  }

  @Override
  public boolean isInitialized() {
    return m_isInitialized;
  }
}
