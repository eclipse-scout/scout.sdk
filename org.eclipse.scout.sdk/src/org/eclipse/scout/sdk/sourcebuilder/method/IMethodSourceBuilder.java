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
package org.eclipse.scout.sdk.sourcebuilder.method;

import java.util.List;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.sourcebuilder.IAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.MethodParameter;

/**
 * <h3>{@link IMethodSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface IMethodSourceBuilder extends IAnnotatableSourceBuilder {

  /**
   * @return
   */
  String getReturnTypeSignature();

  /**
   * @return
   */
  List<String> getExceptionSignatures();

  /**
   * @return
   */
  List<MethodParameter> getParameters();

  /**
   * @return
   */
  IMethodBodySourceBuilder getMethodBodySourceBuilder();

  /**
   * @param commentSourceBuilder
   */
  void setCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder);

  /**
   * @param flags
   */
  void setFlags(int flags);

  /**
   * @param builder
   */
  void addAnnotationSourceBuilder(IAnnotationSourceBuilder builder);

  /**
   * @return
   */
  int getFlags();

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
   * @param returnTypeSignature
   */
  void setReturnTypeSignature(String returnTypeSignature);

  /**
   * @param parameters
   */
  void setParameters(List<MethodParameter> parameters);

  /**
   * @param parameter
   * @return
   */
  boolean addParameter(MethodParameter parameter);

  /**
   * @param parameter
   * @return
   */
  boolean removeParameter(MethodParameter parameter);

  /**
   * @param exceptionSignature
   */
  void addExceptionSignature(String exceptionSignature);

  /**
   * @param exceptionSignature
   * @return
   */
  boolean removeExceptionSignature(String exceptionSignature);

  /**
   * @param exceptionSignatures
   */
  void setExceptionSignatures(String[] exceptionSignatures);

  /**
   * @param methodBodySourceBuilder
   */
  void setMethodBodySourceBuilder(IMethodBodySourceBuilder methodBodySourceBuilder);

  /**
   * Returns a unique identifier of this method. The identifier looks like
   * 'methodname(param1Signature,param2Signature)'.<br>
   * The result of this method can be used to compare methods together with the
   * {@link SignatureUtility#getMethodIdentifier(org.eclipse.jdt.core.IMethod)}.
   *
   * @return
   */
  String getMethodIdentifier();

}
