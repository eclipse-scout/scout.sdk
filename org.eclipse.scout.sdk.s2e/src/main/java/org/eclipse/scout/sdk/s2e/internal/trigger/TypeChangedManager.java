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
package org.eclipse.scout.sdk.s2e.internal.trigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.trigger.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.trigger.ITypeChangedHandler;
import org.eclipse.scout.sdk.s2e.trigger.ITypeChangedManager;
import org.eclipse.scout.sdk.s2e.trigger.ITypeChangedOperation;

/**
 * <h3>{@link TypeChangedManager}</h3>
 *
 * @author Matthias Villiger
 * @author Andreas Hoegger
 * @since 3.10.0 15.08.2013
 */
public class TypeChangedManager implements ITypeChangedManager {

  public static final String TYPE_CHANGED_TRIGGER_JOB_FAMILY = "AUTO_UPDATE_JOB_FAMILY";
  public static final String JAVA_DELTA_CHECK_JOB_FAMILY = "JAVA_DELTA_CHECK_JOB_FAMILY";

  private final AtomicBoolean m_enabled;
  private final List<ITypeChangedHandler> m_updateHandlers;

  private P_JavaChangeListener m_javaChangeListener;

  // queue that buffers all java change events that need processing
  private final ArrayBlockingQueue<ElementChangedEvent> m_javaChangeEventsToCheck;
  // job that works through all buffered java change events and checks if they contain trigger relevant compilation units
  private final P_JavaChangeEventCheckJob m_javaDeltaCheckJob;

  // queue that buffers all trigger operations that need to be executed
  private final ArrayBlockingQueue<ITypeChangedOperation> m_triggerOperations;
  // job that executes all the buffered trigger operations (visible to the user)
  private final P_RunQueuedTriggerOperationsJob m_runTriggerOperationsJob;

  public TypeChangedManager() {
    m_enabled = new AtomicBoolean(true);
    m_updateHandlers = new ArrayList<>();

    m_javaChangeEventsToCheck = new ArrayBlockingQueue<>(5000, true);
    m_triggerOperations = new ArrayBlockingQueue<>(2000, true);

    m_runTriggerOperationsJob = new P_RunQueuedTriggerOperationsJob(m_triggerOperations);
    m_javaDeltaCheckJob = new P_JavaChangeEventCheckJob(this, m_javaChangeEventsToCheck, m_triggerOperations, m_runTriggerOperationsJob);
  }

  /**
   * Shutdown the manager. Afterwards no auto updates are performed. All listeners are removed and all jobs will be
   * cancelled.
   */
  public void dispose() {
    setEnabled(false);

    // wait until all form datas have been generated. otherwise the user ends up with invalid form datas.
    // the user still can cancel the job if desired.
    AbstractJob.waitForJobFamily(TYPE_CHANGED_TRIGGER_JOB_FAMILY);
  }

  @Override
  public void addTypeChangedHandler(ITypeChangedHandler handler) {
    m_updateHandlers.add(handler);
  }

  @Override
  public void removeTypeChangedHandler(ITypeChangedHandler handler) {
    m_updateHandlers.remove(handler);
  }

  @Override
  public void trigger(IType jdtType) {
    IJavaEnvironmentProvider envProvider = new CachingJavaEnvironmentProvider();
    for (ITypeChangedOperation operation : createOperations(jdtType, envProvider)) {
      if (addElementToQueueSecure(m_triggerOperations, operation, operation.getOperationName(), -1, null)) {
        //ok
      }
      else {
        return;
      }
    }
  }

  @Override
  public void triggerAll(IJavaSearchScope scope) {
    new P_RunAllTriggerOperationsJob(this, scope).schedule();
  }

  protected Collection<ITypeChangedOperation> createOperations(IType t, IJavaEnvironmentProvider envProvider) {
    ArrayList<ITypeChangedOperation> all = null;
    for (ITypeChangedHandler handler : m_updateHandlers) {
      try {
        List<ITypeChangedOperation> ops = handler.createOperations(t, envProvider);
        if (ops != null && !ops.isEmpty()) {
          all = (all != null ? all : new ArrayList<ITypeChangedOperation>());
          all.addAll(ops);
        }
      }
      catch (Throwable e) {
        S2ESdkActivator.logError("Unable to create operation with handler '" + handler.getClass() + "'.", e);
      }
    }
    return all != null ? all : Collections.<ITypeChangedOperation> emptyList();
  }

  protected Collection<ITypeChangedOperation> createAllOperations(IJavaSearchScope scope, IJavaEnvironmentProvider envProvider) {
    ArrayList<ITypeChangedOperation> all = null;
    for (ITypeChangedHandler handler : m_updateHandlers) {
      try {
        List<ITypeChangedOperation> ops = handler.createAllOperations(scope, envProvider);
        if (ops != null && !ops.isEmpty()) {
          all = (all != null ? all : new ArrayList<ITypeChangedOperation>());
          all.addAll(ops);
        }
      }
      catch (Throwable e) {
        S2ESdkActivator.logError("Error while collecting all types from '" + handler.getClass() + "'.", e);
      }
    }
    return all != null ? all : Collections.<ITypeChangedOperation> emptyList();
  }

  @Override
  public synchronized void setEnabled(boolean enabled) {
    m_enabled.set(enabled);
    if (enabled) {
      if (m_javaChangeListener == null) {
        m_javaChangeListener = new P_JavaChangeListener(m_javaChangeEventsToCheck);
        JavaCore.addElementChangedListener(m_javaChangeListener, ElementChangedEvent.POST_CHANGE);
      }
      m_javaDeltaCheckJob.schedule();
    }
    else {
      if (m_javaChangeListener != null) {
        JavaCore.removeElementChangedListener(m_javaChangeListener);
        m_javaChangeListener = null;
      }

      // cancel the job that checks the java deltas
      Thread thread = m_javaDeltaCheckJob.getThread();
      if (thread != null) {
        m_javaDeltaCheckJob.cancel();
        thread.interrupt();
        try {
          m_javaDeltaCheckJob.join(3000);
        }
        catch (InterruptedException e) {
        }
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return m_enabled.get();
  }

  /**
   * Securely inserts the given element in the given queue.<br>
   * If the thread is interrupted too often while waiting for space in the queue it gives up.
   *
   * @param queue
   *          The queue to insert to
   * @param element
   *          The element to insert
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
  private static <T> boolean addElementToQueueSecure(ArrayBlockingQueue<T> queue, T element, String name, long timeout, TimeUnit unit) {
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
    S2ESdkActivator.logWarning("Too many thread interrupts while waiting for space in the scout trigger queue. Skipping '" + name + "'.");
    return false; // we had too many interrupts. we don't want to wait any longer (no endless looping).
  }

  /**
   * The java change listener that adds the given event to the queue to execute later on
   */
  private static final class P_JavaChangeListener implements IElementChangedListener {

    private final ArrayBlockingQueue<ElementChangedEvent> m_eventCollector;

    private P_JavaChangeListener(ArrayBlockingQueue<ElementChangedEvent> eventCollector) {
      m_eventCollector = eventCollector;
    }

    private static boolean acceptUpdateEvent(ElementChangedEvent icu) {
      final String[] excludedJobNamePrefixes = new String[]{"org.eclipse.team.", // excludes svn updates
          "org.eclipse.core.internal.events.NotificationManager.NotifyJob", // excludes annotation processing updates
          "org.eclipse.egit.", // excludes git updates
          "org.eclipse.core.internal.events.AutoBuildJob", // exclude annotation processing updates
          "org.eclipse.m2e.", // maven updates
          "org.eclipse.jdt.internal.core.ExternalFoldersManager.RefreshJob", // refresh of external folders after svn update
          "org.eclipse.core.internal.refresh.RefreshJob" // refresh after git import
      };

      Job curJob = Job.getJobManager().currentJob();
      if (curJob == null) {
        return false;
      }

      if (curJob instanceof AbstractJob || curJob instanceof P_RunQueuedTriggerOperationsJob) {
        // do not automatically update on Scout SDK changes. We expect the SDK to trigger manually where required.
        return false;
      }

      if (curJob.belongsTo(ResourcesPlugin.FAMILY_AUTO_BUILD) || curJob.belongsTo(ResourcesPlugin.FAMILY_MANUAL_BUILD)) {
        // ignore build changes (annotation processing)
        return false;
      }

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
    public void elementChanged(ElementChangedEvent event) {
      if (event != null && acceptUpdateEvent(event) && !addElementToQueueSecure(m_eventCollector, event, event.toString(), 10, TimeUnit.SECONDS)) {
        // element could not be added within the given timeout
      }
    }
  }

  /**
   * Job that iterates over all java change events and checks if they require a update.
   */
  private static final class P_JavaChangeEventCheckJob extends AbstractJob {

    private final TypeChangedManager m_manager;
    private final ArrayBlockingQueue<ElementChangedEvent> m_queueToConsume;
    private final ArrayBlockingQueue<ITypeChangedOperation> m_operationCollector;
    private final P_RunQueuedTriggerOperationsJob m_runTriggerOperationsJob;

    private P_JavaChangeEventCheckJob(TypeChangedManager manager, ArrayBlockingQueue<ElementChangedEvent> queueToConsume, ArrayBlockingQueue<ITypeChangedOperation> operationCollector,
        P_RunQueuedTriggerOperationsJob runtriggerOperationsJob) {
      super("Check if java deltas triggers a " + ITypeChangedHandler.class.getSimpleName());
      setSystem(true);
      setUser(false);
      setPriority(DECORATE);
      m_manager = manager;
      m_queueToConsume = queueToConsume;
      m_operationCollector = operationCollector;
      m_runTriggerOperationsJob = runtriggerOperationsJob;
    }

    @Override
    public boolean belongsTo(Object family) {
      return JAVA_DELTA_CHECK_JOB_FAMILY.equals(family);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      while (!monitor.isCanceled()) {
        ElementChangedEvent event = null;
        try {
          event = m_queueToConsume.take(); // blocks until deltas are available
        }
        catch (InterruptedException e1) {
        }
        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }
        if (event != null && event.getDelta() != null) {
          // collect all operations for the compilation units within the delta
          Set<ICompilationUnit> icus = new HashSet<>();
          collectCompilationUnitsFromDelta(event.getDelta(), icus);
          if (addElementsToQueue(icus)) {
            m_runTriggerOperationsJob.abort();
            m_runTriggerOperationsJob.schedule(1000); // wait a little to give other follow-up events time so that they don't trigger another re-calculation job
          }
        }
      }
      return Status.CANCEL_STATUS;
    }

    private void collectCompilationUnitsFromDelta(IJavaElementDelta delta, Set<ICompilationUnit> collector) {
      IJavaElement curElement = delta.getElement();
      if (curElement == null) {
        return;
      }

      int elementType = curElement.getElementType();
      if (elementType == IJavaElement.COMPILATION_UNIT && curElement.exists()) {
        collector.add((ICompilationUnit) curElement);
      }

      boolean hasChildren = (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
      boolean processChildren = hasChildren && curElement.getElementType() < IJavaElement.COMPILATION_UNIT; // stop at compilation unit level
      if (processChildren) {
        IJavaElementDelta[] affectedChildren = delta.getAffectedChildren();
        for (IJavaElementDelta childDelta : affectedChildren) {
          collectCompilationUnitsFromDelta(childDelta, collector);
        }
      }
    }

    /**
     * @param icus
     * @return true if elements were added and job can be scheduled, false if the system is too busy
     */
    private boolean addElementsToQueue(Set<ICompilationUnit> icus) {
      CachingJavaEnvironmentProvider envProvider = new CachingJavaEnvironmentProvider();
      for (ICompilationUnit icu : icus) {
        try {
          for (IType t : icu.getTypes()) {
            for (ITypeChangedOperation operation : m_manager.createOperations(t, envProvider)) {
              if (!m_operationCollector.contains(operation)) {
                if (addElementToQueueSecure(m_operationCollector, operation, operation.getOperationName(), -1, null)) {
                  //ok, continue
                }
                else {
                  return false;
                }
              }
            }
          }
        }
        catch (Exception e) {
          S2ESdkActivator.logError("Unable to handle event for compilation unit '" + icu.getElementName() + "'.", e);
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Job that executes all trigger operations that have been enqueued, with lowest prio
   */
  private static final class P_RunQueuedTriggerOperationsJob extends Job {

    private final ArrayBlockingQueue<ITypeChangedOperation> m_queueToConsume;
    private boolean m_isAborted;

    private P_RunQueuedTriggerOperationsJob(ArrayBlockingQueue<ITypeChangedOperation> queueToConsume) {
      super("Auto-updating derived resources");
      setRule(RunTriggerOperationsJobRule.INSTANCE);
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

      monitor.beginTask(getName(), numOperations);
      for (int i = 1; i <= numOperations; i++) {
        if (monitor.isCanceled()) {
          return doCancel();
        }
        if (isAborted()) {
          return doAbort();
        }

        // already remove the operation here. if there is a problem with this operation we don't want to keep trying
        ITypeChangedOperation operation = m_queueToConsume.poll();
        try {
          monitor.setTaskName(operation.getOperationName() + "' [" + i + " of " + numOperations + "]");
          monitor.subTask(operation.getOperationName());
          operation.validate();
          operation.run(monitor);
        }
        catch (Throwable e) {
          S2ESdkActivator.logError("Error while '" + operation.getOperationName() + "'.", e);
        }
        monitor.worked(1);
      }
      return Status.OK_STATUS;
    }
  }

  /**
   * Job that executes some trigger operations with normal prio, not enqueued, immediately, cancellable.
   */
  private static final class P_RunAllTriggerOperationsJob extends Job {
    private TypeChangedManager m_manager;
    private IJavaSearchScope m_scope;

    private P_RunAllTriggerOperationsJob(TypeChangedManager manager, IJavaSearchScope scope) {
      super("Auto-updating derived resources");
      m_manager = manager;
      m_scope = scope;
      setUser(true);
      setRule(RunTriggerOperationsJobRule.INSTANCE);
    }

    @Override
    public boolean belongsTo(Object family) {
      return TYPE_CHANGED_TRIGGER_JOB_FAMILY.equals(family);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }

      monitor.setTaskName("Preparing update handlers...");
      IJavaEnvironmentProvider envProvider = new CachingJavaEnvironmentProvider();
      Collection<ITypeChangedOperation> ops = m_manager.createAllOperations(m_scope, envProvider);

      if (ops.size() < 1) {
        return Status.OK_STATUS;
      }

      monitor.beginTask(getName(), ops.size());
      for (ITypeChangedOperation operation : ops) {
        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }
        try {
          monitor.setTaskName(operation.getOperationName());
          monitor.subTask("");
          operation.validate();
          operation.run(monitor);
        }
        catch (Throwable e) {
          S2ESdkActivator.logError("Error while '" + operation.getOperationName() + "'.", e);
        }
        monitor.worked(1);
      }
      return Status.OK_STATUS;
    }
  }

  public static final class RunTriggerOperationsJobRule implements ISchedulingRule {

    public static final RunTriggerOperationsJobRule INSTANCE = new RunTriggerOperationsJobRule();

    private RunTriggerOperationsJobRule() {
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
