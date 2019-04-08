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
package org.eclipse.scout.sdk.s2e.derived;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.Future;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link DerivedResourceManager}</h3>
 *
 * @since 3.10.0 2013-08-15
 */
public class DerivedResourceManager implements IDerivedResourceManager {

  public static final String TYPE_CHANGED_TRIGGER_JOB_FAMILY = "AUTO_UPDATE_JOB_FAMILY";
  public static final String JAVA_DELTA_CHECK_JOB_FAMILY = "JAVA_DELTA_CHECK_JOB_FAMILY";

  private boolean m_enabled;
  private final List<IDerivedResourceHandlerFactory> m_updateHandlerFactories;

  private IResourceChangeListener m_resourceChangeListener;
  private Predicate<IResourceChangeEvent> m_resourceChangeEventFilter;

  // queue that buffers all java change events that need processing
  private final BlockingQueue<IResourceChangeEvent> m_javaChangeEventsToCheck;
  // job that works through all buffered java change events and checks if they contain trigger relevant compilation units
  private final P_ResourceChangeEventCheckJob m_javaDeltaCheckJob;

  // queue that buffers all trigger operations that need to be executed
  private final BlockingQueue<BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>>> m_triggerHandlers;
  // job that executes all the buffered trigger operations (visible to the user)
  private final P_RunQueuedTriggerHandlersJob m_runQueuedTriggerHandlersJob;

  public DerivedResourceManager() {
    m_enabled = false;
    m_updateHandlerFactories = new ArrayList<>();

    DefaultResourceChangeEventFilter filter = new DefaultResourceChangeEventFilter();
    filter.setIgnoreScoutSdkEvents(false);
    m_resourceChangeEventFilter = filter;

    m_javaChangeEventsToCheck = new ArrayBlockingQueue<>(500, true);
    m_triggerHandlers = new ArrayBlockingQueue<>(200, true);

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
  public void trigger(Set<IResource> resources) {
    Job triggerJob = new AbstractJob("Searching base resources for derived resources update...") {
      @Override
      protected void execute(IProgressMonitor monitor) {
        triggerSync(resources);
      }
    };
    triggerJob.setPriority(Job.DECORATE);
    triggerJob.schedule();
  }

  @Override
  public Predicate<IResourceChangeEvent> getResourceChangeEventFilter() {
    return m_resourceChangeEventFilter;
  }

  @Override
  public void setResourceChangeEventFilter(Predicate<IResourceChangeEvent> resourceChangeEventFilter) {
    m_resourceChangeEventFilter = resourceChangeEventFilter;
  }

  protected void triggerSync(Collection<IResource> resources) {
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
      IJavaSearchScope searchScope = JdtUtils.createJavaSearchScope(resources);
      for (BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>> handler : createOperations(resources, searchScope)) {
        if (!m_triggerHandlers.contains(handler)) {
          if (addElementToQueueSecure(m_triggerHandlers, handler, handler.toString(), -1, null)) {
            //ok, continue
            added = true;
          }
          else {
            SdkLog.warning("Unable to queue more derived resource update events. Queue is already full. Skipping event: {}", handler.toString());
          }
        }
      }
    }
    catch (RuntimeException e) {
      SdkLog.warning("Unable to create java search scope", e);
    }
    return added;
  }

  protected static Set<IResource> cleanCopy(Collection<IResource> resources) {
    if (resources == null) {
      return emptySet();
    }

    Set<IResource> cleanSet = new LinkedHashSet<>(resources.size());
    for (IResource r : resources) {
      if (r != null && r.isAccessible() && !existsParentIn(resources, r)) {
        cleanSet.add(r);
      }
    }
    return cleanSet;
  }

  protected static boolean existsParentIn(Iterable<IResource> searchList, IResource resource) {
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

  protected Collection<BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>>> createOperations(Set<IResource> resources, IJavaSearchScope searchScope) {
    List<BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>>> all = null;
    for (IDerivedResourceHandlerFactory factory : m_updateHandlerFactories) {
      try {
        List<BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>>> ops = factory.createHandlersFor(resources, searchScope);
        if (ops != null && !ops.isEmpty()) {
          if (all == null) {
            all = new ArrayList<>();
          }
          all.addAll(ops);
        }
      }
      catch (CoreException e) {
        SdkLog.error("Unable to create operation with handler '{}'.", factory.getClass(), e);
      }
    }
    if (all == null) {
      return emptyList();
    }
    return all;
  }

  @Override
  public synchronized void setEnabled(boolean enabled) {
    if (m_enabled == enabled) {
      return;
    }

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
   *          The {@link TimeUnit} that defines the meaning of timeout if > 0.
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
        SdkLog.debug(e);
        numInterrupted++;
        interrupted = numInterrupted < 10;
      }
    }
    while (interrupted);
    SdkLog.warning("Too many thread interrupts while waiting for space in the trigger queue. Skipping '{}'.", name);
    return false; // we had too many interrupts. we don't want to wait any longer (no endless looping).
  }

  /**
   * The java change listener that adds the given event to the queue to execute later on
   */
  private final class P_ResourceChangeListener implements IResourceChangeListener {

    private final BlockingQueue<IResourceChangeEvent> m_eventCollector;

    private P_ResourceChangeListener(BlockingQueue<IResourceChangeEvent> eventCollector) {
      m_eventCollector = eventCollector;
    }

    private boolean isInterestingResourceChangeEvent(IResourceChangeEvent event) {
      Predicate<IResourceChangeEvent> filter = getResourceChangeEventFilter();
      return filter == null || filter.test(event);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      if (isInterestingResourceChangeEvent(event) && !addElementToQueueSecure(m_eventCollector, event, event.toString(), 10, TimeUnit.SECONDS)) {
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

    private P_ResourceChangeEventCheckJob(DerivedResourceManager m, BlockingQueue<IResourceChangeEvent> queueToConsume) {
      super("Check if resource delta triggers a derived resource update");
      setSystem(true);
      setUser(false);
      setPriority(DECORATE);
      m_manager = m;
      m_queueToConsume = queueToConsume;
    }

    @Override
    public boolean belongsTo(Object family) {
      return JAVA_DELTA_CHECK_JOB_FAMILY.equals(family);
    }

    @Override
    protected void execute(IProgressMonitor monitor) {
      while (!monitor.isCanceled()) {
        IResourceChangeEvent event = null;
        try {
          event = m_queueToConsume.take(); // blocks until deltas are available
        }
        catch (InterruptedException e1) {
          // nop
        }
        if (monitor.isCanceled()) {
          return;
        }
        if (event != null && event.getDelta() != null) {
          // collect all files that have been changed as part of this delta
          Set<IResource> resources = collectFilesFromDelta(event.getDelta());
          m_manager.triggerSync(resources);
        }
      }
    }

    private static Set<IResource> collectFilesFromDelta(IResourceDelta d) {
      Set<IResource> scope = new LinkedHashSet<>();
      try {
        d.accept(delta -> {
          IResource resource = delta.getResource();
          if (resource != null && resource.getType() == IResource.FILE && resource.exists()) {
            scope.add(resource);
            return false;
          }
          return true;
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

    private final BlockingQueue<BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>>> m_queueToConsume;
    private boolean m_isAborted;

    private P_RunQueuedTriggerHandlersJob(BlockingQueue<BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>>> queueToConsume) {
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

    private void doCancel() {
      m_queueToConsume.clear();
    }

    private void doAbort() {
      m_isAborted = false;
      schedule(); // there may have been more operations added since we were aborted
    }

    @Override
    protected void execute(IProgressMonitor monitor) {
      if (monitor.isCanceled()) {
        doCancel();
        return;
      }
      if (isAborted()) {
        doAbort();
        return;
      }

      int numOperations = m_queueToConsume.size();
      if (numOperations < 1) {
        return;
      }

      runInEclipseEnvironment((env, progress) -> execute(env, progress, numOperations));
    }

    private void execute(IEnvironment env, EclipseProgress progress, int numOperations) {
      int workByHandler = 100;
      progress.init("", numOperations * workByHandler);
      Collection<IFuture<?>> executedHandlers = new ArrayList<>(numOperations);
      for (int i = 1; i <= numOperations; i++) {
        if (isAborted()) {
          doAbort();
          return;
        }

        // already remove the operation here. if there is a problem with this operation we don't want to keep trying
        BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>> handler = m_queueToConsume.poll();
        if (handler == null) {
          continue;
        }
        String handlerName = handler.toString();
        progress.monitor().setTaskName(handlerName + " [" + i + " of " + numOperations + ']');
        progress.monitor().subTask("");

        try {
          executedHandlers.addAll(executeHandler(handler, env, progress.newChild(workByHandler)));
        }
        catch (OperationCanceledException e) {
          doCancel();
          throw e; // is handled by job manager
        }
        catch (RuntimeException e) {
          SdkLog.error("Error while: {}", handlerName, e);
        }

        if (i % 500 == 0) {
          // flush derived resources to disk in blocks of 500 items. this prevents out-of-memory in large workspaces
          currentWorkingCopyManager().checkpoint(null);
        }
      }
      Future.awaitAll(executedHandlers); // wait until all write operations are executed. otherwise the java environment might already be closed while writing jobs are using it
    }

    private static Collection<? extends IFuture<?>> executeHandler(BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>> handler, IEnvironment env, IProgress progress) {
      long start = System.currentTimeMillis();
      try {
        return handler.apply(env, progress);
      }
      finally {
        SdkLog.debug("Derived Resource Handler ({}) took {}ms to execute.", handler.toString(), System.currentTimeMillis() - start);
      }
    }
  }

  public static final class RunTriggerHandlersJobRule implements ISchedulingRule {

    public static final ISchedulingRule INSTANCE = new RunTriggerHandlersJobRule();

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
