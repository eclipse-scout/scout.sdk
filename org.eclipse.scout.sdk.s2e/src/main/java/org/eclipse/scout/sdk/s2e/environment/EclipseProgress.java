/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.environment;

import static org.eclipse.scout.sdk.core.log.MessageFormatter.arrayFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link EclipseProgress}</h3>
 *
 * @since 7.0.0
 */
public class EclipseProgress implements IProgress {

  private final SubMonitor m_monitor;

  /**
   * @param inner
   *          May be {@code null}.
   */
  protected EclipseProgress(IProgressMonitor inner) {
    this(SubMonitor.convert(inner));
  }

  /**
   * @param inner
   *          Must not be {@code null}.
   */
  protected EclipseProgress(SubMonitor inner) {
    m_monitor = Ensure.notNull(inner);
  }

  @Override
  public EclipseProgress init(int totalWork, CharSequence name, Object... args) {
    var message = arrayFormat(name, args).message();
    SdkLog.debug(message);
    m_monitor.beginTask(message, totalWork);
    return this;
  }

  @Override
  public EclipseProgress newChild(int work) {
    return new EclipseProgress(m_monitor.split(work));
  }

  public SubMonitor monitor() {
    return m_monitor;
  }

  @Override
  public EclipseProgress setWorkRemaining(int i) {
    m_monitor.setWorkRemaining(i);
    return this;
  }

  @Override
  public EclipseProgress worked(int work) {
    m_monitor.worked(work);
    return this;
  }
}
