/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.log;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Optional;

/**
 * <h3>{@link FormattingTuple}</h3> Use {@link MessageFormatter#arrayFormat(CharSequence, Object...)} to obtain a
 * {@link FormattingTuple} instance.
 *
 * @since 6.1.0
 */
public class FormattingTuple {

  private final String m_message;
  private final List<Throwable> m_throwables;

  FormattingTuple(String message, List<Throwable> throwables) {
    m_message = message;
    m_throwables = throwables;
  }

  /**
   * @return The message with all formatting anchors replaced.
   */
  public String message() {
    return m_message;
  }

  /**
   * @return A {@link List} with all {@link Throwable}s of the arguments.
   */
  public List<Throwable> throwables() {
    return unmodifiableList(m_throwables);
  }

  /**
   * @return The first {@link Throwable} of the arguments.
   */
  public Optional<Throwable> firstThrowable() {
    return throwables().stream().findFirst();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    var other = (FormattingTuple) o;
    return m_message.equals(other.m_message) && m_throwables.equals(other.m_throwables);
  }

  @Override
  public int hashCode() {
    return 31 * m_message.hashCode() + m_throwables.hashCode();
  }

  @Override
  public String toString() {
    var sb = new StringBuilder("FormattingTuple{");
    sb.append("message='").append(m_message).append('\'');
    sb.append(", throwables=").append(m_throwables);
    sb.append('}');
    return sb.toString();
  }
}
