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
package org.eclipse.scout.sdk.core.importvalidator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.scout.sdk.core.model.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.IImportDeclaration;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

public class ImportValidator implements IImportValidator {

  private final String m_packageName; // can be null (default package)
  private final ILookupEnvironment m_lookup; // can be null
  private final Map<String/* simpleName */, String/* packageName */> m_initialImports;
  private final Map<String/* simpleName */, String/* packageName */> m_newImports;
  private final Map<String/* simpleName */, Boolean /* exists in own package*/> m_existsInOwnPackageCache;

  public ImportValidator() {
    this((String) null);
  }

  public ImportValidator(String pckName) {
    this(pckName, null);
  }

  public ImportValidator(String packageName, ILookupEnvironment lookupEnv) {
    m_lookup = lookupEnv;
    m_packageName = packageName;
    m_initialImports = new HashMap<>();
    m_newImports = new HashMap<>();
    m_existsInOwnPackageCache = new HashMap<>();
  }

  public ImportValidator(ICompilationUnit icu) {
    m_lookup = Validate.notNull(icu).getLookupEnvironment();
    m_packageName = icu.getPackage().getName();
    m_initialImports = getExistingImportsFromIcu(icu);
    m_newImports = new HashMap<>();
    m_existsInOwnPackageCache = new HashMap<>();
  }

  protected static Map<String, String> getExistingImportsFromIcu(ICompilationUnit icu) {
    Map<String, IImportDeclaration> imports = icu.getImports();
    Map<String, String> usedImps = new HashMap<>(imports.size());
    for (IImportDeclaration imp : imports.values()) {
      String packageName = imp.getQualifier();
      String simpleName = imp.getSimpleName();
      usedImps.put(simpleName, packageName);
    }
    return usedImps;
  }

  @Override
  public String getTypeName(String signature) {

    // handle primitive and void types
    int kind = Signature.getTypeSignatureKind(signature);
    if (kind == ISignatureConstants.BASE_TYPE_SIGNATURE) {
      return Signature.getSignatureSimpleName(signature);
    }

    // handle unresolved signatures
    if (SignatureUtils.isUnresolved(signature)) {
      return Signature.toString(signature);
    }

    // handle normal signature
    StringBuilder result = new StringBuilder(signature.length());

    String sigWithoutDollar = signature.replace(ISignatureConstants.C_DOLLAR, ISignatureConstants.C_DOT); // e.g. a.b.c.MyClass$InnerClass$SecondInner -> a.b.c.MyClass.InnerClass.SecondInner
    String fullQualification = Signature.getSignatureQualifier(sigWithoutDollar); // e.g. a.b.c.MyClass$InnerClass$SecondInner -> a.b.c.MyClass.InnerClass
    String packageName = Signature.getSignatureQualifier(signature);
    String lastSegment = Signature.getSignatureSimpleName(sigWithoutDollar); // e.g. a.b.c.MyClass$InnerClass$SecondInner -> SecondInner
    if (StringUtils.isBlank(fullQualification)) {
      return lastSegment;
    }

    if (useQualifiedName(fullQualification, lastSegment, packageName)) {
      result.append(fullQualification).append('.');
    }
    else {
      m_newImports.put(lastSegment, fullQualification);
    }

    return result.append(lastSegment).toString();
  }

  protected String findUsedPackageName(String simpleName) {
    if (m_initialImports.containsKey(simpleName)) {
      return m_initialImports.get(simpleName);
    }
    return m_newImports.get(simpleName);
  }

  protected boolean useQualifiedName(String fullQualification, String lastSegment, String packageName) {
    String usedPackageName = findUsedPackageName(lastSegment);
    if (Objects.equals(fullQualification, usedPackageName)) {
      // already used with same package -> re-use
      return false;
    }
    if (usedPackageName != null) {
      return true; // already used with different package -> qualify
    }

    if (m_lookup == null) {
      return false;
    }

    if (Objects.equals(m_packageName, packageName)) {
      return false;
    }

    // check if used in own package
    Boolean existsInOwnPackage = m_existsInOwnPackageCache.get(lastSegment);
    if (existsInOwnPackage == null) {
      // load to cache
      String nameInOwnPackage = new StringBuilder(m_packageName.length() + 1 + lastSegment.length()).append(m_packageName).append('.').append(lastSegment).toString();
      existsInOwnPackage = Boolean.valueOf(m_lookup.existsType(nameInOwnPackage));
      m_existsInOwnPackageCache.put(lastSegment, existsInOwnPackage);
    }

    return existsInOwnPackage;
  }

  @Override
  public void addImport(String fqn) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    m_newImports.put(simpleName, packageName);
  }

  @Override
  public Set<String> getImportsToCreate() {
    TreeSet<String> importsToCreate = new TreeSet<>();
    for (Entry<String, String> e : m_newImports.entrySet()) {
      String pck = e.getValue();
      String simpleName = e.getKey();
      // don't create imports for java.lang.* classes and classes in the same package.
      if (!"java.lang".equals(pck) && !pck.equals(getPackageName())) {
        importsToCreate.add(pck + '.' + simpleName);
      }
    }
    return importsToCreate;
  }

  public String getPackageName() {
    return m_packageName;
  }
}
