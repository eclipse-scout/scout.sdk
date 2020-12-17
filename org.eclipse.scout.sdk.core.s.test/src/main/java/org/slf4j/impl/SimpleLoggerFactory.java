/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
