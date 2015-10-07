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

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link AnnotationQuery}</h3> Annotation query that by default returns all annotations directly defined on the
 * owner.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class AnnotationQuery<T> {
  private final IType m_containerType;
  private final AnnotatableSpi m_owner;
  private final String m_methodId;

  private boolean m_includeSuperClasses = false;
  private boolean m_includeSuperInterfaces = false;
  private String m_name;
  private Class<AbstractManagedAnnotation> m_managedWrapperType;
  private IFilter<IAnnotation> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public AnnotationQuery(IType containerType, AnnotatableSpi owner) {
    m_containerType = containerType;
    m_owner = owner;
    if (owner instanceof MethodSpi) {
      m_methodId = SignatureUtils.createMethodIdentifier(((MethodSpi) owner).wrap());
    }
    else {
      m_methodId = null;
    }
  }

  /**
   * Include or exclude super types visiting when searching for annotations.
   *
   * @param b
   *          <code>true</code> if all super classes and super interfaces should be checked for annotations. Default is
   *          <code>false</code>.
   * @return this
   */
  public AnnotationQuery<T> withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Include or exclude super class visiting when searching for annotations.
   *
   * @param b
   *          <code>true</code> if all super classes should be checked for annotations. Default is <code>false</code>.
   * @return this
   */
  public AnnotationQuery<T> withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * Include or exclude super interface visiting when searching for annotations.
   *
   * @param b
   *          <code>true</code> if all super interfaces should be checked for annotations. Default is <code>false</code>
   *          .
   * @return this
   */
  public AnnotationQuery<T> withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Limit the annotations to the given fully qualified annotation type name (see {@link IAnnotation#type()}).
   *
   * @param name
   *          The fully qualified name. Default is no filtering.
   * @return this
   */
  public AnnotationQuery<T> withName(String fullyQualifiedName) {
    m_name = fullyQualifiedName;
    return this;
  }

  /**
   * Limit the annotations to the given managed annotation type and convert the result into the narrowed managed type.
   *
   * @param managedWrapperType
   *          The managed annotation type class. Default no filtering.
   * @return this
   */
  @SuppressWarnings("unchecked")
  public <A extends AbstractManagedAnnotation> AnnotationQuery<A> withManagedWrapper(Class<A> managedWrapperType) {
    m_managedWrapperType = (Class<AbstractManagedAnnotation>) managedWrapperType;
    return (AnnotationQuery<A>) this;
  }

  /**
   * Limit the annotations to the ones that accept the given {@link IFilter}.
   *
   * @param filter
   *          The filter. Default none.
   * @return this
   */
  public AnnotationQuery<T> withFilter(IFilter<IAnnotation> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * Limit the number of annotations to search.
   *
   * @param maxResultCount
   *          The maximum number of annotations to search. Default is unlimited.
   * @return this
   */
  public AnnotationQuery<T> withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IAnnotation a) {
    if (m_name != null && !m_name.equals(a.name())) {
      return false;
    }
    if (m_managedWrapperType != null && !AbstractManagedAnnotation.typeName(m_managedWrapperType).equals(a.name())) {
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
  protected void visitRec(IType type, AnnotatableSpi owner, List<IAnnotation> result, int maxCount, boolean onlyTraverse) {
    if (type == null) {
      return;
    }
    if (!onlyTraverse) {
      if (owner == null) {
        if (m_owner instanceof TypeSpi) {
          owner = type.unwrap();
        }
        else if (m_owner instanceof MethodSpi) {
          // find method with same signature
          IMethod m = type.methods().withFilter(new IFilter<IMethod>() {
            @Override
            public boolean evaluate(IMethod element) {
              return m_methodId.equals(SignatureUtils.createMethodIdentifier(element));
            }
          }).first();

          if (m != null) {
            owner = m.unwrap();
          }
        }
        else if (m_owner instanceof FieldSpi) {
          IField f = type.fields().withName(m_owner.getElementName()).first();
          if (f != null) {
            owner = f.unwrap();
          }
        }
      }
      if (owner != null) {
        for (AnnotationSpi spi : owner.getAnnotations()) {
          IAnnotation a = spi.wrap();
          if (accept(a)) {
            result.add(a);
            if (result.size() >= maxCount) {
              return;
            }
          }
        }
      }
    }
    if (m_includeSuperClasses || m_includeSuperInterfaces) {
      visitRec(type.superClass(), null, result, maxCount, !m_includeSuperClasses);
      if (result.size() >= maxCount) {
        return;
      }
    }

    if (m_includeSuperInterfaces) {
      for (IType superInterface : type.superInterfaces()) {
        visitRec(superInterface, null, result, maxCount, false);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  /**
   * Checks if there is at least one annotation that fulfills this query.
   *
   * @return <code>true</code> if at least one annotation fulfills this query, <code>false</code> otherwise.
   */
  public boolean existsAny() {
    return first() != null;
  }

  /**
   * Gets the first annotation that fulfills this query.
   *
   * @return The first annotation that fulfills this query or <code>null</code> if there is none.
   */
  @SuppressWarnings("unchecked")
  public T first() {
    List<IAnnotation> result = new ArrayList<>(1);
    visitRec(m_containerType, m_owner, result, 1, false);
    if (result.isEmpty()) {
      return null;
    }
    if (m_managedWrapperType != null) {
      return (T) result.get(0).wrap(m_managedWrapperType);
    }
    return (T) result.get(0);
  }

  /**
   * Gets all annotations that fulfill this query.
   *
   * @return A {@link List} with all annotations that fulfill this query. Never returns <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  public List<T> list() {
    List<IAnnotation> result = new ArrayList<>(m_owner.getAnnotations().size());
    visitRec(m_containerType, m_owner, result, m_maxResultCount, false);
    if (result.isEmpty()) {
      return Collections.emptyList();
    }
    if (m_managedWrapperType != null) {
      List<AbstractManagedAnnotation> managedList = new ArrayList<>(result.size());
      for (IAnnotation a : result) {
        managedList.add(a.wrap(m_managedWrapperType));
      }
      return (List<T>) managedList;
    }
    return (List<T>) result;
  }

}
