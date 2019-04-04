/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

    TranslationEntry other = (TranslationEntry) obj;
    return m_store.equals(other.m_store);
  }

  @Override
  public String toString() {
    return super.toString() + " in " + m_store;
  }
}
