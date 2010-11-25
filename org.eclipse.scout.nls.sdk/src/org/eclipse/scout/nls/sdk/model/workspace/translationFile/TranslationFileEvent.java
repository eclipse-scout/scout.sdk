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
package org.eclipse.scout.nls.sdk.model.workspace.translationFile;

import java.util.ArrayList;
import java.util.List;

/**
 * <h4>TranslationFileEvent</h4>
 */
public class TranslationFileEvent {
  public static final int TYPE_ENTRY_ADD = 1 << 0;
  public static final int TYPE_ENTRY_REMOVE = 1 << 1;
  public static final int TYPE_ENTRY_MODIFY = 1 << 2;
  public static final int TYPE_FILE_REMOVED = 1 << 10;

  private List<TranslationFileEvent> m_subEvents;
  private int m_type;

  private String m_translation;
  private String m_key;
  private final ITranslationFile m_source;

  public TranslationFileEvent(ITranslationFile source) {
    m_source = source;
    m_subEvents = new ArrayList<TranslationFileEvent>();
  }

  public TranslationFileEvent(ITranslationFile source, int type) {
    m_source = source;
    m_type = type;
  }

  public TranslationFileEvent(ITranslationFile source, String key, String translation, int type) {
    m_source = source;
    m_key = key;
    m_translation = translation;
    m_type = type;
  }

  public ITranslationFile getSource() {
    return m_source;
  }

  public boolean isMulti() {
    return m_subEvents != null;
  }

  public void addEvent(TranslationFileEvent event) {
    m_subEvents.add(event);
  }

  public boolean removeEvent(TranslationFileEvent event) {
    return m_subEvents.remove(event);
  }

  public TranslationFileEvent[] getSubEvents() {
    if (m_subEvents == null) {
      return new TranslationFileEvent[0];
    }
    else {
      return m_subEvents.toArray(new TranslationFileEvent[m_subEvents.size()]);
    }
  }

  public int getType() {
    return m_type;
  }

  public String getTranslation() {
    return m_translation;
  }

  public String getKey() {
    return m_key;
  }

}
