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
    boolean wasCreated = m_created;
    m_created = false;

    if (wasCreated) {
      // the hierarchy has changed from created (valid) to invalid.
      fireHierarchyChanged();
    }
  }

  private void fireHierarchyChanged() {
    for (ITypeHierarchyChangedListener l : m_hierarchyListeners.getListeners(ITypeHierarchyChangedListener.class)) {
      l.hierarchyInvalidated();
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
      if (!TypeUtility.exists(getType()) || !getType().getJavaProject().exists()) {
        throw new IllegalArgumentException("type or project does not exist");
      }
      try {
        setJdtHierarchy(getType().newTypeHierarchy(monitor));
        m_created = true;
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logError("Unable to create type hierarchy for type " + getType().getFullyQualifiedName(), e);
      }
    }
  }
}
