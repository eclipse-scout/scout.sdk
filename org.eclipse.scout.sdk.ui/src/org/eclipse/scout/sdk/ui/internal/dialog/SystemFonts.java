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
package org.eclipse.scout.sdk.ui.internal.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.graphics.FontData;

/**
 * <h3>{@link SystemFonts}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 17.11.2010
 */
public class SystemFonts {
  private Map<String /*font name*/, List<FontData>> m_fonts;

  public SystemFonts(FontData[] fontData) {
    m_fonts = new HashMap<String, List<FontData>>();
    if (fontData != null) {
      for (FontData d : fontData) {
        List<FontData> set = m_fonts.get(d.getName());
        if (set == null) {
          set = new ArrayList<FontData>();
          m_fonts.put(d.getName(), set);
        }
        set.add(d);
      }
    }
  }

  public Collection<String> getAllFontNames() {
    TreeMap<String, String> fontNames = new TreeMap<String, String>();
    for (String fontName : m_fonts.keySet()) {
      fontNames.put(fontName.toLowerCase(), fontName);
    }
    return fontNames.values();
  }

  public Set<Integer> getFontStyles(String fontName) {
    Set<Integer> sizes = new TreeSet<Integer>();
    List<FontData> set = m_fonts.get(fontName);
    if (set != null) {
      for (FontData d : set) {
        sizes.add(d.getStyle());
      }
    }
    return sizes;
  }

}
