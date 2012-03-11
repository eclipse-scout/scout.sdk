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
package org.eclipse.scout.sdk.util.signature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Signature;

public class SimpleImportValidator implements IImportValidator {
  private HashMap<String/* simpleName */, String/* packageName */> m_newImports;
  private String m_packageName;

  private final static Pattern EXT_REGEX = Pattern.compile("^\\+.*$");
  private final static Pattern EXT_REPL_REGEX = Pattern.compile("^\\+");
  private final static Pattern PLAIN_REPL_REGEX = Pattern.compile("^[\\[\\+]*");

  public SimpleImportValidator() {
    this(null);
  }

  public SimpleImportValidator(String packageName) {
    m_newImports = new HashMap<String, String>();
    m_packageName = packageName;
  }

  @Override
  public String getTypeName(String singleTypeSignature) {
    String prefix = "";
    if (EXT_REGEX.matcher(singleTypeSignature).matches()) {
      prefix = "? extends ";
      singleTypeSignature = EXT_REPL_REGEX.matcher(singleTypeSignature).replaceAll("");
    }
    if (singleTypeSignature.charAt(0) == Signature.C_UNRESOLVED) {
      return Signature.getSignatureSimpleName(singleTypeSignature);
    }
    else {
      String pckName = Signature.getSignatureQualifier(singleTypeSignature);
      String simpleName = Signature.getSignatureSimpleName(singleTypeSignature);
      String plainSimpleName = Signature.getSignatureSimpleName(PLAIN_REPL_REGEX.matcher(singleTypeSignature).replaceAll(""));

      if (isAlreadyUsed(pckName, plainSimpleName)) {
        return prefix + pckName + "." + simpleName;
      }
      else {
        m_newImports.put(plainSimpleName, pckName);
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

  protected boolean isSamePackage(String packageName) {
    if (m_packageName != null) {
      return m_packageName.equals(packageName);
    }
    return false;
  }

  @Override
  public void addImport(String fqn) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    m_newImports.put(simpleName, packageName);
  }

  @Override
  public String[] getImportsToCreate() {
    ArrayList<String> list = new ArrayList<String>();
    for (Entry<String, String> e : m_newImports.entrySet()) {
      if (e.getValue().equals("java.lang")) {
        continue;
      }
      if (isSamePackage(e.getValue())) {
        continue;
      }
      list.add(e.getValue() + "." + e.getKey());
    }
    return list.toArray(new String[list.size()]);
  }
}
