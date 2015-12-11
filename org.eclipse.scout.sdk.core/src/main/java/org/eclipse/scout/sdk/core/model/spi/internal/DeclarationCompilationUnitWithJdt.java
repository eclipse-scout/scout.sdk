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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
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
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class DeclarationCompilationUnitWithJdt extends AbstractJavaElementWithJdt<ICompilationUnit> implements CompilationUnitSpi {
  private final CompilationUnitDeclaration m_astNode;
  private PackageSpi m_package;
  private String m_fileName;
  private TypeSpi m_mainType;
  private List<DeclarationTypeWithJdt> m_types;
  private List<DeclarationImportWithJdt> m_imports;
  private ISourceRange m_source;
  private ISourceRange m_javaDocSource;

  DeclarationCompilationUnitWithJdt(JavaEnvironmentWithJdt env, CompilationUnitDeclaration astNode) {
    super(env);
    m_astNode = Validate.notNull(astNode);
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    for (DeclarationTypeWithJdt oldType : getTypes()) {
      TypeSpi newType = (TypeSpi) oldType.internalFindNewElement(newEnv);
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
    if (m_package == null) {
      ImportReference currentPackage = m_astNode.currentPackage;
      if (currentPackage != null) {
        char[][] importName = currentPackage.getImportName();
        if (importName != null && importName.length > 0) {
          m_package = m_env.createPackage(CharOperation.toString(importName));
        }
      }
      if (m_package == null) {
        m_package = m_env.createDefaultPackage();
      }
    }
    return m_package;
  }

  @Override
  public TypeSpi findTypeBySimpleName(String simpleName) {
    TypeSpi result = findTypeBySimpleNameInternal(simpleName, m_astNode.scope, m_env);
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
    TypeSpi result = findTypeBySimpleNameInternal(simpleName, stb.scope, m_env);
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

  protected TypeSpi findTypeBySimpleNameInternal(String simpleName, Scope scopeForTypeLookup, JavaEnvironmentWithJdt env) {
    TypeBinding type = scopeForTypeLookup.getType(simpleName.toCharArray());
    if (type instanceof MissingTypeBinding || type instanceof ProblemReferenceBinding) {
      return null;
    }
    return SpiWithJdtUtils.bindingToType(env, type);
  }

  @Override
  public String getElementName() {
    if (m_fileName == null) {
      char[] array = m_astNode.getFileName();
      int i = Math.max(CharOperation.lastIndexOf('/', array), CharOperation.lastIndexOf('\\', array));
      m_fileName = i >= 0 ? new String(array, i + 1, array.length - i - 1) : new String(array);
    }
    return m_fileName;
  }

  @Override
  public TypeSpi getMainType() {
    if (m_mainType == null) {
      String mainTypeName = new String(m_astNode.getMainTypeName());
      for (TypeSpi t : getTypes()) {
        if (mainTypeName.equals(t.getElementName())) {
          m_mainType = t;
          break;
        }
      }
    }
    return m_mainType;
  }

  @Override
  public List<DeclarationTypeWithJdt> getTypes() {
    if (m_types == null) {
      TypeDeclaration[] types = m_astNode.types;

      if (types == null || types.length < 1) {
        m_types = new ArrayList<>(0);
      }
      else {
        List<DeclarationTypeWithJdt> result = new ArrayList<>(types.length);
        for (TypeDeclaration td : types) {
          result.add(m_env.createDeclarationType(this, null, td));
        }
        m_types = result;
      }
    }
    return m_types;
  }

  @Override
  public List<DeclarationImportWithJdt> getImports() {
    if (m_imports == null) {
      ImportReference[] imports = m_astNode.imports;
      if (imports == null || imports.length < 1) {
        m_imports = new ArrayList<>(0);
      }
      else {
        List<DeclarationImportWithJdt> result = new ArrayList<>(imports.length);
        for (ImportReference imp : imports) {
          DeclarationImportWithJdt importDeclaration = m_env.createDeclarationImport(this, imp);
          result.add(importDeclaration);
        }
        m_imports = result;
      }
    }
    return m_imports;
  }

  @Override
  public ISourceRange getSource() {
    if (m_source == null) {
      m_source = m_env.getSource(this, m_astNode.sourceStart, m_astNode.sourceEnd);
    }
    return m_source;
  }

  @Override
  public ISourceRange getJavaDoc() {
    if (m_javaDocSource == null) {
      Javadoc doc = m_astNode.javadoc;
      if (doc != null) {
        m_javaDocSource = m_env.getSource(this, doc.sourceStart, doc.sourceEnd);
      }
      else {
        m_javaDocSource = ISourceRange.NO_SOURCE;
      }
    }
    return m_javaDocSource;
  }
}
