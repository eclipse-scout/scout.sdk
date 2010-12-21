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
package org.eclipse.scout.sdk.internal.workspace.typecache;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.internal.jdt.finegraned.FineGrainedAstMatcher;
import org.eclipse.scout.sdk.internal.jdt.finegraned.FineGrainedJavaElementDelta;
import org.eclipse.scout.sdk.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.jdt.JdtEvent;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 *
 */
public class JavaResourceChangedEmitter {

  private P_ResourceListener m_resourceChangeListener;
  private P_JavaElementChangedListener m_javaElementListener;
  private Object m_resourceLock = new Object();
  private HashMap<IResource, JdtEventCollector> m_jdtEvents;
  private EventListenerList m_eventListeners = new EventListenerList();
  private WeakHashMap<IType, ArrayList<WeakReference<IJavaResourceChangedListener>>> m_innerTypeChangedListeners;
  private WeakHashMap<IType, ArrayList<WeakReference<IJavaResourceChangedListener>>> m_methodChangedListeners;
  private Object m_eventListenerLock;
  private final HierarchyCache m_hierarchyCache;
  private IBufferChangedListener m_sourceBufferListener;
  private HashMap<String /*icu path*/, CompilationUnit /*ast*/> m_ast;

  public JavaResourceChangedEmitter(HierarchyCache hierarchyCache) {
    m_hierarchyCache = hierarchyCache;
    m_sourceBufferListener = new P_SourceBufferListener();
    m_jdtEvents = new HashMap<IResource, JdtEventCollector>();
    m_eventListenerLock = new Object();
    m_innerTypeChangedListeners = new WeakHashMap<IType, ArrayList<WeakReference<IJavaResourceChangedListener>>>();
    m_methodChangedListeners = new WeakHashMap<IType, ArrayList<WeakReference<IJavaResourceChangedListener>>>();
    m_resourceChangeListener = new P_ResourceListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceChangeListener);
    m_javaElementListener = new P_JavaElementChangedListener();
    JavaCore.addElementChangedListener(m_javaElementListener);
    // ast tracker
    m_ast = new HashMap<String, CompilationUnit>();
    for (ICompilationUnit icu : JavaCore.getWorkingCopies(null)) {
      try {
        icu.getBuffer().addBufferChangedListener(m_sourceBufferListener);
      }
      catch (JavaModelException e) {
        ScoutSdk.logError("could not access source buffer of '" + icu.getElementName() + "'.", e);
      }
      CompilationUnit ast = createAst(icu);
      m_ast.put(icu.getPath().toString(), ast);
    }
  }

  public void dispose() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceChangeListener);
    JavaCore.removeElementChangedListener(m_javaElementListener);
  }

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

  public void addJavaResourceChangedListener(IJavaResourceChangedListener listener) {
    m_eventListeners.add(IJavaResourceChangedListener.class, listener);
  }

  public void removeJavaResouceChangedListener(IJavaResourceChangedListener listener) {
    m_eventListeners.remove(IJavaResourceChangedListener.class, listener);
  }

  private void add(JdtEvent event) {
    IResource r = event.getElement().getResource();
    if (r != null) {
      synchronized (m_resourceLock) {
        JdtEventCollector jdtEventCollector = m_jdtEvents.get(r);
        if (jdtEventCollector == null) {
          jdtEventCollector = new JdtEventCollector(r);
          m_jdtEvents.put(r, jdtEventCollector);
          ICompilationUnit icu = null;
          if (event.getElement().getElementType() == IJavaElement.COMPILATION_UNIT) {
            icu = (ICompilationUnit) event.getElement();
          }
          else {
            icu = (ICompilationUnit) event.getElement().getAncestor(IJavaElement.COMPILATION_UNIT);
          }
          fireEvent(new JdtEvent(this, JdtEvent.BUFFER_DIRTY, icu));
        }

        jdtEventCollector.addEvent(event);
      }
    }
  }

  private CompilationUnit createAst(ICompilationUnit icu) {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setCompilerOptions(JavaCore.getOptions());
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(icu);
    return (CompilationUnit) parser.createAST(null);
  }

  private void fireEvent(JdtEvent e) {
    // first notify hierarchies which could be used of other listeners
    if (m_hierarchyCache != null) {
      m_hierarchyCache.elementChanged(e);
    }
    for (IJavaResourceChangedListener l : m_eventListeners.getListeners(IJavaResourceChangedListener.class)) {
      l.handleEvent(e);
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

  private void releaseCompilationUnit(ICompilationUnit icu) {
    boolean fireChanges = false;
    JdtEvent[] jdtEvents = new JdtEvent[0];
    synchronized (m_resourceLock) {
      JdtEventCollector eventSet = m_jdtEvents.remove(icu.getResource());
      if (eventSet != null && eventSet.hasEvents()) {
        System.out.println("ts = \n'" + icu.getResource().getModificationStamp() + "'\n'" + eventSet.getLastModification() + "'\n-------------------------");
        fireChanges = (icu == null) || icu.getResource().getModificationStamp() != eventSet.getLastModification();
        jdtEvents = eventSet.getEvents();
      }
    }
    if (fireChanges) {
      for (JdtEvent e : jdtEvents) {
        fireEvent(e);
      }
    }
    fireEvent(new JdtEvent(JavaResourceChangedEmitter.this, JdtEvent.BUFFER_SYNC, icu));
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings({"unused", "deprecation"})
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
            public boolean visit(IResourceDelta visitDelta) {
              IResource resource = visitDelta.getResource();
              if (resource.getType() == IFile.FILE) {
                if (resource.getFileExtension().equalsIgnoreCase("java") && ((visitDelta.getFlags() & IResourceDelta.CONTENT) != 0)) {
                  releaseCompilationUnit((ICompilationUnit) JavaCore.create(resource));
                }
                return false;
              }
              return true;
            }

          });
        }
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
      }
    }
  } // end class P_ResouceListener

  private class P_SourceBufferListener implements IBufferChangedListener {

    @Override
    public void bufferChanged(BufferChangedEvent event) {
      ICompilationUnit icu = (ICompilationUnit) event.getBuffer().getOwner();
      if (!event.getBuffer().hasUnsavedChanges() && TypeUtility.exists(icu)) {
        m_ast.put(icu.getPath().toString(), createAst(icu));
        releaseCompilationUnit(icu);
      }

    }
  }

  private class P_JavaElementChangedListener implements org.eclipse.jdt.core.IElementChangedListener {
    public static final int CHANGED_FLAG_MASK =
        IJavaElementDelta.F_CONTENT |
            IJavaElementDelta.F_MODIFIERS |
            IJavaElementDelta.F_MOVED_FROM |
            IJavaElementDelta.F_MOVED_TO |
            IJavaElementDelta.F_REORDER |
            IJavaElementDelta.F_SUPER_TYPES |
            IJavaElementDelta.F_OPENED |
            IJavaElementDelta.F_CLOSED |
            IJavaElementDelta.F_CATEGORIES |
            IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED |
            IJavaElementDelta.F_ANNOTATIONS;

    @Override
    public void elementChanged(ElementChangedEvent event) {
      visitDelta(event.getDelta(), event.getType());
    }

    private void visitDelta(IJavaElementDelta delta, int eventType) {
      int flags = delta.getFlags();

      int kind = delta.getKind();
      // children
      if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
        IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
        if (childDeltas != null && childDeltas.length > 0) {
          for (int i = 0; i < childDeltas.length; i++) {
            visitDelta(childDeltas[i], eventType);
          }
        }
      }
      else {
        IJavaElement e = delta.getElement();
        if (e != null) {
          switch (kind) {
            case IJavaElementDelta.ADDED:
              add(new JdtEvent(JavaResourceChangedEmitter.this, kind, e));
              break;
            case IJavaElementDelta.REMOVED:
              if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
                add(new JdtEvent(JavaResourceChangedEmitter.this, kind, e));
                JdtEvent[] jdtEvents = new JdtEvent[0];
                synchronized (m_resourceLock) {
                  JdtEventCollector eventSet = m_jdtEvents.remove(e.getResource());
                  if (eventSet != null && eventSet.hasEvents()) {
                    jdtEvents = eventSet.getEvents();
                  }
                }
                for (JdtEvent e1 : jdtEvents) {
                  fireEvent(e1);
                }
                fireEvent(new JdtEvent(JavaResourceChangedEmitter.this, JdtEvent.BUFFER_SYNC, e));

              }
              else {
                add(new JdtEvent(JavaResourceChangedEmitter.this, kind, e));
              }
              break;
            case IJavaElementDelta.CHANGED:

              if ((flags & CHANGED_FLAG_MASK) != 0) {
                // workaround: try to find out what really changed
                if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
                  // new ast

                  CompilationUnit newAst = createAst((ICompilationUnit) e);
                  CompilationUnit oldAst = m_ast.get(e.getPath().toString());
                  if (oldAst != null) {
                    for (FineGrainedJavaElementDelta a : calculateDeltas(oldAst, newAst)) {
                      if (TypeUtility.exists(a.getElement())) {
                        add(new JdtEvent(JavaResourceChangedEmitter.this, kind, a.getElement()));
                      }
                    }
                  }
                  try {
                    m_ast.put(e.getPath().toString(), newAst);
                  }
                  catch (Exception ex) {
                    ex.printStackTrace();
                  }
                }
                else {
                  add(new JdtEvent(JavaResourceChangedEmitter.this, kind, e));
                }
              }
              else if ((flags & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0) {
                if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
                  ICompilationUnit icu = (ICompilationUnit) e;
                  if (icu.isWorkingCopy()) {
                    CompilationUnit ast = createAst(icu);
                    m_ast.put(icu.getPath().toString(), ast);
                    try {
                      icu.getBuffer().addBufferChangedListener(m_sourceBufferListener);
                    }
                    catch (JavaModelException ex) {
                      ScoutSdk.logError("could not access source buffer of '" + icu.getElementName() + "'.", ex);
                    }
                  }
                  else {
                    try {
                      icu.getBuffer().removeBufferChangedListener(m_sourceBufferListener);
                    }
                    catch (JavaModelException ex) {
                      ScoutSdk.logError("could not access source buffer of '" + icu.getElementName() + "'.", ex);
                    }
                    m_ast.remove(icu.getPath().toString());
                  }

                }
              }
              break;
          }
        }
      }
    }

    public FineGrainedJavaElementDelta[] calculateDeltas(CompilationUnit oldAst, final CompilationUnit newAst) {
      final HashSet<FineGrainedJavaElementDelta> set = new HashSet<FineGrainedJavaElementDelta>();
      FineGrainedAstMatcher matcher = new FineGrainedAstMatcher() {
        @Override
        protected boolean processDelta(boolean match, ASTNode node, Object other) {
          if (!match) {
            try {

              IJavaElement e = ((ICompilationUnit) newAst.getJavaElement()).getElementAt(node.getStartPosition());
              if (e != null) {
                set.add(new FineGrainedJavaElementDelta(e));
              }
            }
            catch (JavaModelException e1) {
              // nop
            }
          }
          return true;
        }
      };
      newAst.subtreeMatch(matcher, oldAst);
      return set.toArray(new FineGrainedJavaElementDelta[set.size()]);
    }
  }

}
