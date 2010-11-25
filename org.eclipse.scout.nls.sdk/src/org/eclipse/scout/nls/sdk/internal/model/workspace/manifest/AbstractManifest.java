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
package org.eclipse.scout.nls.sdk.internal.model.workspace.manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.nls.sdk.NlsCore;

public abstract class AbstractManifest {

  List<String> m_entryOrder = new LinkedList<String>();
  HashMap<String, ManifestEntry> m_parsedEntries = new HashMap<String, ManifestEntry>();

  public AbstractManifest(String symbolicBundleName, InputStream stream) {
    // NlsCore.logWarning("begin parsing manifest: "+symbolicBundleName);
    try {
      parseManifest(stream);
    }
    catch (CoreException e) {
      NlsCore.logError("could not parse manifest of bundle " + symbolicBundleName);
      NlsCore.logWarning(e);
    }
    catch (IOException e) {
      NlsCore.logError("could not parse manifest of bundle " + symbolicBundleName);
    }
  }

  protected void parseManifest(InputStream input) throws CoreException, IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    String line = reader.readLine();
    while (line != null) {
      // skip comment
      if (line.startsWith("#")) {
        line = reader.readLine();
        continue;
      }
      // skip empty lines
      if (line.equals("")) {
        line = reader.readLine();
        continue;
      }
      // split
      String[] args = line.split(": ", 2);
      if (args.length != 2) {
        System.err.println("could not parse manifest entry: " + line);
        // NlsCore.logWarning("could not parse manifest entry: " + line);
        line = reader.readLine();
      }
      else {
        String key = args[0].trim();
        String value = args[1].trim();
        line = reader.readLine();
        while (line != null && line.startsWith(" ")) {
          value = value + line.trim();
          line = reader.readLine();
        }

        ManifestEntry entry = new ManifestEntry(key);
        entry.parseElements(value);
        m_parsedEntries.put(key, entry);
        m_entryOrder.add(key);
      }
    }
  }

  public void addElement(ManifestEntry newEntry) {
    ManifestEntry entry = m_parsedEntries.get(newEntry.getKey());

    if (entry != null) {
      for (ManifestElement element : newEntry.getElements()) {
        entry.addElement(element);
      }
    }
    else {
      m_parsedEntries.put(newEntry.getKey(), newEntry);
      m_entryOrder.add(newEntry.getKey());
    }
  }

  public boolean hasElement(String name) {
    return m_parsedEntries.containsKey(name);
  }

  public ManifestEntry getAttribute(String property) {
    ManifestEntry value = m_parsedEntries.get(property);
    return value;
  }

  public abstract boolean isWriteable();

  public abstract IStatus store(IProgressMonitor monitor);
}
