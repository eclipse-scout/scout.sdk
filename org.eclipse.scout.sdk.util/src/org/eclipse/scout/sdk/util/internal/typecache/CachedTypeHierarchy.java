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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;

/**
 *
 */
public class CachedTypeHierarchy extends TypeHierarchy implements ICachedTypeHierarchy {

  private boolean m_created = false;
  private EventListenerList m_hierarchyListeners = new EventListenerList();

  CachedTypeHierarchy(IType type) {
    super(type);
  }

  @Override
  public void invalidate() {
    m_created = false;
  }

  void handleTypeAdding(IType type) {
    fireHierarchyChanged(ITypeHierarchyChangedListener.PRE_TYPE_ADDING, type);
    invalidate();
    fireHierarchyChanged(ITypeHierarchyChangedListener.POST_TYPE_ADDING, type);
  }

  void handleTypeChanged(IType type) {
    fireHierarchyChanged(ITypeHierarchyChangedListener.PRE_TYPE_CHANGED, type);
    invalidate();
    fireHierarchyChanged(ITypeHierarchyChangedListener.POST_TYPE_CHANGED, type);
  }

  void handleTypeRemoving(IType type) {
    fireHierarchyChanged(ITypeHierarchyChangedListener.PRE_TYPE_REMOVING, type);
    invalidate();
    fireHierarchyChanged(ITypeHierarchyChangedListener.POST_TYPE_REMOVING, type);
  }

  private void fireHierarchyChanged(int type, IType affectedType) {
    for (ITypeHierarchyChangedListener l : m_hierarchyListeners.getListeners(ITypeHierarchyChangedListener.class)) {
      l.handleEvent(type, affectedType);
    }
  }

  @Override
  public boolean isCreated() {
    return m_created;
  }

  @Override
  public void addHierarchyListener(ITypeHierarchyChangedListener listener) {
    m_hierarchyListeners.add(ITypeHierarchyChangedListener.class, listener);
  }

  @Override
  public void removeHierarchyListener(ITypeHierarchyChangedListener listener) {
    m_hierarchyListeners.remove(ITypeHierarchyChangedListener.class, listener);
  }

  @Override
  void revalidate(IProgressMonitor monitor) {
    if (!m_created) {
      // TODO aho verify type and project exitstance
      if (!TypeUtility.exists(getType()) || !getType().getJavaProject().exists()) {
        throw new IllegalArgumentException("type or project does not exist");
      }
      try {
        if (getJdtHierarchy() == null) {
          setJdtHierarchy(getType().newTypeHierarchy(monitor));
        }
        else {
          if (TypeUtility.exists(getType()) && getType().getJavaProject().exists()) {
            getJdtHierarchy().refresh(monitor);
          }
          else {
            return;
          }
        }
        m_created = true;
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logError("Unable to refresh cached type hierarchy for type " + getType().getFullyQualifiedName(), e);
      }
    }
  }
}
