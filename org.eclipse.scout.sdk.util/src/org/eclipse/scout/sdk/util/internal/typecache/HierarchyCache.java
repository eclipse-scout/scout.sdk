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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchyResult;
import org.eclipse.scout.sdk.util.typecache.IHierarchyCache;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.TypeHierarchyConstraints;

public final class HierarchyCache implements IHierarchyCache {

  private static final HierarchyCache INSTANCE = new HierarchyCache();

  private final Map<Object, ICacheableTypeHierarchyResult> m_cachedHierarchyResults;

  public static HierarchyCache getInstance() {
    return INSTANCE;
  }

  private HierarchyCache() {
    m_cachedHierarchyResults = new HashMap<>();
  }

  @Override
  public synchronized void dispose() {
    invalidateAll(); // to ensure the resources are released.
    m_cachedHierarchyResults.clear();
  }

  public synchronized List<ICacheableTypeHierarchyResult> getAllCachedHierarchies() {
    return getHierarchiesSafe();
  }

  @Override
  public ICachedTypeHierarchyResult getProjectContextTypeHierarchy(TypeHierarchyConstraints constraints) {
    ICacheableTypeHierarchyResult h = null;
    synchronized (this) {
      h = m_cachedHierarchyResults.get(constraints);
      if (h != null && !TypeUtility.exists(h.getBaseType())) {
        h.invalidate();
        m_cachedHierarchyResults.remove(constraints);
        h = null;
      }
      if (h == null) {
        h = new ProjectContextTypeHierarchyResult(constraints);
        m_cachedHierarchyResults.put(constraints, h);
      }
    }
    return h;
  }

  @Override
  public ICachedTypeHierarchy getTypeHierarchy(IType type) {
    if (!TypeUtility.exists(type) || !type.getJavaProject().exists()) {
      throw new IllegalArgumentException("type does not exist!");
    }
    ICacheableTypeHierarchyResult hierarchy = null;
    synchronized (this) {
      hierarchy = m_cachedHierarchyResults.get(type);
      if (hierarchy != null && (!TypeUtility.exists(hierarchy.getBaseType()) || !TypeUtility.exists(hierarchy.getBaseType().getJavaProject()))) {
        // discard old create new
        hierarchy.invalidate();
        m_cachedHierarchyResults.remove(type);
        hierarchy = null;
      }
      if (hierarchy == null) {
        hierarchy = new CachedTypeHierarchy(type);
        m_cachedHierarchyResults.put(type, hierarchy);
      }
    }
    return (ICachedTypeHierarchy) hierarchy;
  }

  @Override
  public ICachedTypeHierarchy getPrimaryTypeHierarchy(IType type) {
    if (type != null && TypeUtility.exists(type.getDeclaringType())) {
      throw new IllegalArgumentException("type '" + type.getFullyQualifiedName() + "' must be a primary type.");
    }
    return new CachedPrimaryTypeHierarchy(getTypeHierarchy(type));
  }

  synchronized void removeCachedHierarchy(IType type) {
    m_cachedHierarchyResults.remove(type);
  }

  synchronized void replaceCachedHierarchy(Object oldKey, Object newKey, ICacheableTypeHierarchyResult hierarchyToAdd) {
    m_cachedHierarchyResults.remove(oldKey);
    m_cachedHierarchyResults.put(newKey, hierarchyToAdd);
  }

  @Override
  public ITypeHierarchy getLocalTypeHierarchy(IRegion region) {
    try {
      return new TypeHierarchy(null, JavaCore.newTypeHierarchy(region, null, null));
    }
    catch (JavaModelException e) {
      if (!e.isDoesNotExist()) {
        SdkUtilActivator.logWarning("could not build hierarchy of region '" + region + "'.", e);
      }
    }
    return null;
  }

  @Override
  public ITypeHierarchy getSupertypeHierarchy(IType type) {
    try {
      return new TypeHierarchy(type, type.newSupertypeHierarchy(null));
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not build super hierarchy '" + type.getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  @Override
  public synchronized void invalidateAll() {
    List<ICacheableTypeHierarchyResult> hierarchies = getHierarchiesSafe(); // get a copy to prevent ConcurrentModificationException
    for (ICacheableTypeHierarchyResult h : hierarchies) {
      if (h.isCreated()) {
        h.invalidate();
      }
    }
  }

  private synchronized List<ICacheableTypeHierarchyResult> getHierarchiesSafe() {
    return CollectionUtility.arrayList(m_cachedHierarchyResults.values());
  }

  private void handleTypeChange(IType t, ITypeHierarchy superTypeHierarchy) {
    if (superTypeHierarchy != null) {
      List<ICacheableTypeHierarchyResult> hierarchies = getHierarchiesSafe();
      Set<IType> superTypes = superTypeHierarchy.getAllSupertypes(t);
      for (ICacheableTypeHierarchyResult h : hierarchies) {
        if (h.isCreated() && h.contains(t) != h.isTypeAccepted(t, superTypes)) {
          h.invalidate();
        }
      }
    }
  }

  private void handleTypeRemoved(IType type) {
    List<ICacheableTypeHierarchyResult> hierarchies = getHierarchiesSafe();
    for (ICacheableTypeHierarchyResult h : hierarchies) {
      if (h.isCreated() && h.contains(type)) {
        h.invalidate();
      }
    }
  }

  private void handleJavaElementRemoved(IJavaElement element) {
    if (element.getElementType() < IJavaElement.TYPE) {
      List<ICacheableTypeHierarchyResult> hierarchies = getHierarchiesSafe();
      for (ICacheableTypeHierarchyResult h : hierarchies) {
        if (h.isCreated()) {
          if (TypeUtility.isAncestor(element, h.getBaseType())) {
            h.invalidate();
          }
          else {
            for (IType candidate : h) {
              if (TypeUtility.isAncestor(element, candidate)) {
                h.invalidate();
                break;
              }
            }
          }
        }
      }
    }
    else if (element.getElementType() == IJavaElement.TYPE) {
      handleTypeRemoved((IType) element);
    }
  }

  private void handleCompilationUnitChanged(ICompilationUnit icu) {
    if (!TypeUtility.exists(icu)) {
      return;
    }

    IRegion region = JavaCore.newRegion();
    region.add(icu);
    try {
      ITypeHierarchy hierarchy = getLocalTypeHierarchy(region);
      if (hierarchy != null) {
        for (IType t : icu.getTypes()) {
          reqTypeChangedFromExternal(t, hierarchy);
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not find types in compilation unit '" + icu.getElementName() + "'.", e);
    }
  }

  private void reqTypeChangedFromExternal(IType type, ITypeHierarchy hierarchy) {
    handleTypeChange(type, hierarchy);
    try {
      for (IType subType : type.getTypes()) {
        reqTypeChangedFromExternal(subType, hierarchy);
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not find subtypes of type '" + type.getElementName() + "'.", e);
    }
  }

  /**
   * will be notified before events are passed through the event listener list from {@link JavaResourceChangedEmitter}
   */
  void elementChanged(JdtEvent e) {
    switch (e.getEventType()) {
      case IJavaElementDelta.ADDED:
      case IJavaElementDelta.CHANGED: {
        if (e.getElementType() == IJavaElement.TYPE && e.getDeclaringType() == null) {
          handleTypeChange((IType) e.getElement(), e.getSuperTypeHierarchy());
        }
        else if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
          handleCompilationUnitChanged((ICompilationUnit) e.getElement());
        }
        else if (e.getElementType() == IJavaElement.JAVA_PROJECT) {
          if ((e.getFlags() & IJavaElementDelta.F_OPENED) != 0 || e.getFlags() == 0) {
            // a new java project has been created/imported/opened/added in the workspace
            invalidateAll();
          }
          else if ((e.getFlags() & IJavaElementDelta.F_CLOSED) != 0) {
            // a project has been closed
            handleJavaElementRemoved(e.getElement());
          }
        }
        else if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
          if ((e.getFlags() & (IJavaElementDelta.F_ADDED_TO_CLASSPATH | IJavaElementDelta.F_REMOVED_FROM_CLASSPATH | IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED | IJavaElementDelta.F_REORDER)) != 0 || e.getFlags() == 0) {
            // the classpath has been changed
            invalidateAll();
          }
        }
        break;
      }
      case IJavaElementDelta.REMOVED: {
        if (TypeUtility.exists(e.getElement().getParent())) {
          handleJavaElementRemoved(e.getElement());
        }
        break;
      }
      case JavaResourceChangedEmitter.CHANGED_EXTERNAL:
        if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
          handleCompilationUnitChanged((ICompilationUnit) e.getElement());
        }
        else if (e.getElementType() == IJavaElement.TYPE) {
          handleTypeChange((IType) e.getElement(), e.getSuperTypeHierarchy());
        }
        break;
    }
  }
}
