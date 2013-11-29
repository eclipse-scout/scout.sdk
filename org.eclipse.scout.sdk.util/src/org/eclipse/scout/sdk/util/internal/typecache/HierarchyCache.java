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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IHierarchyCache;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;

/**
 *
 */
public final class HierarchyCache implements IHierarchyCache {

  private static final HierarchyCache INSTANCE = new HierarchyCache();

  private final Object m_cacheLock;
  private final HashMap<IType, PrimaryTypeTypeHierarchy> m_cachedPrimaryTypeHierarchies;

  public static HierarchyCache getInstance() {
    return INSTANCE;
  }

  private HierarchyCache() {
    m_cachedPrimaryTypeHierarchies = new HashMap<IType, PrimaryTypeTypeHierarchy>();
    m_cacheLock = new Object();
  }

  @Override
  public void dispose() {
    synchronized (m_cacheLock) {
      m_cachedPrimaryTypeHierarchies.clear();
    }
  }

  @Override
  public IPrimaryTypeTypeHierarchy[] getAllCachedHierarchies() {
    synchronized (m_cacheLock) {
      return m_cachedPrimaryTypeHierarchies.values().toArray(new IPrimaryTypeTypeHierarchy[m_cachedPrimaryTypeHierarchies.size()]);
    }
  }

  @Override
  public IPrimaryTypeTypeHierarchy getPrimaryTypeHierarchy(IType type) throws IllegalArgumentException {
    if (!TypeUtility.exists(type) || !type.getJavaProject().exists()) {
      throw new IllegalArgumentException("type does not exist!");
    }
    else if (TypeUtility.exists(type.getDeclaringType())) {
      throw new IllegalArgumentException("type '" + type.getElementName() + "' must be a primary type.");
    }
    PrimaryTypeTypeHierarchy hierarchy = null;
    synchronized (m_cacheLock) {
      hierarchy = m_cachedPrimaryTypeHierarchies.get(type);
      if (hierarchy != null && (!TypeUtility.exists(hierarchy.getType()) || !TypeUtility.exists(hierarchy.getType().getJavaProject()))) {
        // discard old create new
        m_cachedPrimaryTypeHierarchies.remove(type);
        hierarchy = null;
      }
      if (hierarchy == null) {
        hierarchy = new PrimaryTypeTypeHierarchy(type);
        m_cachedPrimaryTypeHierarchies.put(type, hierarchy);
      }
    }
    return hierarchy;
  }

  /**
   * @param region
   * @return
   */
  @Override
  public TypeHierarchy getLocalHierarchy(IRegion region) {
    try {
      ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
      return new TypeHierarchy(null, hierarchy);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not build hierarchy of region '" + region + "'.", e);
    }
    return null;
  }

  @Override
  public TypeHierarchy getSuperHierarchy(IType type) {
    try {
      ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
      return new TypeHierarchy(null, hierarchy);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not build super hierarchy '" + type.getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  @Override
  public void invalidateAll() {
    synchronized (m_cacheLock) {
      for (IPrimaryTypeTypeHierarchy h : m_cachedPrimaryTypeHierarchies.values()) {
        if (h.isCreated()) {
          h.invalidate();
        }
      }
    }
  }

  private void handleTypeChange(IType t, ITypeHierarchy superTypeHierarchy) {
    try {
      ArrayList<CachedTypeHierarchy> hierarchies = new ArrayList<CachedTypeHierarchy>(m_cachedPrimaryTypeHierarchies.size());
      if (!TypeUtility.exists(t.getDeclaringType())) {
        synchronized (m_cacheLock) {
          hierarchies.addAll(m_cachedPrimaryTypeHierarchies.values());
        }
      }

      if (hierarchies.size() > 0) {
        for (CachedTypeHierarchy h : hierarchies) {
          if (h.isCreated()) {
            IType[] superTypes = superTypeHierarchy.getSupertypes(t);

            if (h.contains(t)) {
              if (!h.containsInSubHierarchy(h.getType(), superTypes)) {
                // remove
                h.invalidate();
              }
              else {
                IType[] hierarchySuperTypes = h.getSubtypes(t);
                if (!TypeUtility.equalTypes(hierarchySuperTypes, superTypes)) {
                  // changed
                  h.invalidate();
                }
              }
            }
            else {
              if (h.containsInSubHierarchy(h.getType(), superTypes)) {
                // add
                h.invalidate();
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      SdkUtilActivator.logError("could not handle type('" + t.getFullyQualifiedName() + "') change in hierarchies.", e);
    }
  }

  /**
   * @param type
   */
  private void handleTypeRemoved(IType type) {
    try {
      ArrayList<CachedTypeHierarchy> hierarchies = new ArrayList<CachedTypeHierarchy>(m_cachedPrimaryTypeHierarchies.size());
      synchronized (m_cacheLock) {
        hierarchies.addAll(m_cachedPrimaryTypeHierarchies.values());
      }
      ITypeFilter compilationUnitFilter = TypeFilters.getInnerTypeFilter(type);
      for (CachedTypeHierarchy h : hierarchies) {
        if (h.isCreated()) {
          IType[] allTypes = h.getJdtHierarchy().getAllTypes();
          for (IType candidate : allTypes) {
            if (compilationUnitFilter.accept(candidate)) {
              // remove
              h.invalidate();
              break;
            }
          }
        }
      }
    }
    catch (Exception e) {
      SdkUtilActivator.logError("could not handle type removed ('" + type.getElementName() + "') change in hierarchies.");
    }
  }

  private void handleJavaElementRemoved(IJavaElement element) {
    if (element.getElementType() < IJavaElement.TYPE) {
      try {
        ArrayList<CachedTypeHierarchy> hierarchies = new ArrayList<CachedTypeHierarchy>(m_cachedPrimaryTypeHierarchies.size());
        synchronized (m_cacheLock) {
          hierarchies.addAll(m_cachedPrimaryTypeHierarchies.values());
        }
        for (CachedTypeHierarchy h : hierarchies) {
          if (TypeUtility.isAncestor(element, h.getType())) {
            synchronized (m_cacheLock) {
              m_cachedPrimaryTypeHierarchies.remove(h.getType());
            }
          }
          else if (h.isCreated()) {
            IType[] allTypes = h.getJdtHierarchy().getAllTypes();
            for (IType candidate : allTypes) {
              if (TypeUtility.isAncestor(element, candidate)) {
                h.invalidate();
              }
            }
          }
        }
      }
      catch (Exception e) {
        SdkUtilActivator.logError("could not handle element removed event ('" + element.getElementName() + "') in hierarchies.");
      }
    }
    else if (element.getElementType() == IJavaElement.TYPE) {
      handleTypeRemoved((IType) element);
    }
  }

  private void handleCompilationUnitChagnedExternal(ICompilationUnit icu) {
    IRegion region = JavaCore.newRegion();
    region.add(icu);
    try {
      ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
      for (IType t : icu.getTypes()) {
        reqTypeChangedFromExternal(t, hierarchy);
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not find types in compilation unti '" + icu.getElementName() + "'.", e);
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

  public void clearCache() {
    ArrayList<CachedTypeHierarchy> hierarchies = new ArrayList<CachedTypeHierarchy>(m_cachedPrimaryTypeHierarchies.size());
    synchronized (m_cacheLock) {
      hierarchies.addAll(m_cachedPrimaryTypeHierarchies.values());
      m_cachedPrimaryTypeHierarchies.clear();
    }
    for (CachedTypeHierarchy hierarchy : hierarchies) {
      hierarchy.invalidate();
    }
  }

  /**
   * will be notified before events are passed through the event listener list from {@link JavaResourceChangedEmitter}
   * 
   * @param e
   */
  public void elementChanged(JdtEvent e) {
    switch (e.getEventType()) {
      case IJavaElementDelta.ADDED:
      case IJavaElementDelta.CHANGED: {
        if (e.getElementType() == IJavaElement.TYPE && e.getDeclaringType() == null) {
          handleTypeChange((IType) e.getElement(), e.getSuperTypeHierarchy());
        }
        else if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
          try {
            IType[] types = ((ICompilationUnit) e.getElement()).getTypes();
            if (types.length > 0) {
              handleTypeChange(types[0], e.getSuperTypeHierarchy());
            }
          }
          catch (JavaModelException ex) {
            SdkUtilActivator.logError(ex);
          }
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
          handleCompilationUnitChagnedExternal((ICompilationUnit) e.getElement());
        }
        break;
    }
  }
}
