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
package org.eclipse.scout.sdk.ui.internal.view.outline;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.job.LoadOutlineChildrenJob;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private IPage m_invisibleRoot;
  private boolean m_autoLoadChildren;
  private EventListenerList m_listener;
  private boolean m_loadSync = false;

  public ViewContentProvider() {
    m_autoLoadChildren = true;
    m_listener = new EventListenerList();
  }

  public void addContentProviderListener(IContentProviderListener listener) {
    m_listener.add(IContentProviderListener.class, listener);
  }

  public void removeContentProviderListener(IContentProviderListener listener) {
    m_listener.remove(IContentProviderListener.class, listener);
  }

  private void fireChildrenLoaded(IPage page) {
    for (IContentProviderListener l : m_listener.getListeners(IContentProviderListener.class)) {
      try {
        l.handleChildrenLoaded(page);
      }
      catch (Exception e) {
        ScoutSdkUi.logError("error during listerner notification.", e);
      }
    }
  }

  public void setAutoLoadChildren(boolean b) {
    m_autoLoadChildren = b;
  }

  public boolean isAutoLoadChildren() {
    return m_autoLoadChildren;
  }

  public void setRoot(IPage invisibleRoot) {
    m_invisibleRoot = invisibleRoot;
  }

  public IPage getRoot() {
    return m_invisibleRoot;
  }

  @Override
  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
  }

  @Override
  public void dispose() {
  }

  /**
   * root elements
   */
  @Override
  public Object[] getElements(Object parent) {
    return new Object[]{m_invisibleRoot};
  }

  @Override
  public Object getParent(Object child) {
    if (child instanceof IPage) {
      return ((IPage) child).getParent();
    }
    return null;
  }

  @Override
  public Object[] getChildren(Object parent) {
    if (parent instanceof IPage) {
      final IPage node = (IPage) parent;
      if (isAutoLoadChildren()) {
        if (!node.isChildrenLoaded()) {
          final LoadOutlineChildrenJob job = new LoadOutlineChildrenJob(node);
          if (isLoadSync()) {
            job.run(new NullProgressMonitor());
          }
          else {
            Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {
              @Override
              public void done(IJobChangeEvent event) {
                if (event.getJob() == job) {
                  Job.getJobManager().removeJobChangeListener(this);
                  ScoutSdkUi.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                      fireChildrenLoaded(node);
                    }
                  });
                }
              }
            });
            job.schedule();
            try {
              job.join(100);
            }
            catch (InterruptedException e) {
            }
            if (!job.isDone()) {
              return new Object[]{Texts.get("Loading")};
            }
          }
        }
      }
      return node.getChildArray();
    }
    return new Object[0];
  }

  @Override
  public boolean hasChildren(Object parent) {
    if (parent instanceof IPage) {
      IPage node = (IPage) parent;
      if (node.isChildrenLoaded()) {
        return node.hasChildren();
      }
      return true;
    }
    return false;
  }

  public void setLoadSync(boolean loadSync) {
    m_loadSync = loadSync;
  }

  public boolean isLoadSync() {
    return m_loadSync;
  }
}
