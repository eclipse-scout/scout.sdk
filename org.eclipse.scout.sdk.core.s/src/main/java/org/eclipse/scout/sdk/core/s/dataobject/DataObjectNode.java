/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dataobject;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * Represents a DataObject node (e.g. DoValue or DoList)
 */
public class DataObjectNode {

  /**
   * The Node type
   */
  public enum DataObjectNodeKind {
    DO_VALUE,
    DO_LIST;

    /**
     * Parses the given {@link IType} to the {@link DataObjectNodeKind}.<br>
     * If it is the {@code DoValue} type, {@link #DO_VALUE} is returned.<br>
     * If it is the {@code DoList} type, {@link #DO_LIST} is returned.<br>
     * Otherwise an empty optional is returned.
     * 
     * @param t
     *          The {@link IType} to check or {@code null}.
     * @return An {@link Optional} holding the parsed value or an empty one if it cannot be parsed.
     */
    public static Optional<DataObjectNodeKind> valueOf(IType t) {
      if (t == null) {
        return Optional.empty();
      }

      var optScoutApi = t.javaEnvironment().api(IScoutApi.class);
      if (optScoutApi.isEmpty()) {
        return Optional.empty();
      }

      var scoutApi = optScoutApi.get();
      var name = t.name();
      if (scoutApi.DoValue().fqn().equals(name)) {
        return Optional.of(DO_VALUE);
      }
      if (scoutApi.DoList().fqn().equals(name)) {
        return Optional.of(DO_LIST);
      }
      return Optional.empty();
    }
  }

  private final DataObjectNodeKind m_kind;
  private final String m_name;
  private final IType m_dataType;
  private final boolean m_inherited;

  public DataObjectNode(DataObjectNodeKind kind, String name, IType dataType, boolean inherited) {
    m_kind = Ensure.notNull(kind);
    m_name = Ensure.notBlank(name);
    m_dataType = Ensure.notNull(dataType);
    m_inherited = inherited;
  }

  /**
   * @return {@link DataObjectNodeKind#DO_VALUE} or {@link DataObjectNodeKind#DO_LIST}. Is never {@code null}.
   */
  public DataObjectNodeKind kind() {
    return m_kind;
  }

  /**
   * @return The node name. Is never {@code null}.
   */
  public String name() {
    return m_name;
  }

  /**
   * @return The data type of the node. Is never {@code null}. If the node is of kind {@link DataObjectNodeKind#DO_LIST}
   *         this data type represents the list element type.
   */
  public IType dataType() {
    return m_dataType;
  }

  /**
   * @return {@code true} if this node is inherited from a super type.
   */
  public boolean isInherited() {
    return m_inherited;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataObjectNode.class.getSimpleName() + " [", "]")
        .add("name='" + m_name + "'")
        .add("kind=" + m_kind)
        .add("dataType='" + m_dataType + "'")
        .add("inherited=" + m_inherited)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    var that = (DataObjectNode) o;
    return m_inherited == that.m_inherited
        && m_kind == that.m_kind
        && Objects.equals(m_dataType, that.m_dataType)
        && Objects.equals(m_name, that.m_name);
  }

  @Override
  public int hashCode() {
    var result = m_kind.hashCode();
    result = 31 * result + m_name.hashCode();
    result = 31 * result + m_dataType.hashCode();
    result = 31 * result + (m_inherited ? 1 : 0);
    return result;
  }
}
