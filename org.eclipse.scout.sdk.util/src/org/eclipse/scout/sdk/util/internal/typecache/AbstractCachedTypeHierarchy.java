/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.internal.typecache;

import java.util.Comparator;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;

/**
 * <h3>{@link AbstractCachedTypeHierarchy}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 16.05.2014
 */
public abstract class AbstractCachedTypeHierarchy extends TypeHierarchy implements ICacheableTypeHierarchyResult, ICachedTypeHierarchy {

  private final EventListenerList m_hierarchyListeners;
  private volatile boolean m_created;

  /**
   * @param type
   */
  protected AbstractCachedTypeHierarchy(IType type) {
    super(type);
    m_hierarchyListeners = new EventListenerList();
    m_created = false;
  }

  @Override
  protected void setBaseType(IType newBaseType) {
    super.setBaseType(newBaseType);
    invalidate();
  }

  @Override
  public boolean contains(IType type) {
    revalidateImpl();
    return super.contains(type);
  }

  @Override
  public Set<IType> getAllClasses(ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getAllClasses(filter, comparator);
  }

  @Override
  public Set<IType> getAllInterfaces(ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getAllInterfaces(filter, comparator);
  }

  @Override
  public boolean isSubtype(IType type, IType potentialSubtype) {
    if (CompareUtility.equals(type, potentialSubtype)) {
      return true;
    }
    revalidateImpl();
    return super.isSubtype(type, potentialSubtype);
  }

  @Override
  public Set<IType> getAllSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getAllSubtypes(type, filter, comparator);
  }

  @Override
  public Set<IType> getAllSuperclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getAllSuperclasses(type, filter, comparator);
  }

  @Override
  public Set<IType> getAllSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getAllSuperInterfaces(type, filter, comparator);
  }

  @Override
  public Set<IType> getAllTypes(ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getAllTypes(filter, comparator);
  }

  @Override
  public Set<IType> getSubclasses(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getSubclasses(type, filter, comparator);
  }

  @Override
  public Set<IType> getSubtypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getSubtypes(type, filter, comparator);
  }

  @Override
  public IType getSuperclass(IType type) {
    revalidateImpl();
    return super.getSuperclass(type);
  }

  @Override
  public Set<IType> getSuperInterfaces(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getSuperInterfaces(type, filter, comparator);
  }

  @Override
  public Set<IType> getSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getSupertypes(type, filter, comparator);
  }

  @Override
  public Set<IType> getAllSupertypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    revalidateImpl();
    return super.getAllSupertypes(type, filter, comparator);
  }

  private void revalidateImpl() {
    if (!isCreated()) {
      synchronized (this) {
        if (!isCreated()) {
          HierarchyCache hierarchyCache = HierarchyCache.getInstance();
          if (getBaseType() == null) {
            hierarchyCache.removeCachedHierarchy(null);
            throw new IllegalArgumentException("Type 'null' does not exist.");
          }

          // Always re-resolve the base IType to ensure we still have the most accurate
          IType newBaseType = TypeUtility.getType(getBaseType().getFullyQualifiedName());
          if (TypeUtility.exists(newBaseType) && TypeUtility.exists(newBaseType.getJavaProject())) {
            if (!newBaseType.equals(getBaseType())) {
              hierarchyCache.replaceCachedHierarchy(getBaseType(), newBaseType, this);
              setBaseType(newBaseType);
            }
          }
          else {
            // base type does no longer exist -> remove
            hierarchyCache.removeCachedHierarchy(getBaseType());
            throw new IllegalArgumentException("Type '" + getBaseType().getFullyQualifiedName() + "' does not exist.");
          }

          revalidate();

          m_created = getJdtHierarchy() != null;
          if (!m_created) {
            // re-validate failed. cancel
            throw new IllegalArgumentException("Hierarchy '" + getBaseType().getFullyQualifiedName() + "' could not be re-validated. See prior errors for details.");
          }
        }
      }
    }
  }

  @Override
  public void invalidate() {
    boolean wasCreated = m_created;
    synchronized (this) {
      m_created = false;
      setJdtHierarchy(null); // free up resources
    }

    if (wasCreated) {
      // the hierarchy has changed from created (valid) to invalid.
      fireHierarchyChanged();
    }
  }

  /**
   * revalidates (re-creates) the hierarchy.
   * Use {@link #setJdtHierarchy(org.eclipse.jdt.core.ITypeHierarchy)} to set the new hierarchy.
   */
  protected abstract void revalidate();

  private void fireHierarchyChanged() {
    for (ITypeHierarchyChangedListener l : m_hierarchyListeners.getListeners(ITypeHierarchyChangedListener.class)) {
      try {
        l.hierarchyInvalidated();
      }
      catch (Exception e) {
        SdkUtilActivator.logError("Error invoking hierarchy changed listener '" + l.getClass().getName() + "'.", e);
      }
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
}
