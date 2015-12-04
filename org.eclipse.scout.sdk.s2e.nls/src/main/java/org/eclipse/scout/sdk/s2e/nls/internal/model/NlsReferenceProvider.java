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
package org.eclipse.scout.sdk.s2e.nls.internal.model;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.s2e.nls.internal.ui.editor.IReferenceProvider;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsReferenceProvider</h4>
 */
public class NlsReferenceProvider implements IReferenceProvider {
  private final Map<String, List<Match>> m_matches;

  public NlsReferenceProvider(Map<String, List<Match>> matches) {
    m_matches = matches;
  }

  @Override
  public int getReferenceCount(INlsEntry entry) {
    if (m_matches.containsKey(entry.getKey())) {
      return m_matches.get(entry.getKey()).size();
    }
    return 0;
  }

  @Override
  public Match[] getReferences(INlsEntry entry) {
    if (m_matches.containsKey(entry.getKey())) {
      List<Match> list = m_matches.get(entry.getKey());
      return list.toArray(new Match[list.size()]);
    }
    return new Match[0];
  }

}
