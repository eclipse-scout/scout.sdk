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
package org.eclipse.scout.nls.sdk.simple.ui.dialog.language;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.smartfield.ISmartFieldModel;
import org.eclipse.swt.graphics.Image;

public class LanguageSmartFieldModel implements ISmartFieldModel {

  Locale[] m_locales;

  public LanguageSmartFieldModel() {
    ArrayList<Locale> locs = new ArrayList<Locale>();
    for (String isoLang : Locale.getISOLanguages()) {
      locs.add(new Locale(isoLang));
    }
    m_locales = locs.toArray(new Locale[locs.size()]);
  }

  @Override
  public Image getImage(Object item) {
    return NlsCore.getImage(NlsCore.ICON_COMMENT);
  }

  @Override
  public List<Object> getProposals(String pattern) {
    List<Object> props = new LinkedList<Object>();
    for (Locale l : m_locales) {
      if (l.getDisplayLanguage().toLowerCase().startsWith(pattern.toLowerCase())) {
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
    return l.getDisplayLanguage() + " (" + l.getLanguage() + ")";
  }

  public void itemSelected(Object item) {
  }

}
