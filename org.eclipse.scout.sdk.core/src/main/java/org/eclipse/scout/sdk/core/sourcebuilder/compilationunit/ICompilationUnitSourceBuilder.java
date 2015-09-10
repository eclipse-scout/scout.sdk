/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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

  /**
   * Add an error message that is appended to the end of the comopilation unit as triple-X comment
   * <p>
   * Typically this is a code generation error or semantic check issue
   *
   * @param taskType
   *          such as TODO, FIXME, ...
   * @param msg
   * @param exceptions
   */
  void addErrorMessage(String taskType, String msg, Throwable... exceptions);

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
}
