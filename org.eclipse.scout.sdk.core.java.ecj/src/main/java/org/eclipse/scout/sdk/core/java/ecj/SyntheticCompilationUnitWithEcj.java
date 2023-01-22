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
import static java.util.Collections.singletonList;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.internal.CompilationUnitImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.java.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.java.model.spi.ImportSpi;
import org.eclipse.scout.sdk.core.java.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class SyntheticCompilationUnitWithEcj extends AbstractJavaElementWithEcj<ICompilationUnit> implements CompilationUnitSpi {
  private final TypeSpi m_mainType;

  protected SyntheticCompilationUnitWithEcj(AbstractJavaEnvironment env, TypeSpi mainType) {
    super(env);
    m_mainType = mainType;
  }

  @Override
  public CompilationUnitSpi internalFindNewElement() {
    var newType = getJavaEnvironment().findType(m_mainType.getName());
    if (newType == null) {
      return null;
    }
    return newType.getCompilationUnit();
  }

  @Override
  public Path absolutePath() {
    return null;
  }

  @Override
  public ClasspathSpi getContainingClasspathFolder() {
    return null;
  }

  @Override
  protected ICompilationUnit internalCreateApi() {
    return new CompilationUnitImplementor(this);
  }

  @Override
  public boolean isSynthetic() {
    return true;
  }

  @Override
  public PackageSpi getPackage() {
    return m_mainType.getPackage();
  }

  @Override
  public TypeSpi findTypeBySimpleName(String simpleName) {
    throw new UnsupportedOperationException("not supported in synthetic binary compilation unit");
  }

  @Override
  public String getElementName() {
    return m_mainType.getElementName() + JavaTypes.JAVA_FILE_SUFFIX;
  }

  @Override
  public TypeSpi getMainType() {
    return m_mainType;
  }

  @Override
  public List<TypeSpi> getTypes() {
    return singletonList(m_mainType);
  }

  @Override
  public List<ImportSpi> getImports() {
    return emptyList();
  }

  @Override
  public SourceRange getSource() {
    return null;
  }

  @Override
  public SourceRange getJavaDoc() {
    return null;
  }
}
