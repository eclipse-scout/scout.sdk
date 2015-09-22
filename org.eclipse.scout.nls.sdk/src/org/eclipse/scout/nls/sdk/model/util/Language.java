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
package org.eclipse.scout.nls.sdk.model.util;

import java.util.Locale;

import org.eclipse.scout.nls.sdk.internal.NlsCore;

public class Language {

  public static final Language LANGUAGE_DEFAULT = new Language(new Locale("default"));
  public static final Language LANGUAGE_KEY = new Language(new Locale("key"));

  private boolean m_isLocal;
  private final Locale m_locale;

  public Language(Locale locale) {
    if (locale == null) {
      IllegalArgumentException e = new IllegalArgumentException("locale cannot be null!");
      NlsCore.logError(e.getMessage(), e);
      throw e;
    }
    m_locale = locale;
  }

  public Locale getLocale() {
    return m_locale;
  }

  public String getDispalyName() {
    if (m_locale == LANGUAGE_DEFAULT.getLocale()) {
      return "default";
    }
    return m_locale.getDisplayName();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Language)) {
      return false;
    }
    return hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return m_locale.hashCode();
  }

  @Override
  public String toString() {
    return getDispalyName();
  }

  public String getIsoCode() {
    return m_locale.toString();
  }

  public boolean isLocal() {
    return m_isLocal;
  }

  public void setLocal(boolean isLocal) {
    m_isLocal = isLocal;
  }
}