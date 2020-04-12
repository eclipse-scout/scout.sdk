/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.log;

import static java.lang.System.lineSeparator;
import static java.util.Collections.unmodifiableList;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link LogMessage}</h3> Represents a message to write to an {@link ISdkConsoleSpi}.
 * <p>
 * Use {@link SdkLog} and its methods to create log messages.
 *
 * @since 6.1.0
 */
public class LogMessage {

  private final Level m_severity;
  private final String m_prefix;
  private final String m_text;
  private final List<Throwable> m_throwables;

  LogMessage(Level severity, String prefix, String text, List<Throwable> throwables) {
    m_severity = Ensure.notNull(severity);
    m_prefix = Ensure.notNull(prefix);
    m_text = Ensure.notNull(text);
    m_throwables = throwables;
  }

  /**
   * @return The main message to log. It corresponds to the message entered in
   *         {@link SdkLog#log(Level, String, Object...)}. If the message used placeholders (see
   *         {@link MessageFormatter}) these have already been replaced. Is never {@code null}.
   */
  public String text() {
    return m_text;
  }

  /**
   * @return A {@link List} with all {@link Throwable}s of the arguments. Neither the {@link List} nor one of its
   *         elements may be {@code null}.
   */
  public List<Throwable> throwables() {
    return unmodifiableList(m_throwables);
  }

  /**
   * @return The first {@link Throwable}.
   */
  public Optional<Throwable> firstThrowable() {
    return throwables().stream().findFirst();
  }

  /**
   * @return The severity of the message. Is never {@code null}.
   */
  public Level severity() {
    return m_severity;
  }

  /**
   * @return The message prefix. It consists of a timestamp and the message severity name. Is never {@code null}.
   */
  public String prefix() {
    return m_prefix;
  }

  /**
   * @return {@link #prefix()} and {@link #text()} and the stack traces of all {@link #throwables()}.
   */
  public String all() {
    StringBuilder logContent = new StringBuilder(prefix()).append(text());
    List<Throwable> throwables = throwables();
    if (!throwables.isEmpty()) {
      Iterator<Throwable> iterator = throwables.iterator();
      Throwable t = iterator.next();
      logContent.append(lineSeparator()).append(Strings.fromThrowable(t));
      while (iterator.hasNext()) {
        t = iterator.next();
        logContent.append(lineSeparator()).append(Strings.fromThrowable(t));
      }
    }
    return logContent.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LogMessage that = (LogMessage) o;
    return m_severity.equals(that.m_severity) &&
        m_prefix.equals(that.m_prefix) &&
        m_text.equals(that.m_text) &&
        m_throwables.equals(that.m_throwables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_severity, m_prefix, m_text, m_throwables);
  }

  @Override
  public String toString() {
    return new StringBuilder(LogMessage.class.getSimpleName())
        .append(": ")
        .append(m_prefix)
        .append(m_text)
        .toString();
  }
}
