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
package org.eclipse.scout.sdk.sourcebuilder.compilationunit;

import java.util.List;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.IJavaElementSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;

/**
 * <h3>{@link ICompilationUnitSourceBuilder}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface ICompilationUnitSourceBuilder extends IJavaElementSourceBuilder {

  String getPackageFragmentName();

  /**
   * @return
   */
  List<ITypeSourceBuilder> getTypeSourceBuilder();

  /**
   * @param commentSourceBuilder
   */
  void setCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder);

  /**
   * @param builder
   */
  void addTypeSourceBuilder(ITypeSourceBuilder builder);

  /**
   * @param sortKey
   * @param builder
   */
  void addSortedTypeSourceBuilder(CompositeObject sortKey, ITypeSourceBuilder builder);

  /**
   * @param builder
   * @return
   */
  boolean removeTypeSourceBuilder(ITypeSourceBuilder builder);
}
