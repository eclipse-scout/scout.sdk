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
package org.eclipse.scout.sdk.core.sourcebuilder.compilationunit;

import java.util.List;

import org.eclipse.scout.sdk.core.sourcebuilder.IJavaElementSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;

/**
 * <h3>{@link ICompilationUnitSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface ICompilationUnitSourceBuilder extends IJavaElementSourceBuilder {

  String getPackageName();

  void addDeclaredImport(String name);

  void addDeclaredStaticImport(String name);

  List<String> getDeclaredImports();

  List<String> getDeclaredStaticImports();

  ITypeSourceBuilder getMainType();

  /**
   * @return
   */
  List<ITypeSourceBuilder> getTypes();

  /**
   * @param builder
   */
  void addType(ITypeSourceBuilder builder);

  /**
   * @param sortKey
   * @param builder
   */
  void addSortedType(CompositeObject sortKey, ITypeSourceBuilder builder);

  /**
   * @param builder
   * @return
   */
  boolean removeType(String elementName);

  /**
   * Adds an {@link ISourceBuilder} to this {@link ICompilationUnitSourceBuilder} which is appended to the compilation
   * unit at the very end (after the last closing bracket).
   *
   * @param builder
   *          The {@link ISourceBuilder} to add.
   */
  void addFooter(ISourceBuilder builder);

  /**
   * @return A {@link List} with all footer {@link ISourceBuilder}s in the order in which they have been added.
   */
  List<ISourceBuilder> getFooters();
}
