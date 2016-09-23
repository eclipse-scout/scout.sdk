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

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.WrappedList;

/**
 * <h3>{@link FieldQuery}</h3> Field query that by default returns all {@link IField}s directly declared on the owner.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class FieldQuery {
  private final IType m_type;
  private boolean m_includeSuperClasses = false;
  private boolean m_includeSuperInterfaces = false;
  private String m_name;
  private int m_flags = -1;
  private Predicate<IField> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public FieldQuery(IType type) {
    m_type = type;
  }

  /**
   * Include or exclude super types visiting when searching for {@link IField}s.
   *
   * @param b
   *          <code>true</code> if all super classes and super interfaces should be checked for {@link IField}s. Default
   *          is <code>false</code>.
   * @return this
   */
  public FieldQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Include or exclude super class visiting when searching for {@link IField}s.
   *
   * @param b
   *          <code>true</code> if all super classes should be checked for {@link IField}s. Default is
   *          <code>false</code>.
   * @return this
   */
  public FieldQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * Include or exclude super interface visiting when searching for {@link IField}s.
   *
   * @param b
   *          <code>true</code> if all super interfaces should be checked for {@link IField}s. Default is
   *          <code>false</code>.
   * @return this
   */
  public FieldQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Limit the {@link IField}s to the ones having at least all of the given flags.
   *
   * @param flags
   *          The flags that must exist on the {@link IField}.
   * @return this
   * @see Flags
   */
  public FieldQuery withFlags(int flags) {
    m_flags = flags;
    return this;
  }

  /**
   * Limit the {@link IField}s to the given name (see {@link IField#elementName()}).
   *
   * @param name
   *          The {@link IField} name. Default is no filtering.
   * @return this
   */
  public FieldQuery withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * Limit the {@link IField}s to the ones that accept the given {@link Predicate}.
   *
   * @param filter
   *          The filter. Default none.
   * @return this
   */
  public FieldQuery withFilter(Predicate<IField> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * Limit the number of {@link IField}s to search.
   *
   * @param maxResultCount
   *          The maximum number of fields to search. Default is unlimited.
   * @return this
   */
  public FieldQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IField f) {
    if (m_name != null && !m_name.equals(f.elementName())) {
      return false;
    }
    if (m_flags >= 0 && (f.flags() & m_flags) != m_flags) {
      return false;
    }
    if (m_filter != null && !m_filter.test(f)) {
      return false;
    }
    return true;
  }

  protected void visitRec(IType t, List<IField> result, int maxCount, boolean onlyTraverse) {
    if (t == null) {
      return;
    }
    if (!onlyTraverse) {
      for (IField f : new WrappedList<IField>(t.unwrap().getFields())) {
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
   * Checks if there is at least one {@link IField} that fulfills this query.
   *
   * @return <code>true</code> if at least one {@link IField} fulfills this query, <code>false</code> otherwise.
   */
  public boolean existsAny() {
    return first() != null;
  }

  /**
   * Gets the first {@link IField} that fulfills this query.
   *
   * @return The first {@link IField} that fulfills this query or <code>null</code> if there is none.
   */
  public IField first() {
    List<IField> result = new ArrayList<>(1);
    visitRec(m_type, result, 1, false);
    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * Gets all {@link IField}s that fulfill this query.
   *
   * @return A {@link List} with all {@link IField}s that fulfill this query. Never returns <code>null</code>.
   */
  public List<IField> list() {
    List<IField> result = new ArrayList<>(m_type.unwrap().getFields().size());
    visitRec(m_type, result, m_maxResultCount, false);
    return result;
  }

}
