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

import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.apidef.IScout22DoApi;
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
    VALUE,
    LIST,
    SET,
    COLLECTION;

    /**
     * Parses the given {@link IType} to the {@link DataObjectNodeKind}.<br>
     * If it is the {@code DoValue} type, {@link #VALUE} is returned.<br>
     * If it is the {@code DoList} type, {@link #LIST} is returned.<br>
     * If it is the {@code DoSet} type, {@link #SET} is returned.<br>
     * If it is the {@code DoCollection} type, {@link #COLLECTION} is returned.<br>
     * Otherwise an empty optional is returned.
     * 
     * @param type
     *          The {@link IType} to check or {@code null}.
     * @return An {@link Optional} holding the parsed value or an empty one if it cannot be parsed.
     */
    public static Optional<DataObjectNodeKind> valueOf(IType type) {
      return Optional.ofNullable(type)
          .flatMap(t -> t.javaEnvironment()
              .api(IScoutApi.class)
              .flatMap(api -> detectDoNodeKind(api, t)));
    }

    static Optional<DataObjectNodeKind> detectDoNodeKind(IScoutApi scoutApi, IType t) {
      var name = t.name();
      if (scoutApi.DoValue().fqn().equals(name)) {
        return Optional.of(VALUE);
      }
      if (scoutApi.DoList().fqn().equals(name)) {
        return Optional.of(LIST);
      }
      return scoutApi.api(IScout22DoApi.class)
          .flatMap(a -> detectDoCollection(a, name));
    }

    static Optional<DataObjectNodeKind> detectDoCollection(IScout22DoApi extendedApi, String name) {
      if (extendedApi.DoSet().fqn().equals(name)) {
        return Optional.of(SET);
      }
      if (extendedApi.DoCollection().fqn().equals(name)) {
        return Optional.of(COLLECTION);
      }
      return Optional.empty();
    }
  }

  private final DataObjectNodeKind m_kind;
  private final IMethod m_method;
  private final IType m_dataType;
  private final boolean m_inherited;
  private final boolean m_hasJavaDoc;

  public DataObjectNode(DataObjectNodeKind kind, IMethod method, IType dataType, boolean inherited, boolean hasJavaDoc) {
    m_kind = Ensure.notNull(kind);
    m_method = Ensure.notNull(method);
    m_dataType = Ensure.notNull(dataType);
    m_inherited = inherited;
    m_hasJavaDoc = hasJavaDoc;
  }

  /**
   * @return One of the {@link DataObjectNodeKind} values. Is never {@code null}.
   */
  public DataObjectNodeKind kind() {
    return m_kind;
  }

  /**
   * @return The {@link IMethod} defining the DataObject node. Is never {@code null}.
   */
  public IMethod method() {
    return m_method;
  }

  /**
   * @return The node name. Is never {@code null}.
   */
  public String name() {
    return m_method.elementName();
  }

  /**
   * @return The data type of the node. Is never {@code null}. If the node is of kind {@link DataObjectNodeKind#LIST},
   *         {@link DataObjectNodeKind#SET} or {@link DataObjectNodeKind#COLLECTION} this data type represents the list
   *         element type.
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

  /**
   * @return {@code true} if this node has a JavaDoc
   */
  public boolean hasJavaDoc() {
    return m_hasJavaDoc;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataObjectNode.class.getSimpleName() + " [", "]")
        .add("name='" + name() + "'")
        .add("kind=" + kind())
        .add("dataType='" + dataType() + "'")
        .add("inherited=" + isInherited())
        .add("hasJavaDoc=" + hasJavaDoc())
        .toString();
  }

  @Override
  @SuppressWarnings("squid:S1067") // Number of conditional operators
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    var that = (DataObjectNode) o;
    return m_inherited == that.m_inherited
        && m_hasJavaDoc == that.m_hasJavaDoc
        && m_kind == that.m_kind
        && Objects.equals(m_dataType, that.m_dataType)
        && Objects.equals(m_method, that.m_method);
  }

  @Override
  public int hashCode() {
    var result = m_kind.hashCode();
    result = 31 * result + m_method.hashCode();
    result = 31 * result + m_dataType.hashCode();
    result = 31 * result + (m_inherited ? 1 : 0);
    result = 31 * result + (m_hasJavaDoc ? 1 : 0);
    return result;
  }
}
