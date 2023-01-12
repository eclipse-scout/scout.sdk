/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * <h3>{@link StaticLoggerBinder}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("deprecation")
public final class StaticLoggerBinder implements LoggerFactoryBinder {

  private static final StaticLoggerBinder INSTANCE = new StaticLoggerBinder();
  private final ILoggerFactory m_loggerFactory;

  public static StaticLoggerBinder getSingleton() {
    return INSTANCE;
  }

  private StaticLoggerBinder() {
    m_loggerFactory = new SimpleLoggerFactory();
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return m_loggerFactory;
  }

  @Override
  public String getLoggerFactoryClassStr() {
    return SimpleLoggerFactory.class.getName();
  }
}
