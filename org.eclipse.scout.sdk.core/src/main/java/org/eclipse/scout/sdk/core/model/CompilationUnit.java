/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 *
 */
public class CompilationUnit implements ICompilationUnit {

  private final CompilationUnitDeclaration m_ast;
  private final char[] m_id;
  private final int m_hash;
  private final ILookupEnvironment m_env;

  private IPackage m_package;
  private IType m_mainType;
  private ListOrderedSet<IType> m_types;
  private ListOrderedMap<String, IImportDeclaration> m_imports;

  public CompilationUnit(CompilationUnitDeclaration ast, ILookupEnvironment env) {
    m_env = env;
    m_ast = Validate.notNull(ast);
    m_id = computeUniqueKey(ast);
    m_hash = Arrays.hashCode(m_id);
  }

  protected static char[] computeUniqueKey(CompilationUnitDeclaration ast) {
    StringBuilder sig = new StringBuilder();
    ImportReference currentPackage = ast.currentPackage;
    if (currentPackage != null) {
      char[][] importName = currentPackage.getImportName();
      if (importName != null && importName.length > 0) {
        for (char[] part : importName) {
          sig.append(part);
          sig.append('.');
        }
      }
    }
    char[] mainTypeName = ast.getMainTypeName();
    if (mainTypeName != null) {
      sig.append(mainTypeName);
    }

    int sigLength = sig.length();
    char[] uniqueKey = new char[sigLength];
    sig.getChars(0, sigLength, uniqueKey, 0);
    return uniqueKey;
  }

  @Override
  public IPackage getPackage() {
    if (m_package == null) {
      ImportReference currentPackage = m_ast.currentPackage;
      if (currentPackage != null) {
        m_package = new Package(CharOperation.toString(currentPackage.getImportName()));
      }
    }
    return m_package;
  }

  @Override
  public IType findTypeBySimpleName(String simpleName) {
    IType result = JavaModelUtils.findTypeBySimpleName(simpleName, m_ast.scope, m_env);
    if (result != null) {
      return result;
    }

    // check inner types recursive
    for (SourceTypeBinding stb : m_ast.scope.topLevelTypes) {
      result = findTypeInSourceTypeBinding(stb, simpleName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private IType findTypeInSourceTypeBinding(Binding b, String simpleName) {
    if (!(b instanceof SourceTypeBinding)) {
      return null;
    }
    SourceTypeBinding stb = (SourceTypeBinding) b;
    IType result = JavaModelUtils.findTypeBySimpleName(simpleName, stb.scope, m_env);
    if (result != null) {
      return result;
    }
    for (ReferenceBinding mb : stb.memberTypes) {
      result = findTypeInSourceTypeBinding(mb, simpleName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public IType getMainType() {
    if (m_mainType == null) {
      String mainTypeName = new String(m_ast.getMainTypeName());
      for (IType t : getTypes()) {
        if (mainTypeName.equals(t.getSimpleName())) {
          m_mainType = t;
          break;
        }
      }
    }
    return m_mainType;
  }

  @Override
  public ListOrderedSet<IType> getTypes() {
    if (m_types == null) {
      TypeDeclaration[] types = m_ast.types;

      if (types == null || types.length < 1) {
        m_types = ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
      }
      else {
        List<IType> result = new ArrayList<>(types.length);
        for (TypeDeclaration td : types) {
          result.add(new DeclarationType(td, this, null, m_env));
        }
        m_types = ListOrderedSet.listOrderedSet(result);
      }
    }
    return m_types;
  }

  @Override
  public ListOrderedMap<String, IImportDeclaration> getImports() {
    if (m_imports == null) {
      ImportReference[] imports = m_ast.imports;
      if (imports == null || imports.length < 1) {
        m_imports = ListOrderedMap.listOrderedMap(new HashMap<String, IImportDeclaration>(0));
      }
      else {
        Map<String, IImportDeclaration> result = new LinkedHashMap<>(imports.length);
        for (ImportReference imp : imports) {
          ImportDeclaration importDeclaration = new ImportDeclaration(imp, this);
          result.put(importDeclaration.getSimpleName(), importDeclaration);
        }
        m_imports = ListOrderedMap.listOrderedMap(result);
      }
    }
    return m_imports;
  }

  @Override
  public int hashCode() {
    return m_hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CompilationUnit)) {
      return false;
    }
    CompilationUnit other = (CompilationUnit) obj;
    if (!Arrays.equals(m_id, other.m_id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    ModelPrinter.print(this, sb);
    return sb.toString();
  }

  @Override
  public ILookupEnvironment getLookupEnvironment() {
    return m_env;
  }

  public CompilationUnitDeclaration getAst() {
    return m_ast;
  }
}
