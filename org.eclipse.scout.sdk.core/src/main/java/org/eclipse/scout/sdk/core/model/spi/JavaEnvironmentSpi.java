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
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IFileLocator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.internal.JavaEnvironmentWithJdt;

/**
 * <h3>{@link JavaEnvironmentSpi}</h3> Represents a Java lookup environment (classpath) capable to resolve
 * {@link TypeSpi}s by name.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface JavaEnvironmentSpi {

  PackageSpi getPackage(String name);

  /**
   * Tries to find the {@link TypeSpi} with the given name in the receiver {@link JavaEnvironmentSpi} (classpath).
   * <p>
   * Also primitive types such as int, float, void, null etc. are supported
   *
   * @param fqn
   *          The fully qualified name of the {@link TypeSpi} to find. For inner {@link TypeSpi}s the inner part must be
   *          separated using '$': <code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.
   * @return The {@link Iype} matching the given fully qualified name or <code>null</code> if it could not be found.
   */
  TypeSpi findType(String fqn);

  /**
   * @return the file locator used to read files from the workspace module.
   *         <p>
   *         This method returns null if this {@link IJavaEnvironment} is not a workspace module.
   */
  IFileLocator getFileLocator();

  /**
   * @return the new {@link JavaEnvironmentSpi}. This new environment is automatically published to all existing api
   *         classes that are wrapping an spi of it
   */
  JavaEnvironmentSpi reload();

  /**
   * Register an override for a (possibly) existing compilation unit. This only has an effect after a call to
   * {@link #reload()}
   *
   * @param packageName
   * @param fileName
   * @param src
   * @return
   */
  boolean registerCompilationUnitOverride(String packageName, String fileName, char[] src);

  /**
   * @param fqn
   *          type name
   * @return null if the type has no compilation errors
   */
  String getCompileErrors(String fqn);

  List<ClasspathSpi> getClasspath();

  IJavaEnvironment wrap();

  /**
   * @return
   */
  JavaEnvironmentWithJdt emptyCopy();

}
