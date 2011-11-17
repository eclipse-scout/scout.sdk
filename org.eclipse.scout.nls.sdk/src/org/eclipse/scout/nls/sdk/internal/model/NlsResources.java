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
package org.eclipse.scout.nls.sdk.internal.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.util.concurrent.UiRunnable;
import org.eclipse.swt.widgets.Shell;

public class NlsResources {
  private static NlsResources s_instance;
  private HashMap<String, List<P_ResourceListener>> m_listeners = new HashMap<String, List<P_ResourceListener>>();

  private P_InternalResourceListener listener = new P_InternalResourceListener();

  private NlsResources() {
    ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
  }

  @Override
  protected void finalize() throws Throwable {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
    super.finalize();
  }

  public static final NlsResources getInstance() {
    if (s_instance == null) {
      s_instance = new NlsResources();
    }
    return s_instance;
  }

  public void addResourceListener(IResource resource, IResourceChangeListener l) {
    addResourceListener(resource, l, null);
  }

  public void addResourceListener(IResource resource, IResourceChangeListener l, Shell shell) {
    List<P_ResourceListener> list = getOrCreateFor(resource);
    list.add(new P_ResourceListener(l, shell));
  }

  public void removeResourceListener(IResource resource, IResourceChangeListener l) {
    List<P_ResourceListener> list = getOrCreateFor(resource);
    list.remove(new WeakReference<IResourceChangeListener>(l));

  }

  private List<P_ResourceListener> getOrCreateFor(IResource resource) {
    List<P_ResourceListener> list = m_listeners.get(resource);
    if (list == null) {
      list = new ArrayList<P_ResourceListener>(2);
      m_listeners.put(resource.toString(), list);
    }

    return list;
  }

  private class P_InternalResourceListener implements IResourceChangeListener {
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      IResourceDelta delta = event.getDelta();
      try {
        if (delta != null) {
          delta.accept(new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta d) {
              IResource resource = d.getResource();
              asyncStructureChanged(resource, d);
              return true;
            }
          });
        }
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
      }
    }

    public void asyncStructureChanged(IResource resource, IResourceDelta delta) {

      // get children till reaching the elelment

      List<P_ResourceListener> list = m_listeners.get(resource.toString());
      if (list != null) {
        List<P_ResourceListener> toRemove = new ArrayList<P_ResourceListener>(2);
        for (P_ResourceListener reference : list) {
          IResourceChangeListener l = reference.getListener();
          if (l == null) {
            // prepare to delete
            toRemove.add(reference);
          }
          else {
            IResourceChangeEvent event = new P_ResourceChangedEvent(resource, delta, -1);
            if (reference.getShell() != null && !reference.getShell().isDisposed()) {
              reference.getShell().getDisplay().asyncExec(new UiRunnable(new Object[]{l, event}) {
                @Override
                public void run() {
                  try {
                    ((IResourceChangeListener) p_args[0]).resourceChanged((IResourceChangeEvent) p_args[1]);
                  }
                  catch (Throwable e) {
                    NlsCore.logError("listener throwed an Exception", e);
                  }
                }
              });
            }
            try {
              l.resourceChanged(event);
            }
            catch (Throwable e) {
              NlsCore.logError("listener throwed an Exception", e);
            }

          }
        }
        for (P_ResourceListener reference : toRemove) {
          list.remove(reference);
        }
      }
    }

  }

  private class P_ResourceListener {
    private Shell m_shell;
    private WeakReference<IResourceChangeListener> m_listener;

    public P_ResourceListener(IResourceChangeListener listener, Shell shell) {
      m_shell = shell;
      m_listener = new WeakReference<IResourceChangeListener>(listener);
    }

    IResourceChangeListener getListener() {
      return m_listener.get();
    }

    Shell getShell() {
      return m_shell;
    }
  } // end P_ResourceListener

  private class P_ResourceChangedEvent implements IResourceChangeEvent {
    private IResource m_resource;
    private IResourceDelta m_delta;
    private int m_type;

    public P_ResourceChangedEvent(IResource resource, IResourceDelta delta, int type) {
      m_resource = resource;
      m_delta = delta;
      m_type = type;
    }

    @Override
    public IMarkerDelta[] findMarkerDeltas(String type, boolean includeSubtypes) {
      return null;
    }

    @Override
    public int getBuildKind() {
      return m_delta.getKind();
    }

    @Override
    public IResourceDelta getDelta() {
      return m_delta;
    }

    @Override
    public IResource getResource() {
      return m_resource;
    }

    @Override
    public Object getSource() {
      return NlsResources.this;
    }

    @Override
    public int getType() {
      return m_type;
    }

  } // end class P_ResourceChangedEvent

}
