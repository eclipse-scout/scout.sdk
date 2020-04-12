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
import static java.util.Collections.singletonList;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.CompilationUnitImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.ImportSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 *
 */
public class SyntheticCompilationUnitWithEcj extends AbstractJavaElementWithEcj<ICompilationUnit> implements CompilationUnitSpi {
  private final TypeSpi m_mainType;

  protected SyntheticCompilationUnitWithEcj(AbstractJavaEnvironment env, TypeSpi mainType) {
    super(env);
    m_mainType = mainType;
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    TypeSpi newType = getJavaEnvironment().findType(m_mainType.getName());
    if (newType != null) {
      return newType.getCompilationUnit();
    }
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
  public ISourceRange getSource() {
    return null;
  }

  @Override
  public ISourceRange getJavaDoc() {
    return null;
  }
}
