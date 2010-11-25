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
package org.eclipse.scout.nls.sdk.operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class TranslationHandler {
  public static final String KEY_ORIGINAL = "original";

  private HashMap<String, String[]> m_texts = new HashMap<String, String[]>();
  private HashMap<String, Integer> m_languageIndexMapping = new HashMap<String, Integer>();
  private String[] m_languages;

  public TranslationHandler(String[] languages) {
    m_languages = languages;
    for (int i = 0; i < languages.length; i++) {
      m_languageIndexMapping.put(languages[i], new Integer(i));
    }
  }

  public TranslationHandler() {

  }

  public boolean add(String key, String language, String text) {
    String[] allLanguages = m_texts.get(key);
    if (allLanguages == null) {
      allLanguages = new String[m_languageIndexMapping.size()];
      m_texts.put(key, allLanguages);
    }
    if (allLanguages[getLanguageIndex(language)] != null) {
      System.err.println("Double translated word: key=" + key + " text=" + text + " language=" + language);
      return false;
    }
    allLanguages[getLanguageIndex(language)] = text;
    return true;
  }

  public HashMap<String, String> getLanguageMap(String language, String missingWordSubstitution) {
    int languageIndex = getLanguageIndex(language);
    HashMap<String, String> map = new HashMap<String, String>();
    for (Entry<String, String[]> entry : m_texts.entrySet()) {
      if (entry.getValue()[languageIndex] == null) {
        if (missingWordSubstitution != null) {
          map.put(entry.getKey(), missingWordSubstitution);
        }
      }
      else {
        map.put(entry.getKey(), entry.getValue()[languageIndex]);
      }
    }
    return map;
  }

  public String[] getAllLanguages() {

    return m_languages;
  }

  public String[] getTranslations(String original) {
    return m_texts.get(original);
  }

  public HashMap<String, String[]> getTexts() {
    return m_texts;
  }

  public int getLineCount() {
    return m_texts.size();
  }

  public void writeCsv(OutputStream out) throws IOException {
    char separator = ';';
    out.write("key".getBytes());
    out.write(separator);
    for (String language : m_languages) {
      out.write(language.getBytes());
      out.write(separator);
    }
    out.write('\n');
    for (Iterator<Entry<String, String[]>> it = m_texts.entrySet().iterator(); it.hasNext();) {
      Entry<String, String[]> entry = it.next();
      out.write(entry.getKey().getBytes());
      out.write(separator);
      for (String text : entry.getValue()) {
        if (text != null) {
          out.write(text.getBytes());
        }
        out.write(separator);
      }
      out.write('\n');
    }
  }

  public void buildUpFromStream(InputStream stream) throws IOException {
    cleanUp();
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    try {
      // first language line
      String line = reader.readLine();
      if (line != null) {
        line = line.trim();
        parseLanguageLine(line);
        line = reader.readLine();
        while (line != null) {
          line = line.trim();
          // DOIT
          parseCsvLine(line);
          line = reader.readLine();
        }
      }
    }
    finally {
      reader.close();
    }
  }

  private void parseLanguageLine(String line) {
    String[] args = line.split(";");
    m_languages = new String[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      m_languages[i - 1] = args[i];
      m_languageIndexMapping.put(args[i], new Integer(i - 1));
    }
  }

  private boolean parseCsvLine(String line) {
    String[] args = line.split(";");
    if (args.length < m_languages.length + 1) if (m_texts.get(args[0]) != null) {
      System.err.println("Doubled translated word: " + args[0]);
      return false;
    }
    for (String language : m_languages) {
      int index = getLanguageIndex(language) + 1;
      if (index < args.length) {
        add(args[0], language, args[index]);
      }
    }
    return true;
  }

  private void cleanUp() {
    m_texts.clear();
    m_languages = null;
    m_languageIndexMapping.clear();
  }

  private int getLanguageIndex(String language) {
    return m_languageIndexMapping.get(language).intValue();
  }

}
