/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.Map;

import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.util.search.FileQueryMatch;

/**
 * <h4>NlsReferenceProvider</h4>
 */
public class NlsReferenceProvider {
  private final Map<String, ? extends Collection<FileQueryMatch>> m_matches;

  public NlsReferenceProvider(Map<String, ? extends Collection<FileQueryMatch>> matches) {
    m_matches = matches;
  }

  public Collection<FileQueryMatch> getReferencesFor(ITranslation entry) {
    if (entry == null) {
      return emptySet();
    }
    return getReferencesFor(entry.key());
  }

  public Collection<FileQueryMatch> getReferencesFor(String key) {
    var references = m_matches.get(key);
    if (references == null) {
      return emptySet();
    }
    return references;
  }
}
