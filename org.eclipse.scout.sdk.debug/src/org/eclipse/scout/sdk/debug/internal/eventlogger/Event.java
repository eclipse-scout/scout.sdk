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
package org.eclipse.scout.sdk.debug.internal.eventlogger;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Event {

  public static enum EventGroup {
    JDT_EVENT,
    RESOURCE_EVENT
  }

  public static enum Type {
    CHANGED,
    REMOVED,
    ADDED,
    POST_CHANGE,
    POST_RECONCILE,
    PRE_AUTO_BUILD,
    UNDEFINED,
    POST_BUILD,
    PRE_BUILD,
    PRE_CLOSE,
    PRE_DELETE,
    PRE_REFRESH,
    ADDED_PHANTOM,
    REMOVED_PHANTOM
  }

  private EventGroup m_eventGroup;
  private Type m_eventType;
  private String m_elementType;
  private String m_element;
  private long m_eventTime;
  private List<Event> m_children;

  public Event() {
    this(null, null, null);
  }

  public Event(Type eventType, String elementType, String element) {
    m_eventType = eventType;
    m_elementType = elementType;
    m_element = element;
    m_children = new ArrayList<Event>();
  }

  public Type getEventType() {
    return m_eventType;
  }

  public void setEventType(Type eventType) {
    m_eventType = eventType;
  }

  public String getElementType() {
    return m_elementType;
  }

  public void setElementType(String elementType) {
    m_elementType = elementType;
  }

  public String getElement() {
    return m_element;
  }

  public void setElement(String element) {
    m_element = element;
  }

  public long getEventTime() {
    return m_eventTime;
  }

  public void setEventTime(long eventTime) {
    m_eventTime = eventTime;
  }

  public EventGroup getEventGroup() {
    return m_eventGroup;
  }

  public void setEventGroup(EventGroup eventGroup) {
    m_eventGroup = eventGroup;
  }

  public synchronized boolean isLeaf() {
    return m_children.isEmpty();
  }

  public synchronized void addChildEvent(Event event) {
    m_children.add(event);
  }

  public synchronized Event[] getChildren() {
    return m_children.toArray(new Event[m_children.size()]);
  }
}
