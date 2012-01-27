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
package org.eclipse.scout.sdk.ui.internal.fields.table;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.EventListenerList;

/**
 *
 */
public abstract class AbstractColumnProvider implements IColumnProvider {

  private EventListenerList m_listeners = new EventListenerList();

  @Override
  public void dispose() {
  }

  @Override
  public void addLayoutUpdateListener(LayoutUpdateListener listener) {
    m_listeners.add(LayoutUpdateListener.class, listener);
  }

  @Override
  public void removeLayoutUpdateListener(LayoutUpdateListener listener) {
    m_listeners.remove(LayoutUpdateListener.class, listener);
  }

  protected void scheduleUpdateLayout() {
    synchronized (m_updateJobLock) {
      m_updateLayoutJob = new P_UpdateLayoutJob();
      m_updateLayoutJob.schedule(300);
    }
  }

  private void fireUpdateLayout() {
    for (LayoutUpdateListener l : m_listeners.getListeners(LayoutUpdateListener.class)) {
      l.updateLayout();
    }
  }

  private Job m_updateLayoutJob;
  private Object m_updateJobLock = new Object();

  private class P_UpdateLayoutJob extends Job {
    /**
     * @param name
     */
    public P_UpdateLayoutJob() {
      super(P_UpdateLayoutJob.class.getName());
      setSystem(true);
      setPriority(Job.DECORATE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      synchronized (m_updateJobLock) {
        if (m_updateLayoutJob != this) {
          return Status.CANCEL_STATUS;
        }
      }
      if (getViewer().getControl() != null && !getViewer().getControl().isDisposed()) {
        getViewer().getControl().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            fireUpdateLayout();
          }
        });
      }
      return Status.OK_STATUS;
    }
  }
}
