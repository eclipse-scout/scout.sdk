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
package org.eclipse.scout.sdk.s2e.internal.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateHandler;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateManager;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.job.JobEx;
import org.eclipse.scout.sdk.s2e.job.OperationJob;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link DtoAutoUpdateManager}</h3>
 *
 * @author Matthias Villiger
 * @author Andreas Hoegger
 * @since 3.10.0 15.08.2013
 */
public class DtoAutoUpdateManager implements IDtoAutoUpdateManager {

  public static final String AUTO_UPDATE_JOB_FAMILY = "AUTO_UPDATE_JOB_FAMILY";
  public static final String JAVA_DELTA_CHECK_JOB_FAMILY = "JAVA_DELTA_CHECK_JOB_FAMILY";

  private final AtomicBoolean m_enabled;
  private final List<IDtoAutoUpdateHandler> m_updateHandlers;

  private P_JavaChangeListener m_javaChangeListener;

  // queue that buffers all java change events that need processing
  private final ArrayBlockingQueue<ElementChangedEvent> m_javaChangeEventsToCheck;
  // job that works through all buffered java change events and checks if they contain DTO update relevant compilation units
  private final P_JavaChangeEventCheckJob m_javaDeltaCheckJob;

  // queue that buffers all dto update operations that need to be executed
  private final ArrayBlockingQueue<IDtoAutoUpdateOperation> m_dtoUpdateOperations;
  // job that executes all the buffered dto update operations (visible to the user)
  private final P_AutoUpdateOperationsJob m_autoUpdateJob;

  public DtoAutoUpdateManager() {
    m_enabled = new AtomicBoolean(true);
    m_updateHandlers = new ArrayList<>();

    m_javaChangeEventsToCheck = new ArrayBlockingQueue<>(5000, true);
    m_dtoUpdateOperations = new ArrayBlockingQueue<>(2000, true);

    m_autoUpdateJob = new P_AutoUpdateOperationsJob(m_dtoUpdateOperations);
    m_javaDeltaCheckJob = new P_JavaChangeEventCheckJob(m_updateHandlers, m_javaChangeEventsToCheck, m_dtoUpdateOperations, m_autoUpdateJob);
  }

  /**
   * Shutdown the manager. Afterwards no auto updates are performed. All listeners are removed and all jobs will be
   * cancelled.
   */
  public void dispose() {
    setEnabled(false);

    // wait until all form datas have been generated. otherwise the user ends up with invalid form datas.
    // the user still can cancel the job if desired.
    JobEx.waitForJobFamily(AUTO_UPDATE_JOB_FAMILY);
  }

  @Override
  public void addModelDataUpdateHandler(IDtoAutoUpdateHandler factory) {
    m_updateHandlers.add(factory);
  }

  @Override
  public List<IDtoAutoUpdateHandler> getUpdateHandlers() {
    return new ArrayList<>(m_updateHandlers);
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
   * If the thread is interrupted to often while waiting for space in the queue it gives up.
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
  private static <T> boolean addElementToQueueSecure(ArrayBlockingQueue<T> queue, T element, long timeout, TimeUnit unit) {
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
    return false; // we had to much interrupts. we don't want to wait any longer (no endless looping).
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
      final String[] EXCLUDED_JOB_NAME_PREFIXES = new String[]{
          "org.eclipse.team.", // excludes svn updates
          "org.eclipse.core.internal.events.NotificationManager.NotifyJob", // excludes annotation processing updates
          "org.eclipse.egit.", // excludes git updates
          "org.eclipse.core.internal.events.AutoBuildJob", // exclude annotation processing updates
          "org.eclipse.m2e.", // maven updates
          "org.eclipse.jdt.internal.core.ExternalFoldersManager.RefreshJob" // refresh of external folders after svn update
      };

      Job curJob = Job.getJobManager().currentJob();
      if (curJob == null) {
        return false;
      }

      if (curJob instanceof OperationJob || curJob instanceof P_AutoUpdateOperationsJob) {
        return false;
      }

      String jobFqn = curJob.getClass().getName().replace('$', '.');
      for (String excludedPrefix : EXCLUDED_JOB_NAME_PREFIXES) {
        if (jobFqn.startsWith(excludedPrefix)) {
          return false;
        }
      }

      return true;
    }

    @Override
    public void elementChanged(ElementChangedEvent event) {
      if (event != null && acceptUpdateEvent(event)) {
        if (!addElementToQueueSecure(m_eventCollector, event, 10, TimeUnit.SECONDS)) {
          // element could not be added within the given timeout
          S2ESdkActivator.logWarning("No more space in the Scout DTO auto update event queue. Skipping event.");
        }
      }
    }
  }

  /**
   * Job that iterates over all java change events and checks if they require a DTO update.
   */
  private static final class P_JavaChangeEventCheckJob extends JobEx {

    private final List<IDtoAutoUpdateHandler> m_handlers;
    private final ArrayBlockingQueue<ElementChangedEvent> m_queueToConsume;
    private final ArrayBlockingQueue<IDtoAutoUpdateOperation> m_operationCollector;
    private final P_AutoUpdateOperationsJob m_dtoUpdateJob;

    private P_JavaChangeEventCheckJob(List<IDtoAutoUpdateHandler> handlers, ArrayBlockingQueue<ElementChangedEvent> queueToConsume, ArrayBlockingQueue<IDtoAutoUpdateOperation> operationCollector, P_AutoUpdateOperationsJob autoUpdateJob) {
      super("Check if java deltas require a Scout DTO update");
      setSystem(true);
      setUser(false);
      setPriority(DECORATE);
      m_handlers = handlers;
      m_queueToConsume = queueToConsume;
      m_operationCollector = operationCollector;
      m_dtoUpdateJob = autoUpdateJob;
    }

    @Override
    public boolean belongsTo(Object family) {
      return JAVA_DELTA_CHECK_JOB_FAMILY.equals(family);
    }

    /**
     * Creates {@link IDtoAutoUpdateOperation}s for the given type using {@link IDtoAutoUpdateHandler}. All handers are
     * requested for providing update operations. The first one is returned.
     *
     * @throws CoreException
     */
    private IDtoAutoUpdateOperation createAutoUpdateOperation(IType t) throws CoreException {
      org.eclipse.scout.sdk.core.model.IType modelType = JdtUtils.jdtTypeToScoutType(t);
      for (IDtoAutoUpdateHandler handler : m_handlers) {
        IDtoAutoUpdateOperation operation = handler.createUpdateOperation(modelType, t.getJavaProject().getProject());
        if (operation != null) {
          return operation;
        }
      }
      return null;
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
          for (ICompilationUnit icu : icus) {
            try {
              for (IType t : icu.getTypes()) {
                IDtoAutoUpdateOperation operation = createAutoUpdateOperation(t);
                if (operation != null) {
                  if (!m_operationCollector.contains(operation)) {
                    if (addElementToQueueSecure(m_operationCollector, operation, -1, null)) {
                      // do the scheduling after the first icu is parsed (not parsing all and then scheduling)
                      // this way the user has a faster response time and we can already start in parallel (even though we may abort again).
                      m_dtoUpdateJob.abort();
                      m_dtoUpdateJob.schedule(1000); // wait a little to give other follow-up events time so that they don't trigger another re-calculation job
                    }
                    else {
                      S2ESdkActivator.logWarning("To many thread interrupts while waiting for space in the Scout DTO auto update event queue. Skipping type '" + t.getFullyQualifiedName('$') + "'.");
                    }
                  }
                }
              }
            }
            catch (Exception e) {
              S2ESdkActivator.logError("Unable to handle event for compilation unit '" + icu.getElementName() + "'.", e);
            }
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
      if (elementType == IJavaElement.COMPILATION_UNIT) {
        collector.add((ICompilationUnit) curElement);
      }

      boolean hasChildren = (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
      boolean processChildren = hasChildren && (curElement == null || curElement.getElementType() < IJavaElement.COMPILATION_UNIT); // stop at compilation unit level
      if (processChildren) {
        IJavaElementDelta[] affectedChildren = delta.getAffectedChildren();
        for (IJavaElementDelta childDelta : affectedChildren) {
          collectCompilationUnitsFromDelta(childDelta, collector);
        }
      }
    }
  }

  /**
   * Job that executes all dto update operations that have been discovered.
   */
  private static final class P_AutoUpdateOperationsJob extends Job {

    private final ArrayBlockingQueue<IDtoAutoUpdateOperation> m_queueToConsume;
    private boolean m_isAborted;

    private P_AutoUpdateOperationsJob(ArrayBlockingQueue<IDtoAutoUpdateOperation> queueToConsume) {
      super("Auto-updating derived resources");
      setRule(DtoAutoUpdateJobRule.INSTANCE);
      setPriority(Job.DECORATE);
      m_isAborted = false;
      m_queueToConsume = queueToConsume;
    }

    @Override
    public boolean belongsTo(Object family) {
      return AUTO_UPDATE_JOB_FAMILY.equals(family);
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
        IDtoAutoUpdateOperation operation = m_queueToConsume.poll();
        try {
          org.eclipse.scout.sdk.core.model.IType type = operation.getModelType();
          monitor.setTaskName("Updating derived resources for '" + type.getSimpleName() + "' [" + i + " of " + numOperations + "]");
          monitor.subTask("update '" + type.getName() + "'.");
          operation.validate();
          operation.run(monitor, null);
        }
        catch (Exception e) {
          S2ESdkActivator.logError("Error while updating DTO for '" + operation.getModelType().getName() + "'.", e);
        }
        monitor.worked(1);
      }

      return Status.OK_STATUS;
    }
  }

  public static final class DtoAutoUpdateJobRule implements ISchedulingRule {

    public static final DtoAutoUpdateJobRule INSTANCE = new DtoAutoUpdateJobRule();

    private DtoAutoUpdateJobRule() {
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
