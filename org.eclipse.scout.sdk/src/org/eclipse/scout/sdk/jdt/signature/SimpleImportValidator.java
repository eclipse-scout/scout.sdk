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
package org.eclipse.scout.sdk.jdt.signature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.jdt.core.Signature;

public class SimpleImportValidator implements IImportValidator {
  private HashMap<String/* simpleName */, String/* packageName */> m_newImports;

  public SimpleImportValidator() {
    m_newImports = new HashMap<String, String>();
  }

  @Override
  public String getSimpleTypeRef(String singleTypeSignature) {
    String prefix = "";
    if (singleTypeSignature.matches("^\\+.*$")) {
      prefix = "? extends ";
      singleTypeSignature = singleTypeSignature.replaceAll("^\\+", "");
    }
    if (singleTypeSignature.charAt(0) == Signature.C_UNRESOLVED) {
      return Signature.getSignatureSimpleName(singleTypeSignature);
    }
    else {
      String pckName = Signature.getSignatureQualifier(singleTypeSignature);
      String simpleName = Signature.getSignatureSimpleName(singleTypeSignature);
      String plainSimpleName = Signature.getSignatureSimpleName(singleTypeSignature.replaceAll("^[\\[\\+]*", ""));
      if (isAlreadyUsed(pckName, plainSimpleName)) {
        return prefix + pckName + "." + simpleName;
      }
      else {
        m_newImports.put(plainSimpleName, pckName);
        System.out.println(pckName);
        return prefix + simpleName;
      }
    }
  }

  protected boolean isAlreadyUsed(String packageName, String simpleName) {
    String usedPackageName = m_newImports.get(simpleName);
    if (usedPackageName != null) {
      if (!usedPackageName.equals(packageName)) {
        // fully quallified
        return true;
      }
    }
    return false;
  }

  @Override
  public void addImport(String fqn) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    System.out.println(packageName);
    m_newImports.put(simpleName, packageName);
  }

  @Override
  public String[] getImportsToCreate() {
    ArrayList<String> list = new ArrayList<String>();
    for (Entry<String, String> e : m_newImports.entrySet()) {
      if (!e.getValue().equals("java.lang")) {
        list.add(e.getValue() + "." + e.getKey());
      }
    }
    return list.toArray(new String[list.size()]);
  }
}
