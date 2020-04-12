/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.CompilationUnitImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 *
 */
public class DeclarationCompilationUnitWithEcj extends AbstractJavaElementWithEcj<ICompilationUnit> implements CompilationUnitSpi {
  private final CompilationUnitDeclaration m_astNode;
  private final FinalValue<PackageSpi> m_package;
  private final FinalValue<String> m_fileName;
  private final FinalValue<TypeSpi> m_mainType;
  private final FinalValue<List<DeclarationTypeWithEcj>> m_types;
  private final FinalValue<List<DeclarationImportWithEcj>> m_imports;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<ISourceRange> m_javaDocSource;

  protected DeclarationCompilationUnitWithEcj(AbstractJavaEnvironment env, CompilationUnitDeclaration astNode) {
    super(env);
    m_astNode = Ensure.notNull(astNode);
    m_package = new FinalValue<>();
    m_fileName = new FinalValue<>();
    m_mainType = new FinalValue<>();
    m_types = new FinalValue<>();
    m_imports = new FinalValue<>();
    m_source = new FinalValue<>();
    m_javaDocSource = new FinalValue<>();
  }

  protected static TypeSpi findTypeBySimpleNameInternal(String simpleName, Scope scopeForTypeLookup, JavaEnvironmentWithEcj env) {
    TypeBinding type = scopeForTypeLookup.getType(simpleName.toCharArray());
    if (type instanceof MissingTypeBinding || type instanceof ProblemReferenceBinding) {
      return null;
    }
    return SpiWithEcjUtils.bindingToType(env, type);
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    for (DeclarationTypeWithEcj oldType : getTypes()) {
      TypeSpi newType = (TypeSpi) oldType.internalFindNewElement();
      if (newType != null) {
        return newType.getCompilationUnit();
      }
    }
    return null;
  }

  @Override
  protected ICompilationUnit internalCreateApi() {
    return new CompilationUnitImplementor(this);
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  public CompilationUnitDeclaration getInternalCompilationUnitDeclaration() {
    return m_astNode;
  }

  @Override
  public PackageSpi getPackage() {
    return m_package.computeIfAbsentAndGet(() -> {
      ImportReference currentPackage = m_astNode.currentPackage;
      if (currentPackage != null) {
        char[][] importName = currentPackage.getImportName();
        if (importName != null && importName.length > 0) {
          return javaEnvWithEcj().createPackage(CharOperation.toString(importName));
        }
      }
      return javaEnvWithEcj().createDefaultPackage();
    });
  }

  @Override
  public TypeSpi findTypeBySimpleName(String simpleName) {
    TypeSpi result = findTypeBySimpleNameInternal(simpleName, m_astNode.scope, javaEnvWithEcj());
    if (result != null) {
      return result;
    }

    // check inner types recursive
    for (SourceTypeBinding stb : m_astNode.scope.topLevelTypes) {
      result = findTypeInSourceTypeBindingRec(stb, simpleName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  protected TypeSpi findTypeInSourceTypeBindingRec(Binding b, String simpleName) {
    if (!(b instanceof SourceTypeBinding)) {
      return null;
    }
    SourceTypeBinding stb = (SourceTypeBinding) b;
    TypeSpi result = findTypeBySimpleNameInternal(simpleName, stb.scope, javaEnvWithEcj());
    if (result != null) {
      return result;
    }
    for (ReferenceBinding mb : stb.memberTypes) {
      result = findTypeInSourceTypeBindingRec(mb, simpleName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public String getElementName() {
    return m_fileName.computeIfAbsentAndGet(() -> {
      char[] array = m_astNode.getFileName();
      int i = Math.max(CharOperation.lastIndexOf('/', array), CharOperation.lastIndexOf('\\', array));
      if (i >= 0) {
        return new String(array, i + 1, array.length - i - 1);
      }
      return new String(array);
    });
  }

  @Override
  public TypeSpi getMainType() {
    return m_mainType.computeIfAbsentAndGet(() -> {
      String mainTypeName = new String(m_astNode.getMainTypeName());
      for (TypeSpi t : getTypes()) {
        if (mainTypeName.equals(t.getElementName())) {
          return t;
        }
      }
      return null;
    });
  }

  @Override
  public List<DeclarationTypeWithEcj> getTypes() {
    return m_types.computeIfAbsentAndGet(() -> {
      TypeDeclaration[] types = m_astNode.types;
      if (types == null || types.length < 1) {
        return emptyList();
      }
      List<DeclarationTypeWithEcj> result = new ArrayList<>(types.length);
      for (TypeDeclaration td : types) {
        result.add(javaEnvWithEcj().createDeclarationType(this, null, td));
      }
      return result;
    });
  }

  @Override
  public List<DeclarationImportWithEcj> getImports() {
    return m_imports.computeIfAbsentAndGet(() -> {
      ImportReference[] imports = m_astNode.imports;
      if (imports == null || imports.length < 1) {
        return emptyList();
      }

      List<DeclarationImportWithEcj> result = new ArrayList<>(imports.length);
      for (ImportReference imp : imports) {
        DeclarationImportWithEcj importDeclaration = javaEnvWithEcj().createDeclarationImport(this, imp);
        result.add(importDeclaration);
      }
      return result;
    });
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> javaEnvWithEcj().getSource(this, m_astNode.sourceStart, m_astNode.sourceEnd));
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      Javadoc doc = m_astNode.javadoc;
      if (doc != null) {
        return javaEnvWithEcj().getSource(this, doc.sourceStart, doc.sourceEnd);
      }
      else if (m_astNode.currentPackage != null && m_astNode.currentPackage.declarationSourceStart > 0) {
        return javaEnvWithEcj().getSource(this, 0, m_astNode.currentPackage.declarationSourceStart - 1);
      }
      else {
        return null;
      }
    });
  }
}
