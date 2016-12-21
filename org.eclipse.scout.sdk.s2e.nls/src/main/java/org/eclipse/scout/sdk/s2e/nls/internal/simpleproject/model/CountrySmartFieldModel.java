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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleproject.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.ISmartFieldModel;
import org.eclipse.swt.graphics.Image;

public class CountrySmartFieldModel implements ISmartFieldModel {

  private final List<Locale> m_locales;

  public CountrySmartFieldModel() {
    String[] isoCountries = Locale.getISOCountries();
    List<Locale> locs = new ArrayList<>(isoCountries.length);
    for (String isoCountry : isoCountries) {
      locs.add(new Locale("", isoCountry));
    }
    m_locales = locs;
  }

  @Override
  public Image getImage(Object item) {
    return NlsCore.getImage(INlsIcons.COMMENT);
  }

  @Override
  public List<Object> getProposals(String pattern) {
    Map<String, Object> props = new TreeMap<>();
    for (Locale l : m_locales) {
      if (l.getDisplayCountry().toLowerCase().startsWith(pattern.toLowerCase())) {
        props.put(getText(l), l);
      }
    }
    return new ArrayList<>(props.values());
  }

  @Override
  public String getText(Object item) {
    if (item == null) {
      return "";
    }
    Locale loc = (Locale) item;
    String dc = loc.getDisplayCountry();
    String c = loc.getCountry();

    StringBuilder b = new StringBuilder(dc.length() + c.length() + 3);
    b.append(dc);
    b.append(" (");
    b.append(c);
    b.append(')');
    return b.toString();
  }
}
