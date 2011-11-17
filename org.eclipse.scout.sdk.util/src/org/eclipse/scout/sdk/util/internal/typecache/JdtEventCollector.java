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
package org.eclipse.scout.sdk.util.internal.typecache;

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;

/**
 *
 */
public class JdtEventCollector {

  private final IResource m_resouce;
  private HashMap<IJavaElement, JdtEvent> m_events;
  private long m_lastModification;

  JdtEventCollector(IResource resouce) {
    m_resouce = resouce;
    m_lastModification = m_resouce.getModificationStamp();
    m_events = new HashMap<IJavaElement, JdtEvent>();
  }

  public void addEvent(JdtEvent e) {
    m_events.put(e.getElement(), e);
  }

  public boolean containsEventFor(IJavaElement element) {
    return m_events.containsKey(element);
  }

  public boolean hasEvents() {
    return !m_events.isEmpty();
  }

  public JdtEvent[] getEvents() {
    return m_events.values().toArray(new JdtEvent[m_events.size()]);
  }

  public IResource getResouce() {
    return m_resouce;
  }

  public long getLastModification() {
    return m_lastModification;
  }

}
