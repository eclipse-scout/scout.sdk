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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.CompilationUnitImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.ImportSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class SyntheticCompilationUnitWithJdt extends AbstractJavaElementWithJdt<ICompilationUnit> implements CompilationUnitSpi {
  private final TypeSpi m_mainType;

  SyntheticCompilationUnitWithJdt(JavaEnvironmentWithJdt env, BindingTypeWithJdt mainType) {
    super(env);
    m_mainType = Validate.notNull(mainType);
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    TypeSpi newType = newEnv.findType(m_mainType.getName());
    if (newType != null) {
      return newType.getCompilationUnit();
    }
    return null;
  }

  @Override
  protected ICompilationUnit internalCreateApi() {
    return new CompilationUnitImplementor(this);
  }

  protected static char[] computeUniqueKey(BindingTypeWithJdt mainType) {
    return mainType.getName().toCharArray();
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
    return m_mainType.getElementName() + SuffixConstants.SUFFIX_STRING_java;
  }

  @Override
  public TypeSpi getMainType() {
    return m_mainType;
  }

  @Override
  public List<TypeSpi> getTypes() {
    return Collections.singletonList(m_mainType);
  }

  @Override
  public List<ImportSpi> getImports() {
    return Collections.emptyList();
  }

  @Override
  public ISourceRange getSource() {
    return ISourceRange.NO_SOURCE;
  }

  @Override
  public ISourceRange getJavaDoc() {
    return ISourceRange.NO_SOURCE;
  }
}