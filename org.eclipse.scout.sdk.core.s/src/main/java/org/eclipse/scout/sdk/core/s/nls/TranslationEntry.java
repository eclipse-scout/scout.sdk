/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link TranslationEntry}</h3>
 *
 * @since 7.0.0
 */
public class TranslationEntry extends Translation implements ITranslationEntry {

  private final ITranslationStore m_store;

  public TranslationEntry(ITranslation template, ITranslationStore store) {
    super(template);
    Ensure.notBlank(template.key());
    m_store = Ensure.notNull(store);
  }

  public TranslationEntry(String key, ITranslationStore store) {
    super(Ensure.notBlank(key));
    m_store = Ensure.notNull(store);
  }

  @Override
  public ITranslationStore store() {
    return m_store;
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + m_store.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }

    var other = (TranslationEntry) obj;
    return m_store.equals(other.m_store);
  }

  @Override
  public String toString() {
    return super.toString() + " in " + m_store;
  }
}
