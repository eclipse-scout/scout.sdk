/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
