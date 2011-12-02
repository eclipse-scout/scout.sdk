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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
  private final P_AsyncLog m_asyncLog;
  private final HashMap<IType, PrimaryTypeTypeHierarchy> m_cachedPrimaryTypeHierarchies;

  public static HierarchyCache getInstance() {
    return INSTANCE;
  }

  private HierarchyCache() {
    m_cachedPrimaryTypeHierarchies = new HashMap<IType, PrimaryTypeTypeHierarchy>();
    m_asyncLog = new P_AsyncLog();
    m_cacheLock = new Object();
  }

  @Override
  public void dispose() {
    synchronized (m_cacheLock) {
      m_cachedPrimaryTypeHierarchies.clear();
    }
  }

  @Override
  public IPrimaryTypeTypeHierarchy getPrimaryTypeHierarchy(IType type) throws IllegalArgumentException {
    if (!TypeUtility.exists(type)) {
      return null;
    }
    else if (TypeUtility.exists(type.getDeclaringType())) {
      throw new IllegalArgumentException("type '" + type.getElementName() + "' must be a primary type.");
    }
    PrimaryTypeTypeHierarchy hierarchy = null;
    synchronized (m_cacheLock) {
      hierarchy = m_cachedPrimaryTypeHierarchies.get(type);
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
      ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
      return new TypeHierarchy(null, hierarchy);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not build super hierarchy '" + type.getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  private void handleTypeChange(IType t, ITypeHierarchy superTypeHierarchy) {
    try {
      ArrayList<CachedTypeHierarchy> hierarchies = new ArrayList<CachedTypeHierarchy>();
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
              if (!h.containsInSubhierarchy(h.getType(), superTypes)) {
                // remove
                h.handleTypeRemoving(t);

              }
              else {
                IType[] hierarchySuperTypes = h.getSubtypes(t);
                if (!TypeUtility.equalTypes(hierarchySuperTypes, superTypes)) {
                  // changed
                  h.handleTypeChanged(t);
                }
              }
            }
            else {
              if (h.containsInSubhierarchy(h.getType(), superTypes)) {
                // add
                h.handleTypeAdding(t);

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
      ArrayList<CachedTypeHierarchy> hierarchies = new ArrayList<CachedTypeHierarchy>();
      synchronized (m_cacheLock) {
        hierarchies.addAll(m_cachedPrimaryTypeHierarchies.values());
      }
      ITypeFilter compilationUnitFilter = TypeFilters.getInnterTypeFilter(type);
      for (CachedTypeHierarchy h : hierarchies) {
        if (h.isCreated()) {
          IType[] allTypes = h.getJdtHierarchy().getAllTypes();
          for (IType candidate : allTypes) {
            if (compilationUnitFilter.accept(candidate)) {
              // remove
              h.handleTypeRemoving(candidate);
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

  private void handleCompilationUnitRemoved(ICompilationUnit icu) {
    try {
      ArrayList<CachedTypeHierarchy> hierarchies = new ArrayList<CachedTypeHierarchy>();
      synchronized (m_cacheLock) {
        hierarchies.addAll(m_cachedPrimaryTypeHierarchies.values());
      }
      ITypeFilter compilationUnitFilter = TypeFilters.getCompilationUnitFilter(icu);
      for (CachedTypeHierarchy h : hierarchies) {

        if (h.isCreated()) {
          IType[] allTypes = h.getJdtHierarchy().getAllTypes();
          for (IType candidate : allTypes) {
            if (compilationUnitFilter.accept(candidate)) {
              // remove
              h.handleTypeRemoving(candidate);
              break;
            }
          }
        }

      }
    }
    catch (Exception e) {
      SdkUtilActivator.logError("could not handle icu removed ('" + icu.getElementName() + "') change in hierarchies.");
    }
  }

  private void handleCompilationUnitAdded(ICompilationUnit icu) {
    try {
      // get types
      IType[] types = icu.getTypes();
      if (types != null && types.length > 0) {
        for (IType t : types) {
          recHandleTypeAdded(t);
        }
      }

    }
    catch (Exception e) {
      SdkUtilActivator.logError("could not handle icu added ('" + icu.getElementName() + "') change in hierarchies.");
    }
  }

  private void recHandleTypeAdded(IType type) {
    try {
      if (TypeUtility.exists(type)) {
        handleTypeChange(type, type.newSupertypeHierarchy(new NullProgressMonitor()));
        IType[] innerTypes = type.getTypes();
        if (innerTypes != null) {
          for (IType t : innerTypes) {
            recHandleTypeAdded(t);
          }
        }
      }
    }
    catch (Exception e) {
      SdkUtilActivator.logError("could not handle type added ('" + type.getElementName() + "') in hierarchies.");
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
    ArrayList<CachedTypeHierarchy> hierarchies = new ArrayList<CachedTypeHierarchy>();
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
//        m_asyncLog.append(e.getElement());
      case IJavaElementDelta.CHANGED: {
        if (e.getElementType() == IJavaElement.TYPE && e.getDeclaringType() == null) {
          handleTypeChange((IType) e.getElement(), e.getSuperTypeHierarchy());
        }
        break;
      }
      case IJavaElementDelta.REMOVED: {
        if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
          handleCompilationUnitRemoved((ICompilationUnit) e.getElement());
        }
        else if (e.getElementType() == IJavaElement.TYPE && e.getDeclaringType() == null) {
          handleTypeRemoved((IType) e.getElement());
//          handleTypeChange((IType) e.getElement(), e.getSuperTypeHierarchy());
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

  private class P_AsyncLog extends Job {
    private ArrayList<IJavaElement> m_elements = new ArrayList<IJavaElement>();

    public P_AsyncLog() {
      super("");
    }

    public void append(IJavaElement e) {
      m_elements.add(e);
      cancel();
      schedule(3000);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      synchronized (m_elements) {
        m_elements.toArray(new IJavaElement[m_elements.size()]);
        m_elements.clear();
      }
      return Status.OK_STATUS;
    }
  }

}
