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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
    List<Object> props = new LinkedList<>();
    for (Locale l : m_locales) {
      if (l.getDisplayCountry().toLowerCase().startsWith(pattern.toLowerCase())) {
        props.add(l);
      }
    }
    return props;
  }

  @Override
  public String getText(Object item) {
    if (item == null) {
      return "";
    }
    Locale l = (Locale) item;
    return l.getDisplayCountry() + " (" + l.getCountry() + ")";
  }
}
