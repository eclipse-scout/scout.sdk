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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class ImportValidator implements IImportValidator {
  private final Map<String/* simpleName */, String/* packageName */> m_initialImports;
  private final Map<String/* simpleName */, String/* packageName */> m_newImports;

  private final String m_packageName; // can be null

  private static final Pattern EXT_REGEX = Pattern.compile("^\\+.*$");
  private static final Pattern EXT_REPL_REGEX = Pattern.compile("^\\+");
  private static final Pattern PLAIN_REPL_REGEX = Pattern.compile("^[\\[\\+]*");
  private static final String EXT_PREFIX = "? extends ";

  public ImportValidator(Map<String/* simpleName */, String/* packageName */> initialImports, String packageName) {
    if (initialImports == null) {
      m_initialImports = Collections.emptyMap();
    }
    else {
      m_initialImports = Collections.unmodifiableMap(initialImports);
    }
    m_newImports = new HashMap<String, String>();
    m_packageName = packageName;
  }

  public ImportValidator() {
    this((String) null);
  }

  public ImportValidator(String packageName) {
    this(new HashMap<String, String>(0), packageName);
  }

  public ImportValidator(ICompilationUnit icu) {
    this(getUsedImportsFromIcu(icu), getPackageFromIcu(icu));
  }

  protected static String getPackageFromIcu(ICompilationUnit icu) {
    String pck = null;
    try {
      IPackageDeclaration[] packageDeclarations = icu.getPackageDeclarations();
      if (packageDeclarations != null && packageDeclarations.length > 0) {
        pck = packageDeclarations[0].getElementName();
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get package of compilation unit '" + icu.getElementName() + "'!", e);
    }
    return pck;
  }

  protected static Map<String, String> getUsedImportsFromIcu(ICompilationUnit icu) {
    Map<String, String> usedImps = new HashMap<String, String>();
    collectExistingImportsFromIcu(icu, usedImps);
    collectTypesInPackage(TypeUtility.getPackage(icu), usedImps);
    return usedImps;
  }

  protected static void collectExistingImportsFromIcu(ICompilationUnit icu, Map<String, String> usedImps) {
    try {
      IImportDeclaration[] imports = icu.getImports();
      if (imports.length > 0) {
        Map<String, String> importsFromIcu = new HashMap<String, String>(imports.length);
        for (IImportDeclaration imp : imports) {
          String fqImp = imp.getElementName();
          String packageName = Signature.getQualifier(fqImp);
          String simpleName = Signature.getSimpleName(fqImp);
          importsFromIcu.put(simpleName, packageName);
        }
        usedImps.putAll(importsFromIcu);
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not collect imports of compilation unit '" + icu.getElementName() + "'!", e);
    }
  }

  protected static void collectTypesInPackage(IPackageFragment pck, Map<String, String> usedImps) {
    try {
      for (ICompilationUnit icu : pck.getCompilationUnits()) {
        for (IType t : icu.getTypes()) {
          usedImps.put(t.getElementName(), pck.getElementName());
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not collect all used typenames in package '" + pck.getElementName() + "'!", e);
    }
  }

  @Override
  public String getTypeName(String singleTypeSignature) {

    // handle primitive and void types
    int kind = SignatureUtility.getTypeSignatureKind(singleTypeSignature);
    if (kind == Signature.BASE_TYPE_SIGNATURE) {
      return Signature.getSignatureSimpleName(singleTypeSignature);
    }

    // handle unresolved signatures
    if (SignatureUtility.isUnresolved(singleTypeSignature)) {
      return Signature.toString(singleTypeSignature);
    }

    StringBuilder result = new StringBuilder(singleTypeSignature.length() + EXT_PREFIX.length());

    // handle extends prefix
    if (EXT_REGEX.matcher(singleTypeSignature).matches()) {
      result.append(EXT_PREFIX);
      singleTypeSignature = EXT_REPL_REGEX.matcher(singleTypeSignature).replaceAll("");
    }

    // handle normal signature
    String pckName = Signature.getSignatureQualifier(singleTypeSignature);
    String simpleName = Signature.getSignatureSimpleName(singleTypeSignature);
    String plainSimpleName = Signature.getSignatureSimpleName(PLAIN_REPL_REGEX.matcher(singleTypeSignature).replaceAll(""));

    if (!StringUtility.hasText(pckName)) {
      return simpleName;
    }

    if (isAlreadyUsed(pckName, plainSimpleName)) {
      result.append(pckName).append(".");
    }
    else {
      m_newImports.put(plainSimpleName, pckName);
    }

    return result.append(simpleName).toString();
  }

  protected String findUsedPackageName(String simpleName) {
    if (m_initialImports.containsKey(simpleName)) {
      return m_initialImports.get(simpleName);
    }
    else {
      return m_newImports.get(simpleName);
    }
  }

  protected boolean isAlreadyUsed(String packageName, String simpleName) {
    String usedPackageName = findUsedPackageName(simpleName);
    return usedPackageName != null && !usedPackageName.equals(packageName);
  }

  @Override
  public void addImport(String fqn) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    m_newImports.put(simpleName, packageName);
  }

  @Override
  public Set<String> getImportsToCreate() {
    TreeSet<String> importsToCreate = new TreeSet<String>();
    for (Entry<String, String> e : m_newImports.entrySet()) {
      String pck = e.getValue();
      String simpleName = e.getKey();
      // don't create imports for java.lang.* classes and classes in the same package.
      if (!"java.lang".equals(pck) && !pck.equals(getPackageName())) {
        importsToCreate.add(pck + "." + simpleName);
      }
    }
    return importsToCreate;
  }

  public String getPackageName() {
    return m_packageName;
  }
}
