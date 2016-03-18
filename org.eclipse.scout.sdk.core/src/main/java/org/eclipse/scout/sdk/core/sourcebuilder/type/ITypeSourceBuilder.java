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
package org.eclipse.scout.sdk.core.sourcebuilder.type;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.IMemberSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.typeparameter.ITypeParameterSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;

/**
 * <h3>{@link ITypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface ITypeSourceBuilder extends IMemberSourceBuilder {

  /**
   * @return
   */
  List<String> getInterfaceSignatures();

  /**
   * @return
   */
  String getSuperTypeSignature();

  /**
   * @return the parent {@link ITypeSourceBuilder} or {@link ICompilationUnitSourceBuilder}
   */
  ISourceBuilder getDeclaringElement();

  /**
   * @param parent
   *          {@link ITypeSourceBuilder} or {@link ICompilationUnitSourceBuilder}
   */
  void setDeclaringElement(ISourceBuilder parent);

  /**
   * @return Gets the fully qualified name this type will have after it has been created.
   */
  String getFullyQualifiedName();

  /**
   * @return
   */
  List<IFieldSourceBuilder> getFields();

  /**
   * @return
   */
  List<IMethodSourceBuilder> getMethods();

  /**
   * @return
   */
  List<ITypeSourceBuilder> getTypes();

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
  void setInterfaceSignatures(Collection<String> interfaceSignatures);

  /**
   * @param builder
   */
  void addField(IFieldSourceBuilder builder);

  /**
   * @param sortKey
   * @param builder
   */
  void addSortedField(CompositeObject sortKey, IFieldSourceBuilder builder);

  /**
   * @param builder
   * @return
   */
  boolean removeField(String elementName);

  /**
   * @param builder
   */
  void addMethod(IMethodSourceBuilder builder);

  /**
   * @param sortKey
   * @param builder
   */
  void addSortedMethod(CompositeObject sortKey, IMethodSourceBuilder builder);

  /**
   * @param builder
   * @return
   */
  boolean removeMethod(String elementName);

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
   * @param typeParameter
   */
  void addTypeParameter(ITypeParameterSourceBuilder typeParameter);

  /**
   * @return
   */
  List<ITypeParameterSourceBuilder> getTypeParameters();

  boolean removeTypeParameter(String elementName);

  /**
   * @return
   */
  String getParentFullyQualifiedName();

  /**
   * @param parentFullyQualifiedName
   */
  void setParentFullyQualifiedName(String parentFullyQualifiedName);

  /**
   * Gets the {@link IMethodSourceBuilder} directly contained in this {@link ITypeSourceBuilder} having the given
   * methodId.
   *
   * @param methodId
   *          The methodId of the {@link IMethodSourceBuilder} to return.
   * @return The {@link IMethodSourceBuilder} with the given identifier or <code>null</code> if no such builder exists
   *         in this {@link ITypeSourceBuilder}.
   * @see SignatureUtils#createMethodIdentifier(String, Iterable)
   */
  IMethodSourceBuilder getMethod(String methodId);
}
