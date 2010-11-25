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
package org.eclipse.scout.nls.sdk.model.workspace.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.nls.sdk.NlsCore;

public class PropertyFileReader {

  HashMap<String, String> m_parsedEntries = new HashMap<String, String>();

  private final String m_fileName;

  /**
   * creates an accessible to the property file
   * 
   * @param stram
   *          the input stream of a property file
   */
  public PropertyFileReader(InputStream stram, String fileName) {
    m_fileName = fileName;
    try {
      parsePropertyFile(stram);
    }
    catch (CoreException e) {
      // TODO Auto-generated catch block
      NlsCore.logWarning(e);
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      NlsCore.logWarning(e);
    }
  }

  /**
   * @param file
   */
  public PropertyFileReader(IFile file) {
    m_fileName = file.getName();
    try {
      if (file.exists()) {
        parsePropertyFile(file.getContents());
      }
    }
    catch (Exception e) {
      NlsCore.logWarning(e);
      // NlsCore.logError("could not parse manifest file: " + file.getProjectRelativePath(), e);
    }
  }

  private void parsePropertyFile(InputStream stram) throws CoreException, IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stram));
    String line = reader.readLine();
    while (line != null) {
      if (line.startsWith("#") || line.matches("\\s*")) {
        // skip comments
        line = reader.readLine();
        continue;
      }
      // split
      String[] args = line.split("=", 2);
      if (args.length != 2) {
        NlsCore.logWarning("could not parse property file entry '" + line + "' of file '" + m_fileName + "'.");
        // NlsCore.logWarning("could not parse manifest entry: " + line);
        line = reader.readLine();
      }
      else {
        String key = args[0].trim();
        String value = args[1].trim();
        // TODO make a manifest arg hierarchically
        if (value.indexOf(";") > 0) {
          value = value.substring(0, value.indexOf(";"));
        }
        line = reader.readLine();
        while (line != null && line.startsWith(" ")) {
          value = value + line.trim();
          line = reader.readLine();
        }
        m_parsedEntries.put(key, value);
      }
    }
  }

  public String getAttribute(String property) {
    String value = m_parsedEntries.get(property);
    return value;
  }
}
