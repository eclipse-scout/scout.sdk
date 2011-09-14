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
package org.eclipse.scout.sdk.internal.workspace.marker;

import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 *
 */
public class ScoutMarkerSupport {
  private static final String MARKER_ID = "org.eclipse.scout.sdk.marker";

  final IType iForm = ScoutSdk.getType(RuntimeClasses.IForm);
  private IElementChangedListener m_elementChangedListener;
  private Object m_typeListLock = new Object();
  private HashSet<IType> m_typesList = new HashSet<IType>();
  private MarkerUpdateJob m_markerUpdateJob = new MarkerUpdateJob();

  public ScoutMarkerSupport() {
    m_elementChangedListener = new P_ElementChangedListener();
    JavaCore.addElementChangedListener(m_elementChangedListener);
  }

  public void dispose() {
    JavaCore.removeElementChangedListener(m_elementChangedListener);
  }

  private void handleTypeRemoved(IType type) {

  }

  private void handleTypeChanged(IType type) {
    if (TypeUtility.exists(type) && type.getDeclaringType() == null) {
      synchronized (m_typeListLock) {
        if (m_typesList.add(type)) {
          m_markerUpdateJob.cancel();
          m_markerUpdateJob.schedule(859);
        }
      }
    }
  }

  private class P_ElementChangedListener implements IElementChangedListener {
    @Override
    public final void elementChanged(ElementChangedEvent event) {
      visitDelta(event.getDelta(), event.getType());
    }

    private void visitDelta(IJavaElementDelta delta, int eventType) {
      int flags = delta.getFlags();
      int kind = delta.getKind();
      IJavaElement e = delta.getElement();
      if (e != null && e.getElementType() == IJavaElement.TYPE) {
        IType type = (IType) e;
        switch (kind) {
          case IJavaElementDelta.ADDED:
          case IJavaElementDelta.CHANGED:
            handleTypeChanged(type);
            break;
//          case IJavaElementDelta.REMOVED:
//            System.out.println("type removed: '" + type.getFullyQualifiedName() + "'");
//            break;
          default:

        }
      }
      else if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
        IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
        if (childDeltas != null && childDeltas.length > 0) {
          for (int i = 0; i < childDeltas.length; i++) {
            visitDelta(childDeltas[i], eventType);
          }
        }
      }
    }
  } // end class P_ElementChangedListener

  private class MarkerUpdateJob extends Job {

    private MarkerUpdateJob() {
      super("");
      setPriority(Job.DECORATE);
      setUser(false);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      System.out.println("*** runn marker update JOB");
      IType[] types = null;
      synchronized (m_typeListLock) {
        types = m_typesList.toArray(new IType[m_typesList.size()]);
        m_typesList.clear();
      }
      for (IType t : types) {
        if (TypeUtility.exists(t)) {
          try {
            ITypeHierarchy hierarchy = t.newSupertypeHierarchy(monitor);
            if (hierarchy.contains(iForm)) {
              ISourceRange nameRange = t.getNameRange();
              System.out.println("update form '" + t.getFullyQualifiedName() + "'------------");
              IMarker marker = t.getResource().createMarker(MARKER_ID);
              marker.setAttribute(IMarker.MESSAGE, "Form data of '" + t.getElementName() + "' not up to date.");
              marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
              marker.setAttribute(IMarker.CHAR_START, nameRange.getOffset());
              marker.setAttribute(IMarker.CHAR_END, nameRange.getOffset() + nameRange.getLength());
              marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
            }

          }
          catch (Exception e) {
            ScoutSdk.logWarning("could not update form data marker for '" + t.getFullyQualifiedName() + "'.");
          }
        }
      }

      return Status.OK_STATUS;
    }
  } // end class MarkerUpdateJob

}
