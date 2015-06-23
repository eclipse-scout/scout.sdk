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
package org.eclipse.scout.sdk.core.sourcebuilder.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link MethodSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class MethodSourceBuilder extends AbstractAnnotatableSourceBuilder implements IMethodSourceBuilder {

  private String m_returnTypeSignature;
  private final List<MethodParameterDescription> m_parameters;
  private final List<String> m_exceptionSignatures;
  private IMethodBodySourceBuilder m_methodBodySourceBuilder;

  /**
   * @param elementName
   */
  public MethodSourceBuilder(String elementName) {
    super(elementName);
    m_parameters = new ArrayList<>();
    m_exceptionSignatures = new ArrayList<>();
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
    //method declaration
    source.append(Flags.toString(getFlags())).append(" ");
    if (!StringUtils.isEmpty(getReturnTypeSignature())) {
      source.append(SignatureUtils.getTypeReference(getReturnTypeSignature(), validator) + " ");
    }
    source.append(getElementName());
    source.append("(");
    // parameters
    Iterator<MethodParameterDescription> parameterIterator = getParameters().iterator();
    if (parameterIterator.hasNext()) {
      MethodParameterDescription param = parameterIterator.next();
      if (param.getFlags() != Flags.AccDefault) {
        source.append(Flags.toString(param.getFlags())).append(" ");
      }
      source.append(SignatureUtils.getTypeReference(param.getSignature(), validator)).append(" ").append(param.getName());
    }
    while (parameterIterator.hasNext()) {
      source.append(", ");
      MethodParameterDescription param = parameterIterator.next();
      if (param.getFlags() != Flags.AccDefault) {
        source.append(Flags.toString(param.getFlags())).append(" ");
      }
      source.append(SignatureUtils.getTypeReference(param.getSignature(), validator)).append(" ").append(param.getName());
    }
    source.append(")");
    // exceptions
    Iterator<String> exceptionSigIterator = getExceptionSignatures().iterator();
    if (exceptionSigIterator.hasNext()) {
      // first
      source.append(" throws ").append(SignatureUtils.getTypeReference(exceptionSigIterator.next(), validator));
    }
    while (exceptionSigIterator.hasNext()) {
      source.append(", ").append(SignatureUtils.getTypeReference(exceptionSigIterator.next(), validator));

    }
    if (Flags.isInterface(getFlags()) || Flags.isAbstract(getFlags())) {
      source.append(";");
    }
    else {
      // content
      source.append("{").append(lineDelimiter);
      int beforeContent = source.length();
      createMethodBody(source, lineDelimiter, context, validator);
      if (beforeContent < source.length()) {
        source.append(lineDelimiter);
      }
      source.append("}");
    }

  }

  public void createMethodBody(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    if (getMethodBodySourceBuilder() != null) {
      getMethodBodySourceBuilder().createSource(this, source, lineDelimiter, context, validator);
    }
  }

  @Override
  public void setReturnTypeSignature(String returnTypeSignature) {
    m_returnTypeSignature = returnTypeSignature;
  }

  @Override
  public String getReturnTypeSignature() {
    return m_returnTypeSignature;
  }

  @Override
  public void setParameters(Set<MethodParameterDescription> parameters) {
    m_parameters.clear();
    m_parameters.addAll(parameters);
  }

  @Override
  public boolean addParameter(MethodParameterDescription parameter) {
    return m_parameters.add(parameter);
  }

  @Override
  public boolean removeParameter(MethodParameterDescription parameter) {
    return m_parameters.remove(parameter);
  }

  @Override
  public List<MethodParameterDescription> getParameters() {
    return Collections.unmodifiableList(m_parameters);
  }

  @Override
  public String getMethodIdentifier() {
    List<String> methodParamSignatures = new ArrayList<>(m_parameters.size());
    for (MethodParameterDescription param : m_parameters) {
      methodParamSignatures.add(param.getSignature());
    }
    return SignatureUtils.getMethodIdentifier(getElementName(), methodParamSignatures);
  }

  @Override
  public String toString() {
    return getMethodIdentifier();
  }

  @Override
  public void addExceptionSignature(String exceptionSignature) {
    m_exceptionSignatures.add(exceptionSignature);
  }

  @Override
  public boolean removeExceptionSignature(String exceptionSignature) {
    return m_exceptionSignatures.remove(exceptionSignature);
  }

  @Override
  public void setExceptionSignatures(List<String> exceptionSignatures) {
    m_exceptionSignatures.clear();
    m_exceptionSignatures.addAll(exceptionSignatures);
  }

  @Override
  public List<String> getExceptionSignatures() {
    return Collections.unmodifiableList(m_exceptionSignatures);
  }

  @Override
  public IMethodBodySourceBuilder getMethodBodySourceBuilder() {
    return m_methodBodySourceBuilder;
  }

  @Override
  public void setMethodBodySourceBuilder(IMethodBodySourceBuilder methodBodySourceBuilder) {
    m_methodBodySourceBuilder = methodBodySourceBuilder;
  }
}
