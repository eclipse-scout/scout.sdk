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
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.WrappedList;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

/**
 * <h3>{@link MethodQuery}</h3> Method query that by default returns all {@link IMethod}s in an {@link IType}.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class MethodQuery {
  private final IType m_type;
  private boolean m_includeSuperClasses = false;
  private boolean m_includeSuperInterfaces = false;
  private String m_name;
  private String m_annotationFqn;
  private String m_methodId;
  private int m_flags = -1;
  private Pattern m_methodNamePattern;
  private Predicate<IMethod> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public MethodQuery(IType type) {
    m_type = type;
  }

  /**
   * Include or exclude super types visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          <code>true</code> if all super classes and super interfaces should be checked for {@link IMethod}s.
   *          Default is <code>false</code>.
   * @return this
   */
  public MethodQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Limit the {@link IMethod}s to the ones having an {@link IAnnotation} with the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the {@link IAnnotation} that must exist on the {@link IMethod}.
   * @return this
   */
  public MethodQuery withAnnotation(String fqn) {
    m_annotationFqn = fqn;
    return this;
  }

  /**
   * Limit the {@link IMethod}s to the ones having at least all of the given flags.
   *
   * @param flags
   *          The flags that must exist on the {@link IMethod}
   * @return this
   * @see Flags
   */
  public MethodQuery withFlags(int flags) {
    m_flags = flags;
    return this;
  }

  /**
   * Include or exclude super class visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          <code>true</code> if all super classes should be checked for {@link IMethod}s. Default is
   *          <code>false</code>.
   * @return this
   */
  public MethodQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * Include or exclude super interfaces visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          <code>true</code> if all super interfaces should be checked for {@link IMethod}s. Default is
   *          <code>false</code>.
   * @return this
   */
  public MethodQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Limit the {@link IMethod}s to the given name (see {@link IMethod#elementName()}).
   *
   * @param name
   *          The {@link IMethod} name. Default is no filtering.
   * @return this
   */
  public MethodQuery withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * Limit the {@link IMethod}s to the given method identifier.
   *
   * @param id
   *          The id of the {@link IMethod}. Use {@link SignatureUtils#createMethodIdentifier(IMethod)} to create a
   *          method identifier.
   * @return this
   */
  public MethodQuery withMethodIdentifier(String id) {
    m_methodId = id;
    return this;
  }

  /**
   * Limit to the {@link IMethod}s whose name ({@link IMethod#elementName()}) matches the given regular expression
   * pattern.
   *
   * @param namePattern
   *          The regular expression the method name must match
   * @return this
   * @see Pattern
   */
  public MethodQuery withName(Pattern namePattern) {
    m_methodNamePattern = namePattern;
    return this;
  }

  /**
   * Limit the {@link IMethod}s to the ones that accept the given {@link Predicate}.
   *
   * @param filter
   *          The filter. Default none.
   * @return this
   */
  public MethodQuery withFilter(Predicate<IMethod> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * Limit the number of {@link IMethod}s to search.
   *
   * @param maxResultCount
   *          The maximum number of methods to search. Default is unlimited.
   * @return this
   */
  public MethodQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected boolean accept(IMethod f) {
    if (m_name != null && !m_name.equals(f.elementName())) {
      return false;
    }
    if (m_methodId != null && !m_methodId.equals(SignatureUtils.createMethodIdentifier(f))) {
      return false;
    }
    if (m_filter != null && !m_filter.test(f)) {
      return false;
    }
    if (m_flags >= 0 && (f.flags() & m_flags) != m_flags) {
      return false;
    }
    if (m_methodNamePattern != null && !m_methodNamePattern.matcher(f.elementName()).matches()) {
      return false;
    }
    if (m_annotationFqn != null && !f.annotations().withName(m_annotationFqn).existsAny()) {
      return false;
    }
    return true;
  }

  protected void visitRec(IType t, List<IMethod> result, int maxCount, boolean onlyTraverse) {
    if (t == null) {
      return;
    }
    if (!onlyTraverse) {
      for (IMethod f : new WrappedList<IMethod>(t.unwrap().getMethods())) {
        if (accept(f)) {
          result.add(f);
          if (result.size() >= maxCount) {
            return;
          }
        }
      }
    }
    if (m_includeSuperClasses || m_includeSuperInterfaces) {
      visitRec(t.superClass(), result, maxCount, !m_includeSuperClasses);
      if (result.size() >= maxCount) {
        return;
      }
    }

    if (m_includeSuperInterfaces) {
      for (IType superInterface : t.superInterfaces()) {
        visitRec(superInterface, result, maxCount, false);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  /**
   * Checks if there is at least one {@link IMethod} that fulfills this query.
   *
   * @return <code>true</code> if at least one {@link IMethod} fulfills this query, <code>false</code> otherwise.
   */
  public boolean existsAny() {
    return first() != null;
  }

  /**
   * Gets the first {@link IMethod} that fulfills this query.
   *
   * @return The first {@link IMethod} that fulfills this query or <code>null</code> if there is none.
   */
  public IMethod first() {
    List<IMethod> result = new ArrayList<>(1);
    visitRec(m_type, result, 1, false);
    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * Gets all {@link IMethod}s that fulfill this query.
   *
   * @return A {@link List} with all {@link IMethod}s that fulfill this query. Never returns <code>null</code>.
   */
  public List<IMethod> list() {
    List<IMethod> result = new ArrayList<>(m_type.unwrap().getMethods().size());
    visitRec(m_type, result, m_maxResultCount, false);
    return result;
  }

}
