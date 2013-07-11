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
package org.eclipse.scout.sdk.operation.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * This class listens on workspace resource changes and uses {@link IAutoUpdateHandler} for generating resources that
 * are depending on other {@link IType}s.
 * 
 * @since 1.0.8
 */
public class AutoUpdateManager {
  public static final String PROP_AUTO_UPDATE = ScoutSdk.PLUGIN_ID + ".propAutoUpdate";
  public static final String AUTO_UPDATE_JOB_FAMILY = "AUTO_UPDATE_JOB_FAMILY";

  private final Object pendingJobsLock;
  private final AtomicBoolean m_enabled;
  private final Set<ICompilationUnit> m_pendingCompilationUnits;
  private final Set<IAutoUpdateOperation> m_pendingAutoUpdateOperations;
  private final List<IAutoUpdateHandler> m_updateHandlers;
  private final AtomicReference<IType> m_currentlyProcessedType;

  private P_ResourceChangedListener m_resourceChangedListener;
  private P_AutoUpdateOperationsJob m_updateJob = null;

  public AutoUpdateManager() {
    pendingJobsLock = new Object();
    m_enabled = new AtomicBoolean();
    m_pendingCompilationUnits = new HashSet<ICompilationUnit>();
    m_pendingAutoUpdateOperations = new HashSet<IAutoUpdateOperation>();
    m_updateHandlers = new ArrayList<IAutoUpdateHandler>();
    m_currentlyProcessedType = new AtomicReference<IType>();
  }

  public void dispose() {
    setEnabled(false);
  }

  public void addModelDataUpdateHandler(IAutoUpdateHandler factory) {
    m_updateHandlers.add(factory);
  }

  private void handleCompilationUnitSaved(ICompilationUnit icu) throws CoreException {
    Set<IAutoUpdateOperation> operations = createAutoUpdateOperations(icu);
    if (operations == null) {
      // nothing to create
      return;
    }
    synchronized (pendingJobsLock) {
      if (m_pendingCompilationUnits.add(icu)) {
        m_pendingAutoUpdateOperations.addAll(operations);
        if (m_updateJob == null) {
          m_updateJob = new P_AutoUpdateOperationsJob();
        }
        else {
          m_updateJob.cancel();
        }
        m_updateJob.schedule(500);
      }
    }
  }

  /**
   * @param enabled
   *          the enabled to set
   */
  public synchronized void setEnabled(boolean enabled) {
    m_enabled.set(enabled);
    if (enabled) {
      if (m_resourceChangedListener == null) {
        m_resourceChangedListener = new P_ResourceChangedListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceChangedListener);
      }
    }
    else {
      if (m_resourceChangedListener != null) {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceChangedListener);
        m_resourceChangedListener = null;
      }
    }
  }

  /**
   * @return the enabled
   */
  public boolean isEnabled() {
    return m_enabled.get();
  }

  /**
   * Creates {@link IAutoUpdateOperation}s for the types of the given {@link ICompilationUnit} using
   * {@link IAutoUpdateHandler}. All handers are requested for providing update operations.
   * 
   * @param icu
   * @return Returns a non-empty set of {@link IAutoUpdateOperation}s or <code>null</code> if there are no derived
   *         resources.
   * @throws CoreException
   */
  protected Set<IAutoUpdateOperation> createAutoUpdateOperations(ICompilationUnit icu) throws CoreException {
    if (!TypeUtility.exists(icu)) {
      return null;
    }

    IType currentlyProcessedType = m_currentlyProcessedType.get();
    Set<IAutoUpdateOperation> operations = null;
    for (IType type : icu.getTypes()) {
      if (currentlyProcessedType != null && currentlyProcessedType.equals(type)) {
        ScoutSdk.logInfo("Auto-update already in progress for '" + currentlyProcessedType.getElementName() + "'. Ignoring nested request.");
        continue;
      }
      ITypeHierarchy hierarchy = TypeUtility.getSuperTypeHierarchy(type);
      for (IAutoUpdateHandler handler : m_updateHandlers) {
        IAutoUpdateOperation operation = handler.createUpdateOperation(type, hierarchy);
        if (operation != null) {
          if (operations == null) {
            operations = new HashSet<IAutoUpdateOperation>();
          }
          operations.add(operation);
        }
      }
    }
    return operations;
  }

  public class P_ResourceChangedListener implements IResourceChangeListener {
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      if (event.getDelta() != null) {
        try {
          event.getDelta().accept(new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta delta) throws CoreException {
              IResource resource = delta.getResource();
              if (resource != null && resource.getType() == IResource.FILE) {
                if ((delta.getFlags() & IResourceDelta.CONTENT) != 0 && resource.getName().endsWith(".java")) {
                  IJavaElement javaElement = JavaCore.create((IFile) resource);
                  if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
                    handleCompilationUnitSaved((ICompilationUnit) javaElement);
                  }
                }
                return false;
              }
              return true;
            }
          });
        }
        catch (CoreException e) {
          ScoutSdk.logWarning("could not process resource change event '" + event.getResource() + "'.", e);
        }
      }
    }
  }// end class P_ResourceChangedListener

  private class P_AutoUpdateOperationsJob extends Job {

    public P_AutoUpdateOperationsJob() {
      super("Auto-updating derived resources");
      setRule(new AutoUpdateOperationsJobRule());
      setPriority(Job.DECORATE);
    }

    @Override
    public boolean belongsTo(Object family) {
      return AUTO_UPDATE_JOB_FAMILY.equals(family);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }
      Set<IAutoUpdateOperation> operations = null;
      synchronized (pendingJobsLock) {
        operations = new HashSet<IAutoUpdateOperation>(m_pendingAutoUpdateOperations);
        m_pendingAutoUpdateOperations.clear();
        m_pendingCompilationUnits.clear();
        m_updateJob = null;
      }
      if (operations.isEmpty()) {
        return Status.OK_STATUS;
      }

      monitor.beginTask(getName(), operations.size());

      int i = 0;
      for (IAutoUpdateOperation operation : operations) {
        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }

        i++;
        monitor.setTaskName("Updating derived resources for '" + operation.getModelType().getElementName() + "' [" + i + " of " + operations.size() + "]");

        try {
          IType type = operation.getModelType();
          m_currentlyProcessedType.set(type);
          monitor.subTask("update '" + type.getFullyQualifiedName() + "'.");
          operation.validate();
          operation.run(monitor, null);
        }
        catch (Exception e) {
          ScoutSdk.logWarning("Error while updating model data for '" + operation.getModelType().getElementName() + "'.", e);
        }
        finally {
          m_currentlyProcessedType.set(null);
        }
        monitor.worked(1);
      }

      return Status.OK_STATUS;
    }
  }

  public static class AutoUpdateOperationsJobRule implements ISchedulingRule {
    @Override
    public boolean contains(ISchedulingRule rule) {
      if (rule instanceof AutoUpdateOperationsJobRule) {
        return true;
      }
      return false;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
      if (rule instanceof AutoUpdateOperationsJobRule) {
        return true;
      }
      return false;
    }
  }
}
