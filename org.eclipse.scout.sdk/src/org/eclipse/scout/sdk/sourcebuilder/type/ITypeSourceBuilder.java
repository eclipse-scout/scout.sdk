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
package org.eclipse.scout.sdk.sourcebuilder.type;

import java.util.List;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.sourcebuilder.IAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;

/**
 * <h3>{@link ITypeSourceBuilder}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface ITypeSourceBuilder extends IAnnotatableSourceBuilder {

  /**
   * @return
   */
  List<String> getInterfaceSignatures();

  /**
   * @return
   */
  String getSuperTypeSignature();

  /**
   * @return Gets the fully qualified name this type will have after it has been created.
   */
  String getFullyQualifiedName();

  /**
   * @return
   */
  List<IFieldSourceBuilder> getFieldSourceBuilders();

  /**
   * @return
   */
  List<IMethodSourceBuilder> getMethodSourceBuilders();

  /**
   * @return
   */
  List<ITypeSourceBuilder> getTypeSourceBuilder();

  /**
   * @param commentSourceBuilder
   */
  void setCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder);

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
   * @param superTypeSignature
   */
  void setSuperTypeSignature(String superTypeSignature);

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
  ITypeSourceBuilder getParentTypeSourceBuilder();

  /**
   * @param parentBuilder
   */
  void setParentTypeSourceBuilder(ITypeSourceBuilder parentBuilder);

  /**
   * @return
   */
  String getParentFullyQualifiedName();

  /**
   * @param parentFullyQualifiedName
   */
  void setParentFullyQualifiedName(String parentFullyQualifiedName);
}
