/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.scout.sdk.core.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;

/**
 * <h3>{@link IPackage}</h3> Represents a package declaration in an {@link ICompilationUnit}.
 *
 * @since 5.1.0
 */
public interface IPackage extends IAnnotatable {

  /**
   * @return The full name of the package or {@code null} if it is the default package.
   */
  @Override
  String elementName();

  /**
   * @return The {@code package-info} type in this {@link IPackage}.
   */
  Optional<IType> packageInfo();

  /**
   * Gets the parent {@link IPackage} of this {@link IPackage}.<br>
   * E.g. if this package is {@code org.eclipse.scout}, the parent package would be {@code org.eclipse}<br>
   * The parent of a top level package (e.g. {@code org}) is the default package. The default package itself has no
   * parent {@link IPackage}.
   * 
   * @return The parent {@link IPackage} or an empty {@link Optional} if this is the default package.
   */
  Optional<IPackage> parent();

  /**
   * Gets the name of this {@link IPackage} as {@link Path}.
   * <p>
   * <b>Example:</b><br>
   * {@code org.eclipse.scout} -> {@code org/eclipse/scout}
   * <p>
   * Please note that the resulting {@link Path} may use platform dependent file separators e.g. when converted to a
   * {@link String}.
   * 
   * @return The {@link IPackage} as {@link Path}.
   */
  Path asPath();

  /**
   * @return Always returns an empty {@link Optional}.
   */
  @Override
  Optional<ISourceRange> source();

  @Override
  PackageSpi unwrap();

  @Override
  PackageGenerator toWorkingCopy();

  @Override
  PackageGenerator toWorkingCopy(IWorkingCopyTransformer transformer);
}
