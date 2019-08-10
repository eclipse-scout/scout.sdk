/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsReferenceProvider</h4>
 */
public class NlsReferenceProvider {
  private final Map<String, List<Match>> m_matches;

  public NlsReferenceProvider(Map<String, List<Match>> matches) {
    m_matches = matches;
  }

  public List<Match> getReferencesFor(ITranslation entry) {
    return m_matches.get(entry.key());
  }

  public Match[] getReferences(ITranslation entry) {
    if (m_matches.containsKey(entry.key())) {
      List<Match> list = m_matches.get(entry.key());
      return list.toArray(new Match[0]);
    }
    return new Match[0];
  }
}
