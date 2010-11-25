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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.scout.nls.sdk.NlsCore;

public class FindDoubleEntries {

  private String m_fileName = "D:\\temp\\nl\\nwuMessages.properties";

  public FindDoubleEntries() {

  }

  private void doIt() throws IOException {
    HashMap<String, String> checkMap = new HashMap<String, String>();
    File file = new File(m_fileName);
    if (!file.exists()) {
      System.err.println("File: " + m_fileName + " not found!");
      return;
    }
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = reader.readLine().trim();
    while (line != null) {
      if (!line.startsWith("#") && line.length() > 1) {
        String[] args = line.trim().split("=");
        if (args.length != 2) {
          System.err.println("could not parse line: " + line);
        }
        else {
          if (checkMap.get(args[0]) != null) {
            System.out.println("!!! doubled entry: " + args[0]);
          }
          else {
            checkMap.put(args[0], args[1]);
          }
        }
      }
      line = reader.readLine();
    }
  }

  public static void main(String[] args) {
    try {
      new FindDoubleEntries().doIt();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      NlsCore.logWarning(e);
    }
  }

}
