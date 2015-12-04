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
package org.eclipse.scout.sdk.core.sourcebuilder.method;

import java.util.List;

import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.IMemberSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.IMethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.typeparameter.ITypeParameterSourceBuilder;

/**
 * <h3>{@link IMethodSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface IMethodSourceBuilder extends IMemberSourceBuilder {

  /**
   * Returns a unique identifier of this method. The identifier looks like
   * 'methodname(param1Signature,param2Signature)'.<br>
   * The result of this method can be used to compare methods together with the
   * {@link SignatureUtils#createMethodIdentifier(org.eclipse.scout.sdk.core.model.api.IMethod)}.
   *
   * @return
   */
  String getMethodIdentifier();

  /**
   * @return
   */
  String getReturnTypeSignature();

  /**
   * @param returnTypeSignature
   */
  void setReturnTypeSignature(String returnTypeSignature);

  /**
   * @return
   */
  List<String> getExceptionSignatures();

  /**
   * @param exceptionSignatures
   */
  void setExceptionSignatures(List<String> exceptionSignatures);

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
   * @return
   */
  ISourceBuilder getBody();

  /**
   * @param methodBodySourceBuilder
   */
  void setBody(ISourceBuilder body);

  /**
   * @return
   */
  List<IMethodParameterSourceBuilder> getParameters();

  boolean addParameter(IMethodParameterSourceBuilder parameter);

  /**
   * @param parameter
   * @return
   */
  boolean removeParameter(String elementName);

  /**
   * @param typeParameter
   */
  void addTypeParameter(ITypeParameterSourceBuilder typeParameter);

  /**
   * @return
   */
  List<ITypeParameterSourceBuilder> getTypeParameters();

  boolean removeTypeParameter(String elementName);

}
