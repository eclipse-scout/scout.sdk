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
package org.eclipse.scout.sdk.jdt.compile;

import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.IScoutSeverityListener;

public final class ScoutSeverityManager {

  private final static ScoutSeverityManager instance = new ScoutSeverityManager();

  public static ScoutSeverityManager getInstance() {
    return instance;
  }

  private EventListenerList m_listenerList;
  private Object m_listenerListLock;

  private ScoutSeverityManager() {
    m_listenerListLock = new Object();
    m_listenerList = new EventListenerList();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
      @Override
      public void resourceChanged(IResourceChangeEvent e) {
        IMarkerDelta[] mdeltas = e.findMarkerDeltas(IMarker.PROBLEM, true);
        if (mdeltas != null && mdeltas.length > 0) {
          HashSet<IResource> changedResorces = new HashSet<IResource>();
          for (IMarkerDelta d : mdeltas) {
            IResource r = d.getMarker().getResource();
            if (r != null) {
              changedResorces.add(r);
            }
          }
          fireSeverityChanged(changedResorces);
        }
      }
    });
  }

  public void addQualityManagerListener(IScoutSeverityListener listener) {
    synchronized (m_listenerListLock) {
      m_listenerList.add(IScoutSeverityListener.class, listener);
    }
  }

  public void removeQualityManagerListener(IScoutSeverityListener listener) {
    synchronized (m_listenerListLock) {
      m_listenerList.remove(IScoutSeverityListener.class, listener);
    }
  }

  public void fireSeverityChanged(Set<IResource> set) {
    EventListener[] a;
    synchronized (m_listenerListLock) {
      a = m_listenerList.getListeners(IScoutSeverityListener.class);
    }
    if (a != null && a.length > 0) {
      for (int i = 0; i < a.length; i++) {
        for (IResource r : set) {
          ((IScoutSeverityListener) a[i]).severityChanged(r);
        }
      }
    }
  }

  /**
   * @param obj
   *          a IJavaElement or a IResource the largest scope is IWorkspaceRoot.
   * @return the max severity one of {@link IMarker#SEVERITY_INFO}, {@link IMarker#SEVERITY_WARNING} or
   *         {@link IMarker#SEVERITY_ERROR}
   */
  public int getSeverityOf(Object obj) {
    // delegate no log output
    return getCompileResult(obj).getSeverity();
  }

  /**
   * @param obj
   *          a IJavaElement or a IResource the largest scope is IWorkspaceRoot.
   * @return a compile result {@link ICompileResult}
   */
  public ICompileResult getCompileResult(Object obj) {
    CompileResult result = new CompileResult();
    collectCompileResult(obj, result);
    return result;
  }

  protected void collectCompileResult(Object obj, CompileResult result) {
    try {
      if (obj instanceof IJavaElement) {
        IJavaElement element = (IJavaElement) obj;
        int type = element.getElementType();
        switch (type) {
          case IJavaElement.JAVA_MODEL:
          case IJavaElement.JAVA_PROJECT:
          case IJavaElement.PACKAGE_FRAGMENT_ROOT:
            collectSeverityMarkers(element.getResource(), IResource.DEPTH_INFINITE, null, result);
            break;
          case IJavaElement.PACKAGE_FRAGMENT:
          case IJavaElement.COMPILATION_UNIT:
          case IJavaElement.CLASS_FILE:
            collectSeverityMarkers(element.getResource(), IResource.DEPTH_ONE, null, result);
            break;
          case IJavaElement.PACKAGE_DECLARATION:
          case IJavaElement.IMPORT_DECLARATION:
          case IJavaElement.IMPORT_CONTAINER:
          case IJavaElement.TYPE:
          case IJavaElement.INITIALIZER:
          case IJavaElement.METHOD:
          case IJavaElement.FIELD:
          case IJavaElement.LOCAL_VARIABLE:
            ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
            if (cu != null) {
              ISourceReference ref = (type == IJavaElement.COMPILATION_UNIT) ? null : (ISourceReference) element;
              collectSeverityMarkers(cu.getResource(), IResource.DEPTH_ONE, ref, result);
            }
            break;
          default:
        }
      }
      else if (obj instanceof IResource) {
        collectSeverityMarkers((IResource) obj, IResource.DEPTH_INFINITE, null, result);
      }
    }
    catch (CoreException e) {
      ScoutSdk.logError(e);
    }
  }

  private void collectSeverityMarkers(IResource res, int depth, ISourceReference sourceElement, CompileResult result) throws CoreException {
    if (res == null || !res.isAccessible()) {
      return;
    }
    IMarker[] markers = res.findMarkers(IMarker.PROBLEM, true, depth);
    if (markers != null) {
      for (IMarker m : markers) {
        if (isMarkerInRange(m, sourceElement)) {
          result.addMarker(m);
        }
      }
    }
  }

  private boolean isMarkerInRange(IMarker marker, ISourceReference sourceElement) throws CoreException {
    if (sourceElement == null) {
      return true;
    }
    if (marker.isSubtypeOf(IMarker.TEXT)) {
      int pos = marker.getAttribute(IMarker.CHAR_START, -1);
      ISourceRange range = sourceElement.getSourceRange();
      if (range != null) {
        int rangeOffset = range.getOffset();
        return (rangeOffset <= pos && rangeOffset + range.getLength() > pos);
      }
      return false;
    }
    return false;
  }

}
