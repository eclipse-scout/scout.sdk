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
package org.eclipse.scout.nls.sdk.model.workspace.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;

/** <h4>NlsProjectEvent</h4> */
public class NlsProjectEvent {
  public static final int TYPE_ENTRY_ADDED = 1 << 0;
  public static final int TYPE_ENTRY_REMOVEED = 1 << 1;
  public static final int TYPE_ENTRY_MODIFYED = 1 << 2;
  public static final int TYPE_REFRESH = 1 << 3;
  public static final int TYPE_FULL_REFRESH = 1 << 4;
  public static final int TYPE_TRANSLATION_FILE_ADDED = 1 << 5;
  public static final int TYPE_TRANSLATION_FILE_REMOVED = 1 << 6;

  private final INlsProject m_source;
  private INlsEntry m_entry;
  private int m_type;

  private List<NlsProjectEvent> m_childEvents;
  private ITranslationFile m_file;

  public NlsProjectEvent(INlsProject source) {
    m_source = source;
    m_childEvents = new ArrayList<NlsProjectEvent>();
  }

  public NlsProjectEvent(INlsProject source, int type) {
    m_source = source;
    m_type = type;
  }

  public NlsProjectEvent(INlsProject source, ITranslationFile file, int type) {
    m_source = source;
    m_file = file;

  }

  public NlsProjectEvent(INlsProject source, INlsEntry entry, int type) {
    m_source = source;
    m_entry = entry;
    m_type = type;
  }

  public boolean isMultiEvent() {
    return m_childEvents != null;
  }

  public void addChildEvent(NlsProjectEvent event) {
    Assert.isNotNull(m_childEvents);
    m_childEvents.add(event);
  }

  public void removeChildEvent(NlsProjectEvent event) {
    Assert.isNotNull(m_childEvents);
    m_childEvents.remove(event);
  }

  public NlsProjectEvent[] getChildEvents() {
    if (m_childEvents == null) {
      return new NlsProjectEvent[0];
    }
    else {
      return m_childEvents.toArray(new NlsProjectEvent[m_childEvents.size()]);
    }
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

  public ITranslationFile getFile() {
    return m_file;
  }
}
