/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.query.InnerTypeQuery;
import org.eclipse.scout.sdk.core.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class CompilationUnitImplementor extends AbstractJavaElementImplementor<CompilationUnitSpi> implements ICompilationUnit {

  public CompilationUnitImplementor(CompilationUnitSpi spi) {
    super(spi);
  }

  @Override
  public boolean isSynthetic() {
    return m_spi.isSynthetic();
  }

  @Override
  public IPackage containingPackage() {
    return m_spi.getPackage().wrap();
  }

  @Override
  public Optional<IType> resolveTypeBySimpleName(String simpleName) {
    return Optional.ofNullable(m_spi.findTypeBySimpleName(simpleName))
        .map(TypeSpi::wrap);
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    Stream<? extends IJavaElement> packageAndImports = Stream.concat(Stream.of(containingPackage()), imports());
    return Stream.concat(packageAndImports, types().stream());
  }

  @Override
  public Optional<IType> mainType() {
    return Optional.ofNullable(m_spi.getMainType())
        .map(TypeSpi::wrap);
  }

  @Override
  public IType requireMainType() {
    return mainType()
        .orElseThrow(() -> newFail("Compilation Unit '{}' has no main type.", path()));
  }

  @Override
  public Path path() {
    return containingPackage().asPath().resolve(elementName());
  }

  @Override
  public Stream<IImport> imports() {
    return WrappingSpliterator.stream(m_spi.getImports());
  }

  @Override
  public Optional<ISourceRange> javaDoc() {
    return Optional.ofNullable(m_spi.getJavaDoc());
  }

  @Override
  public InnerTypeQuery types() {
    return new InnerTypeQuery(new WrappingSpliterator<>(m_spi.getTypes()));
  }

  @Override
  public ICompilationUnitGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return IWorkingCopyTransformer.transformCompilationUnit(this, transformer);
  }

  @Override
  public ICompilationUnitGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
