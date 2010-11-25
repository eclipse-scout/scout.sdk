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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class TestManifest {

  public static void main(String[] args) throws FileNotFoundException, IOException {
    Manifest mf = new Manifest();
    mf.read(new FileReader("META-INF/MANIFEST.MF"));
    for (Map.Entry<String, String> e : mf.getAttributes().entrySet()) {
      System.out.println("" + e.getKey() + ": '" + e.getValue() + "'");
    }
    mf.write(new FileWriter("TestManifest-output.txt"));
    System.out.println("Done");
  }
}
