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
package org.eclipse.scout.sdk.operation.form.formdata;

import java.util.HashSet;

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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link FormDataAutoUpdater}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 18.01.2011
 */
public class FormDataAutoUpdater {
  public static final String PROP_FORMDATA_AUTO_UPDATE = ScoutSdk.PLUGIN_ID + ".propFormdataAutoUpdate";
  public static final String AUTO_UPDATE_JOB_FAMILY = "AUTO_UPDATE_JOB_FAMILY";

  private P_ResourceChangedListener m_resourceChangedListener;

  private Object pendingJobsLock = new Object();
  private HashSet<ICompilationUnit> m_pendingCompilationUnits;
  private P_UpdateFormDataJob m_updateJob = null;
  private boolean m_enabled;

  public FormDataAutoUpdater() {
    m_pendingCompilationUnits = new HashSet<ICompilationUnit>();
  }

  public void dispose() {
    setEnabled(false);
  }

  private void handleCompilationUnitSaved(ICompilationUnit icu) {
    synchronized (pendingJobsLock) {
      if (m_pendingCompilationUnits.add(icu)) {
        if (m_updateJob == null) {
          m_updateJob = new P_UpdateFormDataJob();
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
    m_enabled = enabled;
    if (m_enabled) {
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
    return m_enabled;
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

  private class P_UpdateFormDataJob extends Job {

    public P_UpdateFormDataJob() {
      super("Updating form data");
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
      ICompilationUnit[] compilationUnits = null;
      synchronized (pendingJobsLock) {
        compilationUnits = m_pendingCompilationUnits.toArray(new ICompilationUnit[m_pendingCompilationUnits.size()]);
        m_pendingCompilationUnits.clear();
        m_updateJob = null;
      }
      int totalCompilationUnits = compilationUnits.length;
      if (totalCompilationUnits > 0) {
        monitor.beginTask(getName(), totalCompilationUnits);
        for (int i = 0; i < totalCompilationUnits; i++) {
          if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
          }
          monitor.setTaskName("Update form data [" + i + " of " + totalCompilationUnits + "]");
          ICompilationUnit icu = compilationUnits[i];
          if (TypeUtility.exists(icu)) {
            try {
              IType[] types = icu.getTypes();
              if (types.length > 0) {
                IType type = types[0];
                FormDataAnnotation annotatation = ScoutTypeUtility.findFormDataAnnotation(type, TypeUtility.getSuperTypeHierarchy(type));
                if (annotatation != null && FormDataAnnotation.isSdkCommandCreate(annotatation) &&
                    !StringUtility.isNullOrEmpty(annotatation.getFormDataTypeSignature())) {
                  monitor.subTask("update '" + type.getFullyQualifiedName() + "'.");
                  FormDataUpdateJob formDataUpdateJob = new FormDataUpdateJob(new FormDataUpdateOperation(type, annotatation));
                  formDataUpdateJob.schedule();
                  formDataUpdateJob.join();
                }
              }
            }
            catch (Exception e) {
              ScoutSdk.logWarning("could not determ type for form data update '" + icu.getElementName() + "'.", e);
            }
          }
          monitor.worked(1);
        }
      }
      return Status.OK_STATUS;
    }
  }
}