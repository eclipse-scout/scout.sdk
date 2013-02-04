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
package org.eclipse.scout.sdk.util.internal.typecache;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.jdt.finegraned.FineGrainedJavaElementDelta;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IJavaResourceChangedEmitter;

/**
 *
 */
public final class JavaResourceChangedEmitter implements IJavaResourceChangedEmitter {
  public static final int CHANGED_EXTERNAL = 229;

  public static final int CHANGED_FLAG_MASK =
      IJavaElementDelta.F_CONTENT |
          IJavaElementDelta.F_MODIFIERS |
          IJavaElementDelta.F_MOVED_FROM |
          IJavaElementDelta.F_MOVED_TO |
          IJavaElementDelta.F_REORDER |
          IJavaElementDelta.F_SUPER_TYPES |
          IJavaElementDelta.F_OPENED |
          IJavaElementDelta.F_CLOSED |
          IJavaElementDelta.F_PRIMARY_WORKING_COPY |
          IJavaElementDelta.F_CATEGORIES |
          IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED |
          IJavaElementDelta.F_ANNOTATIONS;

  private static final JavaResourceChangedEmitter INSTANCE = new JavaResourceChangedEmitter(HierarchyCache.getInstance());

  private P_JavaElementChangedListener m_javaElementListener;
  private Object m_resourceLock = new Object();
  private HashMap<ICompilationUnit, JdtEventCollector> m_eventCollectors;
  private EventListenerList m_eventListeners = new EventListenerList();
  private WeakHashMap<IType, ArrayList<WeakReference<IJavaResourceChangedListener>>> m_innerTypeChangedListeners;
  private WeakHashMap<IType, ArrayList<WeakReference<IJavaResourceChangedListener>>> m_methodChangedListeners;
  private Object m_eventListenerLock;
  private final HierarchyCache m_hierarchyCache;
  private IBufferChangedListener m_sourceBufferListener;
  private IResourceChangeListener m_resourceListener;

  public static ICompilationUnit[] getPendingWorkingCopies() {
    synchronized (INSTANCE.m_resourceLock) {
      return INSTANCE.m_eventCollectors.keySet().toArray(new ICompilationUnit[INSTANCE.m_eventCollectors.size()]);
    }
  }

  private JavaResourceChangedEmitter(HierarchyCache hierarchyCache) {
    m_hierarchyCache = hierarchyCache;
    m_eventCollectors = new HashMap<ICompilationUnit, JdtEventCollector>();
    m_eventListenerLock = new Object();
    m_innerTypeChangedListeners = new WeakHashMap<IType, ArrayList<WeakReference<IJavaResourceChangedListener>>>();
    m_methodChangedListeners = new WeakHashMap<IType, ArrayList<WeakReference<IJavaResourceChangedListener>>>();
    m_sourceBufferListener = new P_SourceBufferListener();
    m_javaElementListener = new P_JavaElementChangedListener();
    JavaCore.addElementChangedListener(m_javaElementListener);
    m_resourceListener = new P_ResourceListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceListener);
    // ast tracker
    for (ICompilationUnit icu : JavaCore.getWorkingCopies(null)) {
      try {
        aquireEventCollector(icu);
      }
      catch (JavaModelException ex) {
        SdkUtilActivator.logWarning("could not aquire event collector for '" + icu.getElementName() + "'.", ex);
      }
    }
  }

  public static JavaResourceChangedEmitter getInstance() {
    return INSTANCE;
  }

  @Override
  public void dispose() {
    JavaCore.removeElementChangedListener(m_javaElementListener);
    m_eventCollectors.clear();
    m_innerTypeChangedListeners.clear();
    m_methodChangedListeners.clear();
  }

  @Override
  public void addInnerTypeChangedListener(IType type, IJavaResourceChangedListener listener) {
    synchronized (m_eventListenerLock) {
      ArrayList<WeakReference<IJavaResourceChangedListener>> listenerList = m_innerTypeChangedListeners.get(type);
      if (listenerList == null) {
        listenerList = new ArrayList<WeakReference<IJavaResourceChangedListener>>();
        m_innerTypeChangedListeners.put(type, listenerList);
      }
      listenerList.add(new WeakReference<IJavaResourceChangedListener>(listener));
    }
  }

  @Override
  public void removeInnerTypeChangedListener(IType type, IJavaResourceChangedListener listener) {
    synchronized (m_eventListenerLock) {
      ArrayList<WeakReference<IJavaResourceChangedListener>> listenerList = m_innerTypeChangedListeners.get(type);
      if (listenerList != null) {
        for (Iterator<WeakReference<IJavaResourceChangedListener>> it = listenerList.iterator(); it.hasNext();) {
          WeakReference<IJavaResourceChangedListener> ref = it.next();
          if (ref.get() == null || ref.get().equals(listener)) {
            it.remove();
          }
        }
        if (listenerList.isEmpty()) {
          m_innerTypeChangedListeners.remove(type);
        }
      }
    }
  }

  @Override
  public void addMethodChangedListener(IType type, IJavaResourceChangedListener listener) {
    synchronized (m_eventListenerLock) {
      ArrayList<WeakReference<IJavaResourceChangedListener>> listenerList = m_methodChangedListeners.get(type);
      if (listenerList == null) {
        listenerList = new ArrayList<WeakReference<IJavaResourceChangedListener>>();
        m_methodChangedListeners.put(type, listenerList);
      }
      listenerList.add(new WeakReference<IJavaResourceChangedListener>(listener));
    }
  }

  @Override
  public void removeMethodChangedListener(IType type, IJavaResourceChangedListener listener) {
    synchronized (m_eventListenerLock) {
      ArrayList<WeakReference<IJavaResourceChangedListener>> listenerList = m_methodChangedListeners.get(type);
      if (listenerList != null) {
        for (Iterator<WeakReference<IJavaResourceChangedListener>> it = listenerList.iterator(); it.hasNext();) {
          WeakReference<IJavaResourceChangedListener> ref = it.next();
          if (ref.get() == null || ref.get().equals(listener)) {
            it.remove();
          }
        }
        if (listenerList.isEmpty()) {
          m_methodChangedListeners.remove(type);
        }
      }
    }
  }

  @Override
  public void addJavaResourceChangedListener(IJavaResourceChangedListener listener) {
    m_eventListeners.add(IJavaResourceChangedListener.class, listener);
  }

  @Override
  public void removeJavaResourceChangedListener(IJavaResourceChangedListener listener) {
    m_eventListeners.remove(IJavaResourceChangedListener.class, listener);
  }

  private void handleJdtDelta(ElementChangedEvent rootEvent, IJavaElementDelta delta) {
    IJavaElement e = delta.getElement();
    if (e == null) {
      return;
    }
    JdtEventCollector collector = null;
    ICompilationUnit compilationUnit = (ICompilationUnit) e.getAncestor(IJavaElement.COMPILATION_UNIT);
    if (compilationUnit != null && TypeUtility.exists(compilationUnit)) {
      collector = m_eventCollectors.get(compilationUnit);
      try {
        if (collector == null && compilationUnit.hasUnsavedChanges()) {
          collector = aquireEventCollector(compilationUnit);
        }
      }
      catch (JavaModelException ex) {
        SdkUtilActivator.logWarning("could not aquire event collector for '" + compilationUnit.getElementName() + "'.", ex);
      }
    }
    int kind = delta.getKind();
    int flags = delta.getFlags();
    switch (kind) {
      case IJavaElementDelta.ADDED:
        if (e.getElementType() < IJavaElement.COMPILATION_UNIT) {
          // fire straight
          fireEvent(new JdtEvent(JavaResourceChangedEmitter.this, kind, e));
        }
        else {
          addEvent(collector, new JdtEvent(JavaResourceChangedEmitter.this, kind, e));
        }
        break;
      case IJavaElementDelta.REMOVED:
        if (e.getElementType() <= IJavaElement.COMPILATION_UNIT) {
          // remove all open event collectors
          removeEventCollectors(e);
          // fire straight
          fireEvent(new JdtEvent(JavaResourceChangedEmitter.this, kind, e));
        }
        else {
          addEvent(collector, new JdtEvent(JavaResourceChangedEmitter.this, kind, e));
        }
        break;
      case IJavaElementDelta.CHANGED:
        if (e.getElementType() < IJavaElement.COMPILATION_UNIT) {
          fireEvent(new JdtEvent(JavaResourceChangedEmitter.this, kind, delta.getElement()));
        }
        else if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
          if (collector != null && (flags & CHANGED_FLAG_MASK) != 0) {
            FineGrainedJavaElementDelta[] astDiff = collector.updateAst();
            if (astDiff != null && astDiff.length > 0) {
              for (FineGrainedJavaElementDelta a : astDiff) {
                if (TypeUtility.exists(a.getElement())) {
                  addEvent(collector, new JdtEvent(JavaResourceChangedEmitter.this, kind, a.getElement()));
                }
              }
            }
            else if (collector.isEmpty()) {
              addEvent(collector, new JdtEvent(JavaResourceChangedEmitter.this, (collector != null) ? kind : CHANGED_EXTERNAL, e));
            }
          }
        }
        else {
          addEvent(collector, new JdtEvent(JavaResourceChangedEmitter.this, (collector != null) ? kind : CHANGED_EXTERNAL, e));
        }
    }

    if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
      ICompilationUnit icu = (ICompilationUnit) e;
      try {
        if (!icu.hasUnsavedChanges()) {
          if (rootEvent.getType() == ElementChangedEvent.POST_RECONCILE) {
            // the reconcile event is before the post change event. (fast save)
            releaseEventCollector((ICompilationUnit) e, true);
          }
          else if (rootEvent.getType() == ElementChangedEvent.POST_CHANGE && ((flags & IJavaElementDelta.F_CHILDREN) == 0) && ((flags & IJavaElementDelta.F_CONTENT) == 0)) {
            // normal save
            releaseEventCollector(icu, true);
          }
        }
      }
      catch (JavaModelException ex) {
        SdkUtilActivator.logWarning("could not release event collector for '" + icu.getElementName() + "'.", ex);
      }
    }
  }

  private void addEvent(JdtEventCollector collector, JdtEvent event) {
    if (collector != null) {
      if (collector.isEmpty()) {
        fireEvent(new JdtEvent(JavaResourceChangedEmitter.this, JdtEvent.BUFFER_DIRTY, collector.getCompilationUnit()));
      }
      collector.addEvent(event);
    }
    else {
      fireEvent(event);
    }
  }

  private JdtEventCollector aquireEventCollector(ICompilationUnit icu) throws JavaModelException {
    JdtEventCollector collector = null;
    collector = m_eventCollectors.get(icu);
    if (collector == null && icu.isWorkingCopy()) {
      collector = new JdtEventCollector(icu);
      m_eventCollectors.put(icu, collector);
      icu.getBuffer().addBufferChangedListener(m_sourceBufferListener);
    }
    return collector;
  }

  private void releaseEventCollector(ICompilationUnit icu, boolean clearWorkingCopy) {
    JdtEventCollector collector = null;
    if (icu.isWorkingCopy()) {
      collector = m_eventCollectors.get(icu);
    }
    else {
      collector = m_eventCollectors.remove(icu);
    }
    if (collector != null && !collector.isEmpty()) {
      boolean fireChanges = false;
      JdtEvent[] jdtEvents = new JdtEvent[0];
      synchronized (m_resourceLock) {
        if (collector != null && collector.hasEvents()) {
          long resourceModification = icu.getResource().getModificationStamp();
          fireChanges = (icu == null) || resourceModification != collector.getLastModification();
          jdtEvents = collector.removeAllEvents(resourceModification);
        }
      }
      if (fireChanges) {
        for (JdtEvent e : jdtEvents) {
          fireEvent(e);
        }
      }
      fireEvent(new JdtEvent(JavaResourceChangedEmitter.this, JdtEvent.BUFFER_SYNC, icu));
    }
  }

  private void removeEventCollectors(IJavaElement element) {
    synchronized (m_resourceLock) {
      for (Iterator<Entry<ICompilationUnit, JdtEventCollector>> it = m_eventCollectors.entrySet().iterator(); it.hasNext();) {
        Entry<ICompilationUnit, JdtEventCollector> cur = it.next();
        if (TypeUtility.isAncestor(element, cur.getKey())) {
          it.remove();
        }
      }
    }
  }

  private void fireEvent(JdtEvent e) {
    // first notify hierarchies which could be used by other listeners
    if (m_hierarchyCache != null) {
      m_hierarchyCache.elementChanged(e);
    }
    for (IJavaResourceChangedListener l : m_eventListeners.getListeners(IJavaResourceChangedListener.class)) {
      try {
        l.handleEvent(e);
      }
      catch (Exception ex) {
        SdkUtilActivator.logWarning("error during listener notification.", ex);
      }
    }
    // type
    if (e.getElementType() == IJavaElement.TYPE) {
      ArrayList<IJavaResourceChangedListener> listeners = new ArrayList<IJavaResourceChangedListener>();
      synchronized (m_eventListenerLock) {
        ArrayList<WeakReference<IJavaResourceChangedListener>> listenerList = m_innerTypeChangedListeners.get(e.getDeclaringType());
        if (listenerList != null) {
          for (Iterator<WeakReference<IJavaResourceChangedListener>> it = listenerList.iterator(); it.hasNext();) {
            WeakReference<IJavaResourceChangedListener> ref = it.next();
            IJavaResourceChangedListener listener = ref.get();
            if (listener == null) {
              it.remove();
            }
            else {
              listeners.add(listener);
            }
          }
          if (listenerList.isEmpty()) {
            m_innerTypeChangedListeners.remove(e.getDeclaringType());
          }
        }
      }
      for (IJavaResourceChangedListener l : listeners) {
        l.handleEvent(e);
      }
    }
    // method
    if (e.getElementType() == IJavaElement.METHOD) {
      ArrayList<IJavaResourceChangedListener> listeners = new ArrayList<IJavaResourceChangedListener>();
      synchronized (m_eventListenerLock) {
        ArrayList<WeakReference<IJavaResourceChangedListener>> listenerList = m_methodChangedListeners.get(e.getDeclaringType());
        if (listenerList != null) {
          for (Iterator<WeakReference<IJavaResourceChangedListener>> it = listenerList.iterator(); it.hasNext();) {
            WeakReference<IJavaResourceChangedListener> ref = it.next();
            IJavaResourceChangedListener listener = ref.get();
            if (listener == null) {
              it.remove();
            }
            else {
              listeners.add(listener);

            }
          }
          if (listenerList.isEmpty()) {
            m_methodChangedListeners.remove(e.getDeclaringType());
          }
        }
      }
      for (IJavaResourceChangedListener l : listeners) {
        l.handleEvent(e);
      }
    }
  }

  private void printEventType(int type, PrintStream out) {
    switch (type) {
      case IJavaElementDelta.ADDED:
        out.print("ADDED");
      case IJavaElementDelta.REMOVED:
        out.print("REMOVED");
      case IJavaElementDelta.CHANGED:
        out.print("CHANGED");
      case JdtEvent.BUFFER_DIRTY:
        out.print("BUFFER_DIRTY");
      case JdtEvent.BUFFER_SYNC:
        out.print("BUFFER_SYNC");
      default:
        out.print("UNDEFINED");
    }
    out.flush();
  }

  @SuppressWarnings("deprecation")
  private void printJdtFlags(int flags, PrintStream out) {
    boolean first = true;
    out.print("flags for '" + flags + "'[");
    if ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_ADDED_TO_CLASSPATH");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_CONTENT) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_CONTENT");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_MODIFIERS) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_MODIFIERS");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_CHILDREN");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_MOVED_FROM) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_MOVED_FROM");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_MOVED_TO) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_MOVED_TO");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_ADDED_TO_CLASSPATH");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_REMOVED_FROM_CLASSPATH");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_CLASSPATH_REORDER) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_CLASSPATH_REORDER");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_REORDER) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_REORDER");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_OPENED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_OPENED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_CLOSED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_CLOSED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_SUPER_TYPES) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_SUPER_TYPES");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_SOURCEATTACHED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_SOURCEATTACHED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_SOURCEDETACHED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_SOURCEDETACHED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_FINE_GRAINED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_FINE_GRAINED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_ARCHIVE_CONTENT_CHANGED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_PRIMARY_WORKING_COPY");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_CLASSPATH_CHANGED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_PRIMARY_RESOURCE) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_PRIMARY_RESOURCE");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_AST_AFFECTED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_AST_AFFECTED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_CATEGORIES) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_CATEGORIES");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_RESOLVED_CLASSPATH_CHANGED");
      first = false;
    }
    if ((flags & IJavaElementDelta.F_ANNOTATIONS) != 0) {
      out.print(((!first) ? (", ") : ("")) + "F_ANNOTATIONS");
      first = false;
    }
    out.print("]");
    out.flush();
  }

  private class P_ResourceListener implements IResourceChangeListener {
    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
      IResourceDelta delta = event.getDelta();
      try {
        if (delta != null) {
          delta.accept(new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta visitDelta) {
              IResource resource = visitDelta.getResource();
              if (resource.getType() == IFile.FILE && StringUtility.equalsIgnoreCase("java", resource.getFileExtension())) {
                return false;
              }
              return true;
            }
          });
        }
      }
      catch (CoreException e) {
        SdkUtilActivator.logWarning(e);
      }
    }
  } // end class P_ResourceListener

  private class P_SourceBufferListener implements IBufferChangedListener {
    @Override
    public void bufferChanged(BufferChangedEvent event) {
      IBuffer buffer = event.getBuffer();
      ICompilationUnit icu = (ICompilationUnit) buffer.getOwner();
      if (!buffer.hasUnsavedChanges() && buffer.isClosed()) {
        // release
        releaseEventCollector(icu, icu.isWorkingCopy());
        if (!icu.isWorkingCopy()) {
          buffer.removeBufferChangedListener(m_sourceBufferListener);
        }
      }
    }
  } // end class P_SourceBufferListener

  private class P_JavaElementChangedListener implements IElementChangedListener {

    @Override
    public void elementChanged(ElementChangedEvent event) {
      try {
        visitDelta(event, event.getDelta(), event.getType());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    private void visitDelta(ElementChangedEvent rootEvent, IJavaElementDelta delta, int eventType) {
      // annotations
      for (IJavaElementDelta annotationDelta : delta.getAnnotationDeltas()) {
        visitDelta(rootEvent, annotationDelta, eventType);
      }
      if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
        IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
        if (childDeltas != null && childDeltas.length > 0) {
          for (int i = 0; i < childDeltas.length; i++) {
            visitDelta(rootEvent, childDeltas[i], eventType);
          }
        }
      }
      else {
        handleJdtDelta(rootEvent, delta);
      }
    }
  } // end class P_JavaElementChangedListener
}
