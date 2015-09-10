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
package org.eclipse.scout.sdk.core.model.api.internal;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.sugar.TypeQuery;

/**
 *
 */
public class CompilationUnitImplementor extends AbstractJavaElementImplementor<CompilationUnitSpi>implements ICompilationUnit {

  public CompilationUnitImplementor(CompilationUnitSpi spi) {
    super(spi);
  }

  @Override
  public boolean isSynthetic() {
    return m_spi.isSynthetic();
  }

  @Override
  public IPackage getPackage() {
    return m_spi.getPackage().wrap();
  }

  @Override
  public IType findTypeBySimpleName(String simpleName) {
    return WrapperUtils.wrapType(m_spi.findTypeBySimpleName(simpleName));
  }

  @Override
  public IType getMainType() {
    return WrapperUtils.wrapType(m_spi.getMainType());
  }

  @Override
  public List<IType> getTypes() {
    return new WrappedList<>(m_spi.getTypes());
  }

  @Override
  public List<IImport> getImports() {
    return new WrappedList<>(m_spi.getImports());
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_spi.getJavaDoc();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

  //additional convenience methods

  @Override
  public TypeQuery types() {
    return new TypeQuery(getTypes());
  }
}
