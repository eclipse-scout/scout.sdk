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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * <h3>{@link SimpleLoggerFactory}</h3>
 *
 * @since 5.2.0
 */
public class SimpleLoggerFactory implements ILoggerFactory {

  private final ConcurrentMap<String, Logger> m_loggerMap;

  public SimpleLoggerFactory() {
    m_loggerMap = new ConcurrentHashMap<>();
  }

  @Override
  public Logger getLogger(String name) {
    var simpleLogger = m_loggerMap.get(name);
    if (simpleLogger != null) {
      return simpleLogger;
    }

    Logger newInstance = new SimpleLogger(name);
    var oldInstance = m_loggerMap.putIfAbsent(name, newInstance);
    if (oldInstance == null) {
      return newInstance;
    }
    return oldInstance;
  }

  void reset() {
    m_loggerMap.clear();
  }
}
