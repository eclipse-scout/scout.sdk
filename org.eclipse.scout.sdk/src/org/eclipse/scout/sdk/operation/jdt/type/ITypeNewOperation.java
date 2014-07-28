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
package org.eclipse.scout.sdk.operation.jdt.type;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;

/**
 * <h3>{@link ITypeNewOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.9.0 05.04.2013
 */
public interface ITypeNewOperation extends IOperation {

  /**
   * @return
   */
  ITypeSourceBuilder getSourceBuilder();

  /**
   * @return
   */
  String getElementName();

  /**
   * @param commentSourceBuilder
   */
  void setTypeCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder);

  /**
   * @return
   */
  ICommentSourceBuilder getTypeCommentSourceBuilder();

  /**
   * @param flags
   */
  void setFlags(int flags);

  /**
   * @return
   */
  int getFlags();

  /**
   * @param builder
   */
  void addAnnotationSourceBuilder(IAnnotationSourceBuilder builder);

  /**
   * @param sortKey
   * @param builder
   */
  void addSortedAnnotationSourceBuilder(CompositeObject sortKey, IAnnotationSourceBuilder builder);

  /**
   * @param childOp
   * @return
   */
  boolean removeAnnotationSourceBuilder(IAnnotationSourceBuilder childOp);

  /**
   * @return
   */
  List<IAnnotationSourceBuilder> getAnnotationSourceBuilders();

  /**
   * @param superTypeSignature
   */
  void setSuperTypeSignature(String superTypeSignature);

  /**
   * @return
   */
  String getSuperTypeSignature();

  /**
   * @param interfaceSignature
   */
  void addInterfaceSignature(String interfaceSignature);

  /**
   * @param interfaceSignature
   * @return
   */
  boolean removeInterfaceSignature(String interfaceSignature);

  /**
   * @param interfaceSignatures
   */
  void setInterfaceSignatures(String[] interfaceSignatures);

  /**
   * @return
   */
  List<String> getInterfaceSignatures();

  /**
   * @param builder
   */
  void addFieldSourceBuilder(IFieldSourceBuilder builder);

  /**
   * @param sortKey
   * @param builder
   */
  void addSortedFieldSourceBuilder(CompositeObject sortKey, IFieldSourceBuilder builder);

  /**
   * @param builder
   * @return
   */
  boolean removeFieldSourceBuilder(IFieldSourceBuilder builder);

  /**
   * @return
   */
  List<IFieldSourceBuilder> getFieldSourceBuilders();

  /**
   * @param builder
   */
  void addMethodSourceBuilder(IMethodSourceBuilder builder);

  /**
   * @param sortKey
   * @param builder
   */
  void addSortedMethodSourceBuilder(CompositeObject sortKey, IMethodSourceBuilder builder);

  /**
   * @param builder
   * @return
   */
  boolean removeMethodSourceBuilder(IMethodSourceBuilder builder);

  /**
   * @return
   */
  List<IMethodSourceBuilder> getMethodSourceBuilders();

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

  /**
   * @return
   */
  List<ITypeSourceBuilder> getTypeSourceBuilder();

  /**
   * @param formatSource
   */
  void setFormatSource(boolean formatSource);

  /**
   * @return
   */
  boolean isFormatSource();

  /**
   * @return
   */
  IType getCreatedType();

}
