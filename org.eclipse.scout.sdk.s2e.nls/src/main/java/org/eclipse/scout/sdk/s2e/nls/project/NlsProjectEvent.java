/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.resource.ITranslationResource;

/**
 * <h4>NlsProjectEvent</h4>
 */
public class NlsProjectEvent {
  public static final int TYPE_ENTRY_ADDED = 1 << 0;
  public static final int TYPE_ENTRY_REMOVED = 1 << 1;
  public static final int TYPE_ENTRY_MODIFYED = 1 << 2;
  public static final int TYPE_REFRESH = 1 << 3;
  public static final int TYPE_FULL_REFRESH = 1 << 4;
  public static final int TYPE_TRANSLATION_RESOURCE_ADDED = 1 << 5;
  public static final int TYPE_TRANSLATION_RESOURCE_REMOVED = 1 << 6;

  private final INlsProject m_source;
  private final int m_type;
  private final List<NlsProjectEvent> m_childEvents;
  private final ITranslationResource m_resource;
  private final INlsEntry m_entry;

  public NlsProjectEvent(INlsProject source) {
    this(source, 0);
  }

  public NlsProjectEvent(INlsProject source, int type) {
    this(source, null, null, type);
  }

  public NlsProjectEvent(INlsProject source, ITranslationResource r, int type) {
    this(source, r, null, type);
  }

  public NlsProjectEvent(INlsProject source, INlsEntry entry, int type) {
    this(source, null, entry, type);
  }

  private NlsProjectEvent(INlsProject source, ITranslationResource r, INlsEntry entry, int type) {
    m_source = source;
    m_entry = entry;
    m_type = type;
    m_resource = r;
    m_childEvents = new ArrayList<>();
  }

  public boolean isMultiEvent() {
    return m_childEvents != null && m_childEvents.size() > 0;
  }

  public void addChildEvent(NlsProjectEvent event) {
    m_childEvents.add(event);
  }

  public void removeChildEvent(NlsProjectEvent event) {
    m_childEvents.remove(event);
  }

  public NlsProjectEvent[] getChildEvents() {
    if (m_childEvents == null) {
      return new NlsProjectEvent[0];
    }
    return m_childEvents.toArray(new NlsProjectEvent[m_childEvents.size()]);
  }

  public INlsProject getSource() {
    return m_source;
  }

  public INlsEntry getEntry() {
    return m_entry;
  }

  public int getType() {
    return m_type;
  }

  public ITranslationResource getResource() {
    return m_resource;
  }
}
