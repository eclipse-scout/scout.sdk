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
package org.eclipse.scout.sdk.core.model.api.internal;

import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformPackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

public class PackageImplementor extends AbstractJavaElementImplementor<PackageSpi> implements IPackage {

  public PackageImplementor(PackageSpi spi) {
    super(spi);
  }

  @Override
  public Path asPath() {
    return Paths.get(elementName().replace(JavaTypes.C_DOT, '/'));
  }

  @Override
  public ISourceGenerator<ISourceBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return Strings.notBlank(transformPackage(this, transformer))
        .<ISourceGenerator<IJavaSourceBuilder<?>>> map(name -> builder -> builder.append("package ").append(name).semicolon())
        .map(g -> g.generalize(JavaSourceBuilder::create))
        .orElseGet(ISourceGenerator::empty);
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    return Stream.empty();
  }

  @Override
  public ISourceGenerator<ISourceBuilder<?>> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
