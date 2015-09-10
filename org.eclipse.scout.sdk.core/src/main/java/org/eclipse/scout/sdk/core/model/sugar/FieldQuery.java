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

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link FieldQuery}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class FieldQuery {
  private final IType m_type;
  private boolean m_includeSuperClasses = false;
  private boolean m_includeSuperInterfaces = false;
  private String m_name;
  private IFilter<IField> m_filter;
  private int m_maxResultCount = Integer.MAX_VALUE;

  public FieldQuery(IType type) {
    m_type = type;
  }

  /**
   * Include / Exclude super classes and super types for visiting
   *
   * @param b
   *          default false
   * @return this
   */
  public FieldQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param b
   *          default false
   * @return this
   */
  public FieldQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  /**
   * @param b
   *          default false
   * @return this
   */
  public FieldQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * @param name
   * @return this
   */
  public FieldQuery withName(String name) {
    m_name = name;
    return this;
  }

  /**
   * @param filter
   * @return this
   */
  public FieldQuery withFilter(IFilter<IField> filter) {
    m_filter = filter;
    return this;
  }

  /**
   * @param maxResultCount
   * @return this
   */
  public FieldQuery withMaxResultCount(int maxResultCount) {
    m_maxResultCount = maxResultCount;
    return this;
  }

  protected boolean accept(IField f) {
    if (m_name != null && !m_name.equals(f.getElementName())) {
      return false;
    }
    if (m_filter != null && !m_filter.evaluate(f)) {
      return false;
    }
    return true;
  }

  protected void visitRec(IType t, List<IField> result, int maxCount) {
    if (t == null) {
      return;
    }
    for (IField f : t.getFields()) {
      if (accept(f)) {
        result.add(f);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
    if (m_includeSuperClasses) {
      visitRec(t.getSuperClass(), result, maxCount);
      if (result.size() >= maxCount) {
        return;
      }
    }

    if (m_includeSuperInterfaces) {
      for (IType superInterface : t.getSuperInterfaces()) {
        visitRec(superInterface, result, maxCount);
        if (result.size() >= maxCount) {
          return;
        }
      }
    }
  }

  public boolean exists() {
    return first() != null;
  }

  public IField first() {
    ArrayList<IField> result = new ArrayList<>(1);
    visitRec(m_type, result, 1);
    return result.isEmpty() ? null : result.get(0);
  }

  public List<IField> list() {
    ArrayList<IField> result = new ArrayList<>();
    visitRec(m_type, result, m_maxResultCount);
    return result;
  }

}
