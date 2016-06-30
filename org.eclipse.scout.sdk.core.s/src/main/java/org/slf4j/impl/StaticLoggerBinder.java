/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * <h3>{@link StaticLoggerBinder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
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
