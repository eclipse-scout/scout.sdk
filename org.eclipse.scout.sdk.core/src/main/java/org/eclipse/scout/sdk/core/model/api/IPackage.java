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
package org.eclipse.scout.sdk.core.model.api;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.scout.sdk.core.model.spi.PackageSpi;

/**
 * <h3>{@link IPackage}</h3> Represents a package declaration in an {@link ICompilationUnit}.
 *
 * @since 5.1.0
 */
public interface IPackage extends IJavaElement {

  /**
   * @return The full name of the package or {@code null} if it is the default package.
   */
  @Override
  String elementName();

  /**
   * Gets the name of this {@link IPackage} as {@link Path}.
   * <p>
   * <b>Example:</b><br>
   * {@code org.eclipse.scout.sdk} -> {@code org/eclipse/scout/sdk}
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
}
