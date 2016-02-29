/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.internal.trigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceHandler;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceHandlerFactory;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link DerivedResourceManager}</h3>
 *
 * @author Matthias Villiger
 * @author Andreas Hoegger
 * @since 3.10.0 15.08.2013
 */
public class DerivedResourceManager implements IDerivedResourceManager {

  public static final String TYPE_CHANGED_TRIGGER_JOB_FAMILY = "AUTO_UPDATE_JOB_FAMILY";
  public static final String JAVA_DELTA_CHECK_JOB_FAMILY = "JAVA_DELTA_CHECK_JOB_FAMILY";

  private boolean m_enabled;
  private final List<IDerivedResourceHandlerFactory> m_updateHandlerFactories;

  private IResourceChangeListener m_resourceChangeListener;

  // queue that buffers all java change events that need processing
  private final BlockingQueue<IResourceChangeEvent> m_javaChangeEventsToCheck;
  // job that works through all buffered java change events and checks if they contain trigger relevant compilation units
  private final P_ResourceChangeEventCheckJob m_javaDeltaCheckJob;

  // queue that buffers all trigger operations that need to be executed
  private final BlockingQueue<IDerivedResourceHandler> m_triggerHandlers;
  // job that executes all the buffered trigger operations (visible to the user)
  private final P_RunQueuedTriggerHandlersJob m_runQueuedTriggerHandlersJob;

  public DerivedResourceManager() {
    m_enabled = false;
    m_updateHandlerFactories = new ArrayList<>();

    m_javaChangeEventsToCheck = new ArrayBlockingQueue<>(5000, true);
    m_triggerHandlers = new ArrayBlockingQueue<>(2000, true);

    m_runQueuedTriggerHandlersJob = new P_RunQueuedTriggerHandlersJob(m_triggerHandlers);
    m_javaDeltaCheckJob = new P_ResourceChangeEventCheckJob(this, m_javaChangeEventsToCheck);
  }

  /**
   * Shutdown the manager. Afterwards no auto updates are performed. All listeners are removed. Waits until all derived
   * resources have finished updating.
   */
  public void dispose() {
    setEnabled(false);

    // wait until all derived resources have been generated. otherwise the user ends up with invalid derived resources.
    // the user still can cancel the job if desired.
    AbstractJob.waitForJobFamily(TYPE_CHANGED_TRIGGER_JOB_FAMILY);
  }

  @Override
  public void addDerivedResourceHandlerFactory(IDerivedResourceHandlerFactory handler) {
    m_updateHandlerFactories.add(handler);
  }

  @Override
  public void removeDerivedResourceHandlerFactory(IDerivedResourceHandlerFactory handler) {
    m_updateHandlerFactories.remove(handler);
  }

  @Override
  public void trigger(final Set<IResource> resources) {
    AbstractJob triggerJob = new AbstractJob("Searching base resources for derived resources update...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        triggerSync(resources);
        return Status.OK_STATUS;
      }
    };
    triggerJob.setPriority(Job.DECORATE);
    triggerJob.schedule();
  }

  protected void triggerSync(Set<IResource> resources) {
    Set<IResource> cleanResources = cleanCopy(resources); // remove non-accessible, containing and null resources
    if (enqueueFiles(cleanResources)) {
      m_runQueuedTriggerHandlersJob.abort();
      m_runQueuedTriggerHandlersJob.schedule(1000); // wait a little to give other follow-up events time so that they don't trigger another re-calculation job
    }
  }

  /**
   * @return true if elements were added and job can be scheduled.
   */
  protected boolean enqueueFiles(Set<IResource> resources) {
    if (resources.isEmpty()) {
      return false;
    }

    boolean added = false;
    try {
      CachingJavaEnvironmentProvider envProvider = new CachingJavaEnvironmentProvider();
      IJavaSearchScope searchScope = S2eUtils.createJavaSearchScope(resources);
      for (IDerivedResourceHandler handler : createOperations(resources, envProvider, searchScope)) {
        if (!m_triggerHandlers.contains(handler)) {
          if (addElementToQueueSecure(m_triggerHandlers, handler, handler.getName(), -1, null)) {
            //ok, continue
            added = true;
          }
          else {
            SdkLog.warning("Unable to queue more derived resource update events. Queue is already full. Skipping event: {}", handler.getName());
          }
        }
      }
    }
    catch (CoreException e) {
      SdkLog.warning("Unable to create java search scope", e);
    }
    return added;
  }

  protected static Set<IResource> cleanCopy(Set<IResource> resources) {
    if (resources == null) {
      return Collections.emptySet();
    }

    Set<IResource> cleanSet = new HashSet<>(resources.size());
    for (IResource r : resources) {
      if (r != null && r.isAccessible() && !existsParentIn(resources, r)) {
        cleanSet.add(r);
      }
    }
    return cleanSet;
  }

  protected static boolean existsParentIn(Collection<IResource> searchList, IResource resource) {
    IPath path = resource.getFullPath();
    for (IResource r : searchList) {
      if (r == null || !r.isAccessible()) {
        continue;
      }
      if (!r.equals(resource) && r.getFullPath().isPrefixOf(path)) {
        return true;
      }
    }
    return false;
  }

  protected Collection<IDerivedResourceHandler> createOperations(Set<IResource> resources, IJavaEnvironmentProvider envProvider, IJavaSearchScope searchScope) {
    List<IDerivedResourceHandler> all = null;
    for (IDerivedResourceHandlerFactory factory : m_updateHandlerFactories) {
      try {
        List<IDerivedResourceHandler> ops = factory.createHandlersFor(resources, envProvider, searchScope);
        if (ops != null && !ops.isEmpty()) {
          if (all == null) {
            all = new ArrayList<>();
          }
          all.addAll(ops);
        }
      }
      catch (Exception e) {
        SdkLog.error("Unable to create operation with handler '{}'.", factory.getClass(), e);
      }
    }
    if (all == null) {
      return Collections.<IDerivedResourceHandler> emptyList();
    }
    return all;
  }

  @Override
  public synchronized void setEnabled(boolean enabled) {
    m_enabled = enabled;
    if (enabled) {
      if (m_resourceChangeListener == null) {
        m_resourceChangeListener = new P_ResourceChangeListener(m_javaChangeEventsToCheck);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
      }
      m_javaDeltaCheckJob.schedule();
    }
    else {
      if (m_resourceChangeListener != null) {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceChangeListener);
        m_resourceChangeListener = null;
      }

      // cancel the job that checks the java deltas
      Thread thread = m_javaDeltaCheckJob.getThread();
      if (thread != null) {
        m_javaDeltaCheckJob.cancel();
        thread.interrupt();
        try {
          m_javaDeltaCheckJob.join(3000, null);
        }
        catch (InterruptedException e) {
          // nop
        }
      }
    }
  }

  @Override
  public synchronized boolean isEnabled() {
    return m_enabled;
  }

  /**
   * Securely inserts the given element in the given queue.<br>
   * If the thread is interrupted too often while waiting for space in the queue it gives up.
   *
   * @param queue
   *          The queue to insert to
   * @param element
   *          The element to insert
   * @param name
   *          The name of the element to add.
   * @param timeout
   *          The timeout.<br>
   *          <0=no time limit. We wait until there is free space (infinite waiting).<br>
   *          0=no timeout, no waiting. Either it can be inserted now or we give up.<br>
   *          >0=we wait for this amount. The meaning of the timeout is defined by the unit parameter which must be
   *          specified in this case.
   * @param unit
   *          The {@link TimeUnit} that defines the meaning of timeout
   * @return true if the element has been added to the queue within the given timeout range. false otherwise.
   */
  private static <T> boolean addElementToQueueSecure(BlockingQueue<T> queue, T element, String name, long timeout, TimeUnit unit) {
    boolean interrupted;
    int numInterrupted = 0;
    do {
      try {
        interrupted = false;
        if (timeout == 0) {
          // immediate insert try (no waiting)
          return queue.offer(element);
        }
        else if (timeout < 0) {
          // no time limit to wait for space
          queue.put(element);
          return true;
        }
        else {
          // specific time to wait
          return queue.offer(element, timeout, unit);
        }
      }
      catch (InterruptedException e) {
        interrupted = numInterrupted++ < 10;
      }
    }
    while (interrupted);
    SdkLog.warning("Too many thread interrupts while waiting for space in the trigger queue. Skipping '{}'.", name);
    return false; // we had too many interrupts. we don't want to wait any longer (no endless looping).
  }

  /**
   * The java change listener that adds the given event to the queue to execute later on
   */
  private static final class P_ResourceChangeListener implements IResourceChangeListener {

    private final BlockingQueue<IResourceChangeEvent> m_eventCollector;

    private P_ResourceChangeListener(BlockingQueue<IResourceChangeEvent> eventCollector) {
      m_eventCollector = eventCollector;
    }

    private static boolean acceptUpdateEvent() {
      Job curJob = Job.getJobManager().currentJob();
      if (curJob == null) {
        return false;
      }

      if (curJob instanceof AbstractJob || curJob instanceof P_RunQueuedTriggerHandlersJob) {
        // do not automatically update on Scout SDK changes. We expect the SDK to trigger manually where required.
        return false;
      }

      if (curJob.belongsTo(ResourcesPlugin.FAMILY_AUTO_BUILD) || curJob.belongsTo(ResourcesPlugin.FAMILY_MANUAL_BUILD)) {
        // ignore build changes (annotation processing)
        return false;
      }

      final String[] excludedJobNamePrefixes = new String[]{"org.eclipse.team.", // excludes svn updates
          "org.eclipse.core.internal.events.NotificationManager.NotifyJob", // excludes annotation processing updates
          "org.eclipse.egit.", // excludes git updates
          "org.eclipse.core.internal.events.AutoBuildJob", // exclude annotation processing updates
          "org.eclipse.m2e.", // maven updates
          "org.eclipse.jdt.internal.core.ExternalFoldersManager.RefreshJob", // refresh of external folders after svn update
          "org.eclipse.core.internal.refresh.RefreshJob", // refresh after git import
          "org.eclipse.jdt.internal.ui.InitializeAfterLoadJob.RealJob", // workspace init job
          "org.eclipse.wst.jsdt.internal.ui.InitializeAfterLoadJob.RealJob"
      };

      String jobFqn = curJob.getClass().getName().replace('$', '.');
      for (String excludedPrefix : excludedJobNamePrefixes) {
        if (jobFqn.startsWith(excludedPrefix)) {
          return false;
        }
      }

      if ("org.eclipse.core.internal.jobs.ThreadJob".equals(jobFqn)) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = stackTrace.length - 1; i >= 0; i--) {
          for (String excludedPrefix : excludedJobNamePrefixes) {
            if (stackTrace[i].getClassName().startsWith(excludedPrefix)) {
              return false;
            }
          }
        }
      }

      return true;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      if (event != null && acceptUpdateEvent() && !addElementToQueueSecure(m_eventCollector, event, event.toString(), 10, TimeUnit.SECONDS)) {
        // element could not be added within the given timeout
        SdkLog.warning("Unable to queue more java element changes. Queue is already full. Skipping event.");
      }
    }
  }

  /**
   * Job that iterates over all java change events and checks if they require a update.
   */
  private static final class P_ResourceChangeEventCheckJob extends AbstractJob {

    private final DerivedResourceManager m_manager;
    private final BlockingQueue<IResourceChangeEvent> m_queueToConsume;

    private P_ResourceChangeEventCheckJob(DerivedResourceManager manager, BlockingQueue<IResourceChangeEvent> queueToConsume) {
      super("Check if resource delta triggers a derived resource update");
      setSystem(true);
      setUser(false);
      setPriority(DECORATE);
      m_manager = manager;
      m_queueToConsume = queueToConsume;
    }

    @Override
    public boolean belongsTo(Object family) {
      return JAVA_DELTA_CHECK_JOB_FAMILY.equals(family);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      while (!monitor.isCanceled()) {
        IResourceChangeEvent event = null;
        try {
          event = m_queueToConsume.take(); // blocks until deltas are available
        }
        catch (InterruptedException e1) {
          // nop
        }
        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }
        if (event != null && event.getDelta() != null) {
          // collect all files that have been changed as part of this delta
          Set<IResource> resources = collectFilesFromDelta(event.getDelta());
          m_manager.triggerSync(resources);
        }
      }
      return Status.CANCEL_STATUS;
    }

    private static Set<IResource> collectFilesFromDelta(IResourceDelta d) {
      final Set<IResource> scope = new HashSet<>();
      try {
        d.accept(new IResourceDeltaVisitor() {
          @Override
          public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            if (resource != null && resource.getType() == IResource.FILE && resource.exists()) {
              scope.add(resource);
              return false;
            }
            return true;
          }
        });
      }
      catch (CoreException e) {
        SdkLog.error("Could not calculate the resources affected by a change event.", e);
      }

      return scope;
    }
  }

  /**
   * Job that executes all trigger operations that have been enqueued, with lowest priority.
   */
  private static final class P_RunQueuedTriggerHandlersJob extends AbstractJob {

    private final BlockingQueue<IDerivedResourceHandler> m_queueToConsume;
    private boolean m_isAborted;

    private P_RunQueuedTriggerHandlersJob(BlockingQueue<IDerivedResourceHandler> queueToConsume) {
      super("Auto-updating derived resources");
      setRule(RunTriggerHandlersJobRule.INSTANCE);
      setPriority(Job.DECORATE);
      m_isAborted = false;
      m_queueToConsume = queueToConsume;
    }

    @Override
    public boolean belongsTo(Object family) {
      return TYPE_CHANGED_TRIGGER_JOB_FAMILY.equals(family);
    }

    /**
     * An abort stops the current or next run of this job.<br>
     * <br>
     * An abort differs to a cancel() in that way, that a cancel (can only be performed by the user) discards all
     * operations that are not yet executed while an abort keeps them and will continue to work on them in the next
     * schedule().<br>
     * <br>
     * An abort will automatically re-schedule this job (if this is no already done) to ensure that no work remains
     * undone.
     */
    private void abort() {
      m_isAborted = true;
    }

    private boolean isAborted() {
      return m_isAborted;
    }

    private IStatus doCancel() {
      m_queueToConsume.clear();
      return Status.CANCEL_STATUS;
    }

    private IStatus doAbort() {
      m_isAborted = false;
      schedule(); // there may have been more operations added since we were aborted
      return Status.CANCEL_STATUS;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (monitor.isCanceled()) {
        return doCancel();
      }
      if (isAborted()) {
        return doAbort();
      }

      int numOperations = m_queueToConsume.size();
      if (numOperations < 1) {
        return Status.OK_STATUS;
      }

      SubMonitor progress = SubMonitor.convert(monitor, getName(), numOperations);
      for (int i = 1; i <= numOperations; i++) {
        if (progress.isCanceled()) {
          return doCancel();
        }
        if (isAborted()) {
          return doAbort();
        }

        // already remove the operation here. if there is a problem with this operation we don't want to keep trying
        IDerivedResourceHandler handler = m_queueToConsume.poll();
        try {
          progress.setTaskName(handler.getName() + " [" + i + " of " + numOperations + "]");
          progress.subTask("");
          handler.validate();

          long start = System.currentTimeMillis();
          try {
            handler.run(progress.newChild(1));
          }
          finally {
            SdkLog.debug("Derived Resource Handler ({}) took {}ms to execute.", handler.getName(), System.currentTimeMillis() - start);
          }
        }
        catch (Exception e) {
          SdkLog.error("Error while: {}", handler.getName(), e);
        }
      }
      return Status.OK_STATUS;
    }
  }

  public static final class RunTriggerHandlersJobRule implements ISchedulingRule {

    public static final RunTriggerHandlersJobRule INSTANCE = new RunTriggerHandlersJobRule();

    private RunTriggerHandlersJobRule() {
    }

    @Override
    public boolean contains(ISchedulingRule rule) {
      return rule == INSTANCE;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
      return rule == INSTANCE;
    }
  }
}
