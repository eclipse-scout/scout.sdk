/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.nls.sdk.model.util;

public class NlsGroupEvent {

  public static final int TYPE_UPDATE_ROW = 1 << 1;
  public static final int ROOT_FILE_CHANGED = 1 << 2;
  public static final int FULL_UPDATE = 1 << 3;
  public static final int MATCHES_VALID = 1 << 4;
  public static final int TYPE_NLS_FILE_ADDED = 1 << 5;
  public static final int TYPE_ROW_ADDED = 1 << 6;
  public static final int TYPE_KEY_UPDATE = 1 << 7;
  public static final int TYPE_ROW_REMOVED = 1 << 8;
  public static final int TYPE_ROW_MODIFIED = 1 << 9;

  private Object[] m_args;
  private int m_eventType;

  public NlsGroupEvent(int eventType) {
    this(eventType, new Object[]{});
  }

  public NlsGroupEvent(int eventType, Object[] args) {
    m_args = args;
    m_eventType = eventType;
  }

  public Object[] getArgs() {
    return m_args;
  }

  public int getEventType() {
    return m_eventType;
  }
}
