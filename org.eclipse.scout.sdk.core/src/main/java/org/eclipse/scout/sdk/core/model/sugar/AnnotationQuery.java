/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.sugar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link AnnotationQuery}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class AnnotationQuery<T> {
  private final IType m_containerType;
  private final IAnnotatable m_owner;
  private boolean m_includeSuperClasses = false;
  private boolean m_includeSuperInterfaces = false;
  private String m_name;
  private Class<AbstractManagedAnnotation> m_managedWrapperType;
  private IFilter<IAnnotation> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public AnnotationQuery(IType containerType, IAnnotatable owner) {
    m_containerType = containerType;
    m_owner = owner;
  }

  /**
   * Include / Exclude super classes and super types for visiting
   *
   * @param b
   *          default false
   * @return this
   */
  public AnnotationQuery<T> withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param b
   *          default false
   * @return this
   */
  public AnnotationQuery<T> withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * @param b
   *          default false
   * @return this
   */
  public AnnotationQuery<T> withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param name
   *          fully qualified name
   * @return this
   */
  public AnnotationQuery<T> withName(String fullyQualifiedName) {
    m_name = fullyQualifiedName;
    return this;
  }

  /**
   * @param managed
   *          type
   * @return this
   */
  @SuppressWarnings("unchecked")
  public <A extends AbstractManagedAnnotation> AnnotationQuery<A> withManagedWrapper(Class<A> managedWrapperType) {
    m_managedWrapperType = (Class<AbstractManagedAnnotation>) managedWrapperType;
    return (AnnotationQuery<A>) this;
  }

  /**
   * @param filter
   * @return this
   */
  public AnnotationQuery<T> withFilter(IFilter<IAnnotation> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * @param maxResultCount
   * @return this
   */
  public AnnotationQuery<T> withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IAnnotation a) {
    if (m_name != null && !m_name.equals(a.getName())) {
      return false;
    }
    if (m_managedWrapperType != null && !ManagedAnnotationUtil.typeName(m_managedWrapperType).equals(a.getName())) {
      return false;
    }
    if (m_filter != null && !m_filter.evaluate(a)) {
      return false;
    }
    return true;
  }

  /**
   * @param type
   *          is the container type of the optional owner
   * @param owner
   *          may be null, then it is automatically calculated based on similarity to the main m_owner (for example
   *          super type methods must have the same name as the main m_owner method)
   * @param result
   * @param maxCount
   */
  protected void visitRec(IType type, IAnnotatable owner, List<IAnnotation> result, int maxCount) {
    if (type == null) {
      return;
    }
    if (owner == null) {
      if (m_owner instanceof IType) {
        owner = type;
      }
      else if (m_owner instanceof IMethod) {
        owner = type.methods().withName(m_owner.getElementName()).first();
      }
      else if (m_owner instanceof IField) {
        owner = type.fields().withName(m_owner.getElementName()).first();
      }
    }
    if (owner != null) {
      for (IAnnotation a : owner.getAnnotations()) {
        if (accept(a)) {
          result.add(a);
          if (result.size() >= maxCount) {
            return;
          }
        }
      }
    }
    if (m_includeSuperClasses) {
      visitRec(type.getSuperClass(), null, result, maxCount);
      if (result.size() >= maxCount) {
        return;
      }
    }

    if (m_includeSuperInterfaces) {
      for (IType superInterface : type.getSuperInterfaces()) {
        visitRec(superInterface, null, result, maxCount);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  public boolean exists() {
    return first() != null;
  }

  @SuppressWarnings("unchecked")
  public T first() {
    ArrayList<IAnnotation> result = new ArrayList<>(1);
    visitRec(m_containerType, m_owner, result, 1);
    if (result.isEmpty()) {
      return null;
    }
    if (m_managedWrapperType != null) {
      return (T) ManagedAnnotationUtil.wrap(result.get(0), m_managedWrapperType);
    }
    return (T) result.get(0);
  }

  @SuppressWarnings("unchecked")
  public List<T> list() {
    ArrayList<IAnnotation> result = new ArrayList<>();
    visitRec(m_containerType, m_owner, result, m_maxResultCount);
    if (result.isEmpty()) {
      return Collections.emptyList();
    }
    if (m_managedWrapperType != null) {
      ArrayList<AbstractManagedAnnotation> managedList = new ArrayList<>(result.size());
      for (IAnnotation a : result) {
        managedList.add(ManagedAnnotationUtil.wrap(a, m_managedWrapperType));
      }
      return (List<T>) managedList;
    }
    return (List<T>) result;
  }

}
