/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class ManagedResource {

  /**
   * modification stamp to indicate that API changed resource to omit JDT Resource Changed notification.
   * Invoke {@link ManagedResource#setModificationStamp(long)} with this code prior to write resource to disk. It is
   * important to reset the modification stamp after having written to disk.
   */
  public static final int API_MODIFICATION_STAMP = Integer.MAX_VALUE;

  protected long m_modificationStamp;
  private IProject m_project;
  protected IFile m_file;
  protected Object m_fileLock;
  private Object m_registrationLock;

  private IResourceChangeListener m_jdtResourceChangedListener;
  private List<P_ResourceListenerEntry> m_resourceListeners;

  public ManagedResource(IProject project) {
    m_project = project;
    m_fileLock = new Object();
    m_registrationLock = new Object();
    m_resourceListeners = new ArrayList<P_ResourceListenerEntry>();
    m_jdtResourceChangedListener = new P_JdtResourceChangedListener();
  }

  private void attachJdtResourceChangeListener() {
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_jdtResourceChangedListener);
  }

  private void detachJdtResourceChangeListener() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_jdtResourceChangedListener);
  }

  /**
   * To receive notifications when this resource change.
   * 
   * @param listener
   */
  public void addResourceListener(IResourceListener listener) {
    addResourceListener(null, null, listener);
  }

  /**
   * To receive notifications when this resource change.
   * 
   * @param element
   *          To only receive notification that origin of the given element within the resource.
   * @param listener
   */
  public void addResourceListener(String element, IResourceListener listener) {
    addResourceListener(element, null, listener);
  }

  /**
   * To receive notifications when this resource change.
   * 
   * @param event
   *          the event to be interested in receiving notifications. This is <em>bitwise OR</em>'ing together event
   *          constants defined in {@link IResourceListener}.
   * @param listener
   */
  public void addResourceListener(Integer event, IResourceListener listener) {
    addResourceListener(null, event, listener);
  }

  /**
   * To receive notifications when this resource change.
   * 
   * @param element
   *          To only receive notification that origin of the given element within the resource.
   * @param event
   *          To only receive notification of the given event. This is <em>bitwise OR</em>'ing together event
   *          constants defined in {@link IResourceListener}.
   * @param listener
   */
  public void addResourceListener(String element, Integer event, IResourceListener listener) {
    P_ResourceListenerEntry entry = new P_ResourceListenerEntry();
    entry.setElement(element);
    entry.setEvent(event);
    entry.setListener(listener);

    synchronized (m_registrationLock) {
      if (m_resourceListeners.size() == 0) {
        attachJdtResourceChangeListener();
      }

      m_resourceListeners.add(entry);
    }
  }

  public void removeResourceListener(IResourceListener listener) {
    synchronized (m_registrationLock) {
      for (P_ResourceListenerEntry entry : m_resourceListeners.toArray(new P_ResourceListenerEntry[m_resourceListeners.size()])) {
        if (entry.getListener() == listener) {
          m_resourceListeners.remove(entry);
        }
      }

      if (m_resourceListeners.size() == 0) {
        detachJdtResourceChangeListener();
      }
    }
  }

  public IFile getFile() {
    synchronized (m_fileLock) {
      return m_file;
    }
  }

  public boolean isSameFile(IFile file) {
    if (file == null || m_file == null) {
      return false;
    }
    return file.getProjectRelativePath().equals(m_file.getProjectRelativePath());
  }

  public void setFile(IFile file) {
    synchronized (m_fileLock) {
      m_modificationStamp = IResource.NULL_STAMP;
      m_file = file;
    }
  }

  public boolean existsFile() {
    return m_file != null && m_file.exists();
  }

  protected long getModificationStamp() {
    return m_modificationStamp;
  }

  protected void setModificationStamp(long modificationStamp) {
    m_modificationStamp = modificationStamp;
  }

  public void touch() {
    m_modificationStamp = IResource.NULL_STAMP;
  }

  /**
   * To notify listeners about a change.
   * 
   * @param element
   *          the element that changed (defined in {@link IResourceListener})
   * @param event
   *          the events that describes the change. This is <em>bitwise OR</em>'ing together events described in
   *          {@link IResourceListener}
   */
  protected void notifyResourceListeners(String element, Integer event) {
    if (element == null) {
      throw new IllegalArgumentException("element must not be null");
    }
    if (event == null) {
      throw new IllegalArgumentException("event must not be null");
    }

    Set<IResourceListener> listenersProcessed = new HashSet<IResourceListener>();

    for (P_ResourceListenerEntry entry : m_resourceListeners.toArray(new P_ResourceListenerEntry[m_resourceListeners.size()])) {
      if (!listenersProcessed.contains(entry.getListener()) &&
          (entry.getElement() == null || entry.getElement().equals(element)) &&
          (entry.getEvent() == null || (entry.getEvent() & event) > 0)) {
        try {
          entry.getListener().changed(element, event);
        }
        catch (Throwable e) {
          // failsafe
          JaxWsSdk.logError("Error occured while notifying listener about change", e);
        }
        finally {
          listenersProcessed.add(entry.getListener());
        }
      }
    }
  }

  private class P_JdtResourceChangedListener implements IResourceChangeListener {

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      if (m_file == null) {
        return;
      }

      // there is only interest in POST_CHANGE events
      if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
        return;
      }

      try {
        IResourceDelta rootDelta = event.getDelta();
        rootDelta.accept(new IResourceDeltaVisitor() {

          @Override
          public boolean visit(IResourceDelta delta) throws CoreException {
            if (delta.getKind() == IResourceDelta.REMOVED || delta.getKind() == IResourceDelta.REPLACED || delta.getKind() == IResourceDelta.ADDED || (delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.CONTENT) != 0)) { // it is crucial to exclude marker update events
              IResource candidate = delta.getResource();

              if (candidate.getType() != IResource.FILE) {
                return true;
              }

              if (candidate.getProject() != m_project) {
                return false;
              }

              if (CompareUtility.equals(candidate, m_file)) {
                // only notify if modification stamp of resource is different to current stamp.
                // This is to exclude workspace modifications caused by API code
                if (m_modificationStamp != API_MODIFICATION_STAMP && (m_modificationStamp == IResource.NULL_STAMP || m_modificationStamp != m_file.getModificationStamp())) {
                  notifyResourceListeners(IResourceListener.ELEMENT_FILE, IResourceListener.EVENT_UNKNOWN);
                }
                return false;
              }
            }
            return true;
          }
        });
      }
      catch (Exception e) {
        JaxWsSdk.logError("Unexpected error occured while intercepting 'Resource Change' event.", e);
      }
    }
  }

  private class P_ResourceListenerEntry {
    private IResourceListener m_listener;

    /**
     * the source the listener is interested in to receive notifications
     */
    private String m_element;
    /**
     * the event the listener is interested in to receive notifications
     */
    private Integer m_event;

    public IResourceListener getListener() {
      return m_listener;
    }

    public void setListener(IResourceListener listener) {
      m_listener = listener;
    }

    public String getElement() {
      return m_element;
    }

    public void setElement(String element) {
      m_element = element;
    }

    public Integer getEvent() {
      return m_event;
    }

    public void setEvent(Integer event) {
      m_event = event;
    }
  }
}
