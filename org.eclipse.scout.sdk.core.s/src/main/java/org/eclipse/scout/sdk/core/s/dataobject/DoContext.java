/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dataobject;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Represents the context in which a data object exists. This includes namespace and type version. Use
 * {@link DoContextResolvers#resolve(CharSequence, IJavaEnvironment)} to obtain an instance.
 */
public class DoContext {

  private final IType m_namespace;
  private final String m_namespaceId;
  private final IType m_typeVersion;

  protected DoContext() {
    this(null, null);
  }

  protected DoContext(IType namespace, IType typeVersion) {
    m_namespace = namespace;
    m_typeVersion = typeVersion;
    m_namespaceId = parseId(namespace);
  }

  private static String parseId(IType namespace) {
    return Optional.ofNullable(namespace)
        .flatMap(n -> n.fields().withName("ID").first())
        .flatMap(IField::constantValue)
        .map(v -> v.as(String.class))
        .orElse(null);
  }

  /**
   * @return The INamespace class of this {@link DoContext}.
   */
  public Optional<IType> namespace() {
    return Optional.ofNullable(m_namespace);
  }

  /**
   * @return The ID of the {@link #namespace()}.
   */
  public Optional<String> namespaceId() {
    return Strings.notBlank(m_namespaceId);
  }

  /**
   * @return The ITypeVersion class of this {@link DoContext}.
   */
  public Optional<IType> typeVersion() {
    return Optional.ofNullable(m_typeVersion);
  }

  @Override
  public String toString() {
    return DoContext.class.getSimpleName() + " [" +
        "namespace=" + (m_namespace == null ? null : m_namespace.name()) +
        ", namespaceId=" + m_namespaceId +
        ", typeVersion=" + (m_typeVersion == null ? null : m_typeVersion.name()) + ']';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var doContext = (DoContext) o;
    return Objects.equals(m_namespace, doContext.m_namespace) && Objects.equals(m_typeVersion, doContext.m_typeVersion);
  }

  @Override
  public int hashCode() {
    var result = m_namespace != null ? m_namespace.hashCode() : 0;
    result = 31 * result + (m_typeVersion != null ? m_typeVersion.hashCode() : 0);
    return result;
  }
}
