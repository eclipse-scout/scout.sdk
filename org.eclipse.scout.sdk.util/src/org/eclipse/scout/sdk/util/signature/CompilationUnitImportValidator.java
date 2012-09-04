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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;

public class CompilationUnitImportValidator implements IImportValidator {

  private final ICompilationUnit m_icu;
  private final Map<String/* simpleName */, String/* packageName */> m_icuImports;
  private HashMap<String/* simpleName */, String/* packageName */> m_newImports;

  public CompilationUnitImportValidator(ICompilationUnit icu) {
    m_icu = icu;
    HashMap<String, String> usedImps = new HashMap<String, String>();
    try {
      for (IImportDeclaration imp : m_icu.getImports()) {
        String fqImp = imp.getElementName();
        String packageName = Signature.getQualifier(fqImp);
        String simpleName = Signature.getSimpleName(fqImp);
        usedImps.put(simpleName, packageName);
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not collect imports of compilation unit '" + icu.getElementName() + "'!", e);
    }
    m_icuImports = Collections.unmodifiableMap(usedImps);
    m_newImports = new HashMap<String, String>();
  }

  @Override
  public void addImport(String fqn) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    m_newImports.put(simpleName, packageName);
  }

  private String findUsedPackageName(String simpleName) {
    if (m_icuImports.containsKey(simpleName)) {
      return m_icuImports.get(simpleName);
    }
    else {
      return m_newImports.get(simpleName);
    }
  }

  @Override
  public String getTypeName(String singleTypeSignature) {
    String prefix = "";
    if (singleTypeSignature.matches("^\\+.*$")) {
      prefix = "? extends ";
      singleTypeSignature = singleTypeSignature.replaceAll("^\\+", "");
    }
    String pckName = Signature.getSignatureQualifier(singleTypeSignature);
    String simpleName = Signature.getSignatureSimpleName(singleTypeSignature);
    String plainSimpleName = Signature.getSignatureSimpleName(singleTypeSignature.replaceAll("^[\\[\\+]*", ""));
    if (!StringUtility.isNullOrEmpty(pckName)) {
      String usedPackageName = findUsedPackageName(plainSimpleName);
      if (usedPackageName != null) {
        if (!usedPackageName.equals(pckName)) {
          // fully quallified
          return prefix + pckName + "." + simpleName;
        }
        else {
          return prefix + simpleName;
        }
      }
      else {
        m_newImports.put(plainSimpleName, pckName);
        return prefix + simpleName;
      }
    }
    else {
      return simpleName;
    }
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
