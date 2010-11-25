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
package org.eclipse.scout.sdk.pde;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * <h4>Manifest</h4> makes all manifest entries available for read and write
 * inside osgi/eclipse preferrably use {@link Manifest} , Ivan Motsch
 */
public class RawManifest {

  public static final String MAINFEST_MF_PATH = "META-INF/MANIFEST.MF";
  public static final String SCOUT_PROJECT = "Scout-Project";
  public static final String SCOUT_PROJECT_ALIAS = "Scout-Project-Alias";

  public static final String SCOUT_BUNDLE_TYPE_CLIENT = "client";
  public static final String SCOUT_BUNDLE_TYPE_SHARED = "shared";
  public static final String SCOUT_BUNDLE_TYPE_SERVER = "server";
  /**
   * known types client/shared/server
   */
  public static final String SCOUT_BUNDLE_TYPE = "Scout-Bundle-Type";

  private ArrayList<String> m_originalKeyList = new ArrayList<String>();
  private HashMap<String, String> m_entryMap = new HashMap<String, String>();

  public RawManifest() {
  }

  public String getAttribute(String key) {
    String value = m_entryMap.get(key);
    return value;
  }

  public void setAttribute(String key, String value) {
    m_entryMap.put(key, value);
    if (!m_originalKeyList.contains(key)) {
      m_originalKeyList.add(key);
    }
  }

  public void removeAttribute(String key) {
    m_entryMap.remove(key);
    m_originalKeyList.remove(key);
  }

  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(m_entryMap);
  }

  public void read(InputStream in) throws IOException {
    read(new InputStreamReader(in));
  }

  public void read(Reader r) throws IOException {
    BufferedReader reader = new BufferedReader(r);
    String line = reader.readLine();
    while (line != null) {
      if (line.length() > 0) {
        // split
        String[] args = line.split(":", 2);
        if (args.length != 2) {
          throw new IOException("could not parse manifest entry: " + line);
        }
        else {
          String key = args[0].trim();
          String value = args[1].trim();
          line = reader.readLine();
          while (line != null && line.startsWith(" ")) {
            value = value + line.trim();
            line = reader.readLine();
          }
          if (!m_originalKeyList.contains(key)) {
            m_originalKeyList.add(key);
          }
          m_entryMap.put(key, value);
        }
      }
      else {
        line = reader.readLine();
      }
    }
  }

  public void write(OutputStream out) throws IOException {
    write(new OutputStreamWriter(out));
  }

  public void write(Writer w) throws IOException {
    StringBuilder b = new StringBuilder();
    HashSet<String> remainingKeys = new HashSet<String>(m_entryMap.keySet());
    for (String key : m_originalKeyList) {
      writeEntry(b, key, m_entryMap.get(key));
      remainingKeys.remove(key);
    }
    // remaining (should be empty)
    for (String key : remainingKeys) {
      writeEntry(b, key, m_entryMap.get(key));
    }
    try {
      w.write(b.toString());
    }
    finally {
      w.close();
    }
  }

  private void writeEntry(StringBuilder b, String key, String value) {
    b.append(key);
    b.append(": ");
    if (value != null) {
      String s = value.replaceAll(",", ",\n ");
      b.append(s);
    }
    b.append("\n");
  }

}
