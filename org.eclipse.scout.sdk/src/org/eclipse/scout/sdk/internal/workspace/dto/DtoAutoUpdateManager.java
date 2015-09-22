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
package org.eclipse.scout.sdk.internal.workspace.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.DtoUpdateProperties;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateEventFilter;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateHandler;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateManager;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateOperation;

/**
 * <h3>{@link DtoAutoUpdateManager}</h3>
 *
 * @author Matthias Villiger
 * @author Andreas Hoegger
 * @since 3.10.0 15.08.2013
 */
public class DtoAutoUpdateManager implements IDtoAutoUpdateManager {

  public static final String AUTO_UPDATE_JOB_FAMILY = "AUTO_UPDATE_JOB_FAMILY";
  public static final String RESOURCE_DELTA_CHECK_JOB_FAMILY = "RESOURCE_DELTA_CHECK_JOB_FAMILY";

  private final AtomicBoolean m_enabled;
  private final List<IDtoAutoUpdateHandler> m_updateHandlers;

  private P_ResourceChangedListener m_resourceChangedListener;

  // queue that buffers all resource change events that need processing
  private final ArrayBlockingQueue<IResourceChangeEvent> m_resourceChangeEventsToCheck;
  // job that works through all buffered resource change events and checks if they contain DTO update relevant compilation units
  private final P_ResourceChangeEventCheckJob m_resourceDeltaCheckJob;

  // queue that buffers all dto update operations that need to be executed
  private final ArrayBlockingQueue<IDtoAutoUpdateOperation> m_dtoUpdateOperations;
  // job that executes all the buffered dto update operations (visible to the user)
  private final P_AutoUpdateOperationsJob m_autoUpdateJob;

  public DtoAutoUpdateManager() {
    m_enabled = new AtomicBoolean(true);
    m_updateHandlers = new ArrayList<IDtoAutoUpdateHandler>();

    m_resourceChangeEventsToCheck = new ArrayBlockingQueue<IResourceChangeEvent>(5000, true);
    m_dtoUpdateOperations = new ArrayBlockingQueue<IDtoAutoUpdateOperation>(2000, true);

    m_autoUpdateJob = new P_AutoUpdateOperationsJob(m_dtoUpdateOperations);
    m_resourceDeltaCheckJob = new P_ResourceChangeEventCheckJob(m_updateHandlers, m_resourceChangeEventsToCheck, m_dtoUpdateOperations, m_autoUpdateJob);
  }

  /**
   * Shutdown the manager. Afterwards no auto updates are performed. All listeners are removed and all jobs will be
   * cancelled.
   */
  public void dispose() {
    setEnabled(false);

    // wait until all form datas have been generated. otherwise the user ends up with invalid form datas.
    // the user still can cancel the job if desired.
    JdtUtility.waitForJobFamily(AUTO_UPDATE_JOB_FAMILY);
  }

  @Override
  public void addModelDataUpdateHandler(IDtoAutoUpdateHandler factory) {
    m_updateHandlers.add(factory);
  }

  @Override
  public synchronized void setEnabled(boolean enabled) {
    m_enabled.set(enabled);
    if (enabled) {
      if (m_resourceChangedListener == null) {
        m_resourceChangedListener = new P_ResourceChangedListener(m_resourceChangeEventsToCheck);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceChangedListener);
      }
      m_resourceDeltaCheckJob.schedule();
    }
    else {
      if (m_resourceChangedListener != null) {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceChangedListener);
        m_resourceChangedListener = null;
      }

      // cancel the job that checks the resource deltas
      Thread thread = m_resourceDeltaCheckJob.getThread();
      if (thread != null) {
        m_resourceDeltaCheckJob.cancel();
        thread.interrupt();
        try {
          m_resourceDeltaCheckJob.join(3000);
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
   * The resource change listener that adds the given event to the queue to execute later on
   */
  private static final class P_ResourceChangedListener implements IResourceChangeListener {

    private final ArrayBlockingQueue<IResourceChangeEvent> m_eventCollector;

    private P_ResourceChangedListener(ArrayBlockingQueue<IResourceChangeEvent> eventCollector) {
      m_eventCollector = eventCollector;
    }

    private boolean acceptUpdateEvent(IResourceChangeEvent icu) {
      for (IDtoAutoUpdateEventFilter filter : DtoUpdateEventFilter.getFilters()) {
        try {
          if (!filter.accept(icu)) {
            return false;
          }
        }
        catch (Exception e) {
          ScoutSdk.logError("Unable to apply DTO auto update event filter '" + filter.getClass().getName() + "'. Filter is skipped.", e);
        }
      }
      return true;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      if (event != null && acceptUpdateEvent(event)) {
        if (!addElementToQueueSecure(m_eventCollector, event, 10, TimeUnit.SECONDS)) {
          // element could not be added within the given timeout
          ScoutSdk.logWarning("No more space in the Scout DTO auto update event queue. Skipping event.");
        }
      }
    }
  } // end class P_ResourceChangedListener

  /**
   * Job that iterates over all resource change events and checks if they require a DTO update.
   */
  private static final class P_ResourceChangeEventCheckJob extends JobEx {

    private final List<IDtoAutoUpdateHandler> m_handlers;
    private final ArrayBlockingQueue<IResourceChangeEvent> m_queueToConsume;
    private final ArrayBlockingQueue<IDtoAutoUpdateOperation> m_operationCollector;
    private final P_AutoUpdateOperationsJob m_dtoUpdateJob;

    private P_ResourceChangeEventCheckJob(List<IDtoAutoUpdateHandler> handlers, ArrayBlockingQueue<IResourceChangeEvent> queueToConsume, ArrayBlockingQueue<IDtoAutoUpdateOperation> operationCollector, P_AutoUpdateOperationsJob autoUpdateJob) {
      super("Check if resource deltas require a Scout DTO update");
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
      return RESOURCE_DELTA_CHECK_JOB_FAMILY.equals(family);
    }

    /**
     * Creates {@link IDtoAutoUpdateOperation}s for the types of the given {@link ICompilationUnit} using
     * {@link IAutoUpdateHandler}. All handers are requested for providing update operations.
     *
     * @param icu
     * @return Returns a non-empty set of {@link IDtoAutoUpdateOperation}s or <code>null</code> if there are no derived
     *         resources.
     * @throws CoreException
     */
    private Set<IDtoAutoUpdateOperation> createAutoUpdateOperations(ICompilationUnit icu) {
      if (!TypeUtility.exists(icu)) {
        return null;
      }

      IType[] types = null;
      try {
        types = icu.getTypes();
      }
      catch (JavaModelException e) {
        ScoutSdk.logError("Unable to get types of compilation unit '" + icu.getElementName() + "'. Cannot calculate if a Scout DTO update is required. ", e);
        return null;
      }

      Set<IDtoAutoUpdateOperation> operations = null;
      for (IType type : types) {
        DtoUpdateProperties properties = new DtoUpdateProperties();
        properties.setType(type);
        properties.setSuperTypeHierarchy(TypeUtility.getSupertypeHierarchy(type));
        for (IDtoAutoUpdateHandler handler : m_handlers) {
          try {
            IDtoAutoUpdateOperation operation = handler.createUpdateOperation(properties);
            if (operation != null) {
              if (operations == null) {
                operations = new HashSet<IDtoAutoUpdateOperation>();
              }
              operations.add(operation);
            }
          }
          catch (CoreException e) {
            ScoutSdk.logError("Could not evaluate auto update handler '" + handler.getClass().getName() + "'.", e);
          }
        }
      }
      return operations;
    }

    private List<ICompilationUnit> getCompilationUnitsFromDelta(IResourceDelta d) {
      final LinkedList<ICompilationUnit> collector = new LinkedList<ICompilationUnit>();
      try {
        d.accept(new IResourceDeltaVisitor() {
          @Override
          public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            if (resource != null && resource.getType() == IResource.FILE) {
              if ((delta.getFlags() & IResourceDelta.CONTENT) != 0 && resource.getName().endsWith(".java")) {
                IJavaElement javaElement = JavaCore.create((IFile) resource);
                if (javaElement != null && javaElement.getElementType() == IJavaElement.COMPILATION_UNIT && javaElement.exists()) {
                  ICompilationUnit icu = (ICompilationUnit) javaElement;
                  collector.add(icu);
                }
              }
              return false;
            }
            return true;
          }
        });
      }
      catch (CoreException e) {
        ScoutSdk.logError("Could not calculate the compilation units affected by a resource change event. Unable to determine Scout DTOs to update.", e);
      }

      return collector;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      while (!monitor.isCanceled()) {
        IResourceChangeEvent event = null;
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
          List<ICompilationUnit> icus = getCompilationUnitsFromDelta(event.getDelta());
          for (ICompilationUnit icu : icus) {
            Set<IDtoAutoUpdateOperation> operations = createAutoUpdateOperations(icu);
            if (operations != null) {
              boolean modified = false;
              for (IDtoAutoUpdateOperation op : operations) {
                if (!m_operationCollector.contains(op)) {
                  if (addElementToQueueSecure(m_operationCollector, op, -1, null)) {
                    modified = true;
                  }
                  else {
                    ScoutSdk.logWarning("To many thread interrupts while waiting for space in the Scout DTO auto update event queue. Skipping compilation unit '" + icu.getElementName() + "'.");
                  }
                }
              }

              // do the scheduling after the first icu is parsed (not parsing all and then scheduling)
              // this way the user has a faster response time and we can already start in parallel (even though we may abort again).
              if (modified) {
                m_dtoUpdateJob.abort();
                m_dtoUpdateJob.schedule(1000); // wait a little to give other follow-up events time so that they don't trigger another re-calculation job
              }
            }
          }
        }
      }
      return Status.CANCEL_STATUS;
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
          IType type = operation.getModelType();
          monitor.setTaskName("Updating derived resources for '" + type.getElementName() + "' [" + i + " of " + numOperations + "]");
          monitor.subTask("update '" + type.getFullyQualifiedName() + "'.");
          operation.validate();
          operation.run(monitor, null);
        }
        catch (Exception e) {
          ScoutSdk.logWarning("Error while updating model data for '" + operation.getModelType().getElementName() + "'.", e);
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