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
package org.eclipse.scout.sdk.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.sdk.core.util.EventListenerList.IWeakEventListener;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link PropertySupportTest}</h3>
 *
 * @since 6.1.0
 */
public class PropertySupportTest {

  private static final String PROP_VALUE = "value";
  private static final String PROP_NAME = "prop";

  @Test
  @SuppressWarnings({"CallToSystemGC", "UnusedAssignment"})
  public void testEventListenerAllProperties() {
    var ps = new PropertySupport(this);
    var l = new P_Listener();
    var wl = new P_WeakListener();
    ps.addPropertyChangeListener(l);
    ps.addPropertyChangeListener(wl);

    ps.setProperty(PROP_NAME, PROP_VALUE);
    assertEquals(PROP_VALUE, ps.getPropertyString(PROP_NAME));
    assertEquals(1, l.m_counter.get());
    assertEquals(1, wl.m_counter.get());

    wl = null;
    System.gc();
    ps.removePropertyChangeListener(l); // should also remove the empty weak-entry
    assertNull(listenerFieldOf(ps));
  }

  @Test
  @SuppressWarnings({"CallToSystemGC", "UnusedAssignment"})
  public void testNoFireWeakListenerAfterReclaim() {
    var ps = new PropertySupport(this);
    PropertyChangeListener wl = new P_WeakListener();
    ps.addPropertyChangeListener(wl);
    wl = null;
    System.gc(); // will remove the weak listener
    ps.setProperty(PROP_NAME, PROP_VALUE);
    assertEquals(0, listenerFieldOf(ps).size());
    ps.removePropertyChangeListener(new P_Listener());
    assertNull(listenerFieldOf(ps));
  }

  @Test
  public void testEventListenerSingleProperty() {
    var ps = new PropertySupport(this);
    var l1 = new P_Listener();
    var l2 = new P_Listener();
    var wl = new P_WeakListener();
    ps.addPropertyChangeListener("whatever", l1);
    ps.addPropertyChangeListener(PROP_NAME, l2);
    ps.addPropertyChangeListener(PROP_NAME, wl);

    ps.setProperty(PROP_NAME, PROP_VALUE);
    assertEquals(PROP_VALUE, ps.getPropertyString(PROP_NAME));
    assertEquals(0, l1.m_counter.get());
    assertEquals(1, l2.m_counter.get());
    assertEquals(1, wl.m_counter.get());

    ps.setProperty(PROP_NAME, PROP_VALUE);
    assertEquals(0, l1.m_counter.get());
    assertEquals(1, l2.m_counter.get());
    assertEquals(1, wl.m_counter.get());

    ps.removePropertyChangeListener(PROP_NAME, l2);
    assertEquals(2, childListenerFieldOf(ps).size());

    ps.setProperty(PROP_NAME, "other");
    assertEquals(0, l1.m_counter.get());
    assertEquals(1, l2.m_counter.get());
    assertEquals(2, wl.m_counter.get());

    ps.removePropertyChangeListener(PROP_NAME, wl);

    assertEquals(1, childListenerFieldOf(ps).size());
    ps.removePropertyChangeListener(l1);
    assertNull(childListenerFieldOf(ps));
  }

  @Test
  public void testPropertyChangeListenerProxy() {
    var ps = new PropertySupport(this);
    var listener = new P_Listener();
    PropertyChangeListener p = new PropertyChangeListenerProxy(PROP_NAME, listener);
    ps.addPropertyChangeListener(p);
    ps.setProperty("other", "whatever");
    ps.setProperty(PROP_NAME, PROP_VALUE);
    assertEquals(1, listener.m_counter.get());
    ps.addPropertyChangeListener(listener);
    ps.removePropertyChangeListener(p);
    ps.setProperty(PROP_NAME, "changed");
    assertEquals(2, listener.m_counter.get());
    ps.removePropertyChangeListener(listener);
    assertNull(childListenerFieldOf(ps));
    assertNull(listenerFieldOf(ps));
  }

  @Test
  public void testNullRemoves() {
    var ps = new PropertySupport();
    var l1 = new P_Listener();
    ps.addPropertyChangeListener(PROP_NAME, l1);
    ps.setProperty(PROP_NAME, PROP_VALUE);
    ps.setProperty(PROP_NAME, null);

    assertEquals(2, l1.m_counter.get());
    assertEquals(0, ps.size());
    ps.removePropertyChangeListener(PROP_NAME, l1);
    assertNull(childListenerFieldOf(ps));
    assertNull(listenerFieldOf(ps));
  }

  @Test
  public void testDataTypes() {
    var ps = new PropertySupport(11);
    ps.setPropertyBool("bool", true);
    ps.setPropertyDouble("double", 1.12);
    ps.setPropertyInt("int", 11);
    ps.setPropertyLong("long", 13L);
    ps.setPropertyString("string", "string");

    assertTrue(ps.getPropertyBool("bool", false));
    assertEquals(1.12, ps.getPropertyDouble("double", 0.0), 0.00001);
    assertEquals(11, ps.getPropertyInt("int", 0));
    assertEquals(13L, ps.getPropertyLong("long", 0L));
    assertEquals("string", ps.getPropertyString("string"));

    assertTrue(ps.getPropertyBool("a", true));
    assertEquals(0.44, ps.getPropertyDouble("b", 0.44), 0.00001);
    assertEquals(4, ps.getPropertyInt("c", 4));
    assertEquals(5L, ps.getPropertyLong("d", 5L));
    assertNull(ps.getPropertyString("e"));

    assertEquals(5, ps.getPropertiesMap().size());
  }

  @Test
  @SuppressWarnings({"unlikely-arg-type", "SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testEqualsAndHashCode() {
    var a = new PropertySupport();
    var b = new PropertySupport("source");
    var c = new PropertySupport();
    c.setProperty(PROP_NAME, PROP_VALUE);
    var d = new PropertySupport();
    var e = new PropertySupport("other");
    assertFalse(a.equals(b));
    assertFalse(a.equals(null));
    assertFalse(a.equals(""));
    assertFalse(a.equals(c));
    assertTrue(a.equals(a));
    assertTrue(a.equals(d));
    assertFalse(b.equals(e));
    assertNotEquals(a.hashCode(), b.hashCode());
    assertNotEquals(new PropertySupport(null), b.hashCode());
  }

  @Test
  public void testNoEventsEvent() {
    var p1 = new PropertySupport();
    var l = new P_Listener();
    p1.addPropertyChangeListener(PROP_NAME, l);
    p1.firePropertyChange(null);
    assertEquals(0, l.m_counter.get());

    var ps = new PropertySupport(this);
    ps.addPropertyChangeListener("whatever", l);
    ps.setProperty(PROP_NAME, PROP_VALUE);
    assertEquals(0, l.m_counter.get());
    assertFalse(ps.hasProperty("blubblub"));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, List<Object>> childListenerFieldOf(PropertySupport owner) {
    try {
      var f = PropertySupport.class.getDeclaredField("m_childListeners");
      f.setAccessible(true);
      return (Map<String, List<Object>>) f.get(owner);
    }
    catch (IllegalAccessException | NoSuchFieldException e) {
      throw new SdkException(e);
    }
  }

  private static EventListenerList listenerFieldOf(PropertySupport owner) {
    try {
      var f = PropertySupport.class.getDeclaredField("m_listeners");
      f.setAccessible(true);
      return (EventListenerList) f.get(owner);
    }
    catch (IllegalAccessException | NoSuchFieldException e) {
      throw new SdkException(e);
    }
  }

  private static final class P_Listener implements PropertyChangeListener {
    private final AtomicInteger m_counter = new AtomicInteger();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      m_counter.incrementAndGet();
    }
  }

  private static final class P_WeakListener implements PropertyChangeListener, IWeakEventListener {
    private final AtomicInteger m_counter = new AtomicInteger();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      m_counter.incrementAndGet();
    }
  }
}
