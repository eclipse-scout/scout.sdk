/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.createSourceRange;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.internal.CompilationUnitImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.java.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.java.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class DeclarationCompilationUnitWithEcj extends AbstractJavaElementWithEcj<ICompilationUnit> implements CompilationUnitSpi {
  private final CompilationUnitDeclaration m_astNode;
  private final FinalValue<PackageSpi> m_package;
  private final FinalValue<String> m_fileName;
  private final FinalValue<TypeSpi> m_mainType;
  private final FinalValue<List<DeclarationTypeWithEcj>> m_types;
  private final FinalValue<List<DeclarationImportWithEcj>> m_imports;
  private final FinalValue<SourceRange> m_source;
  private final FinalValue<SourceRange> m_javaDocSource;
  private final FinalValue<Path> m_absolutePath;
  private final FinalValue<ClasspathSpi> m_containingClasspathFolder;

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
    m_absolutePath = new FinalValue<>();
    m_containingClasspathFolder = new FinalValue<>();
  }

  protected static TypeBinding findBindingBySimpleName(String simpleName, DeclarationCompilationUnitWithEcj cu) {
    var t = ((AbstractTypeWithEcj) cu.findTypeBySimpleName(simpleName));
    if (t == null) {
      return null;
    }
    return t.getInternalBinding();
  }

  protected TypeSpi findTypeBySimpleNameInternal(String simpleName, Scope scopeForTypeLookup) {
    var type = scopeForTypeLookup.getType(simpleName.toCharArray());
    if (type instanceof MissingTypeBinding || type instanceof ProblemReferenceBinding) {
      return null;
    }
    return bindingToType(javaEnvWithEcj(), type, () -> withNewElement((DeclarationCompilationUnitWithEcj x) -> findBindingBySimpleName(simpleName, x)));
  }

  @Override
  public CompilationUnitSpi internalFindNewElement() {
    return getTypes().stream()
        .map(DeclarationTypeWithEcj::internalFindNewElement)
        .filter(Objects::nonNull)
        .findFirst()
        .map(TypeSpi::getCompilationUnit)
        .orElse(null);
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
  public Path absolutePath() {
    return m_absolutePath.computeIfAbsentAndGet(this::computeAbsolutePath);
  }

  protected Path computeAbsolutePath() {
    var compilationResult = getInternalCompilationUnitDeclaration().compilationResult();
    if (compilationResult == null) {
      return null;
    }
    var fileName = compilationResult.getFileName();
    if (fileName == null) {
      return null;
    }
    return Paths.get(new String(fileName));
  }

  @Override
  public ClasspathSpi getContainingClasspathFolder() {
    return m_containingClasspathFolder.computeIfAbsentAndGet(this::computeContainingClasspathFolder);
  }

  protected ClasspathSpi computeContainingClasspathFolder() {
    var myPath = absolutePath();
    if (myPath == null) {
      return null;
    }
    return getJavaEnvironment().getClasspath().stream()
        .filter(ClasspathSpi::isDirectory)
        .filter(cp -> myPath.startsWith(cp.getPath()))
        .findFirst()
        .orElse(null);
  }

  @Override
  public PackageSpi getPackage() {
    return m_package.computeIfAbsentAndGet(() -> {
      var currentPackage = m_astNode.currentPackage;
      if (currentPackage != null) {
        var importName = currentPackage.getImportName();
        if (importName != null && importName.length > 0) {
          return javaEnvWithEcj().createPackage(CharOperation.toString(importName));
        }
      }
      return javaEnvWithEcj().createDefaultPackage();
    });
  }

  @Override
  public TypeSpi findTypeBySimpleName(String simpleName) {
    var result = findTypeBySimpleNameInternal(simpleName, m_astNode.scope);
    if (result != null) {
      return result;
    }

    // check inner types recursive
    for (var stb : m_astNode.scope.topLevelTypes) {
      result = findTypeInSourceTypeBindingRec(stb, simpleName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  protected TypeSpi findTypeInSourceTypeBindingRec(Binding b, String simpleName) {
    if (!(b instanceof SourceTypeBinding stb)) {
      return null;
    }
    var result = findTypeBySimpleNameInternal(simpleName, stb.scope);
    if (result != null) {
      return result;
    }
    for (var mb : stb.memberTypes) {
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
      var array = m_astNode.getFileName();
      var i = Math.max(CharOperation.lastIndexOf('/', array), CharOperation.lastIndexOf('\\', array));
      if (i >= 0) {
        return new String(array, i + 1, array.length - i - 1);
      }
      return new String(array);
    });
  }

  @Override
  public TypeSpi getMainType() {
    return m_mainType.computeIfAbsentAndGet(() -> {
      var mainTypeName = new String(m_astNode.getMainTypeName());
      return getTypes().stream()
          .filter(t -> mainTypeName.equals(t.getElementName()))
          .findFirst()
          .orElse(null);
    });
  }

  @Override
  public List<DeclarationTypeWithEcj> getTypes() {
    return m_types.computeIfAbsentAndGet(() -> {
      var types = m_astNode.types;
      if (types == null || types.length < 1) {
        return emptyList();
      }
      var env = javaEnvWithEcj();
      return Arrays.stream(types)
          .map(td -> env.createDeclarationType(this, null, td))
          .collect(toList());
    });
  }

  @Override
  public List<DeclarationImportWithEcj> getImports() {
    return m_imports.computeIfAbsentAndGet(() -> {
      var imports = m_astNode.imports;
      if (imports == null || imports.length < 1) {
        return emptyList();
      }

      var env = javaEnvWithEcj();
      return Arrays.stream(imports)
          .map(imp -> env.createDeclarationImport(this, imp))
          .collect(toList());
    });
  }

  @Override
  public SourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> createSourceRange(m_astNode, this, javaEnvWithEcj()));
  }

  @Override
  public SourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      var doc = m_astNode.javadoc;
      if (doc != null) {
        return createSourceRange(doc, this, javaEnvWithEcj());
      }
      if (m_astNode.currentPackage != null && m_astNode.currentPackage.declarationSourceStart > 0) {
        return javaEnvWithEcj().getSource(this, 0, m_astNode.currentPackage.declarationSourceStart - 1);
      }
      return null;
    });
  }
}
