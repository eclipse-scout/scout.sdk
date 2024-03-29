/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.query.AnnotationQuery;
import org.eclipse.scout.sdk.core.java.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

public class PackageImplementor extends AbstractAnnotatableImplementor<PackageSpi> implements IPackage {

  public PackageImplementor(PackageSpi spi) {
    super(spi);
  }

  @Override
  public Path asPath() {
    var packageName = elementName();
    if (packageName == null) {
      // default package
      return Paths.get("");
    }
    return Paths.get(packageName.replace(JavaTypes.C_DOT, '/'));
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    return annotations().stream();
  }

  @Override
  public Optional<IPackage> parent() {
    return Optional.ofNullable(m_spi.getParentPackage())
        .map(PackageSpi::wrap);
  }

  @Override
  public Optional<IType> packageInfo() {
    return Optional.ofNullable(m_spi.getPackageInfo())
        .map(TypeSpi::wrap);
  }

  @Override
  public AnnotationQuery<IAnnotation> annotations() {
    return packageInfo()
        .<AnnotationQuery<IAnnotation>> map(pi -> new AnnotationQuery<>(pi, m_spi))
        .orElseGet(() -> new EmptyAnnotationQuery(m_spi));
  }

  @Override
  public PackageGenerator toWorkingCopy() {
    return toWorkingCopy(null);
  }

  @Override
  public PackageGenerator toWorkingCopy(IWorkingCopyTransformer transformer) {
    return PackageGenerator.create(this, transformer);
  }

  private static class EmptyAnnotationQuery extends AnnotationQuery<IAnnotation> {

    public EmptyAnnotationQuery(JavaElementSpi owner) {
      super(null, owner);
    }

    @Override
    protected Stream<IAnnotation> createStream() {
      return Stream.empty();
    }
  }
}
