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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.MethodParameter;

/**
 * <h3>{@link MethodSourceBuilder}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class MethodSourceBuilder extends AbstractAnnotatableSourceBuilder implements IMethodSourceBuilder {

  private String m_returnTypeSignature;
  private final List<MethodParameter> m_parameters;
  private final List<String> m_exceptionSignatures;
  private IMethodBodySourceBuilder m_methodBodySourceBuilder;

  /**
   * @param elementName
   */
  public MethodSourceBuilder(String elementName) {
    super(elementName);
    m_parameters = new ArrayList<MethodParameter>();
    m_exceptionSignatures = new ArrayList<String>();
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    super.createSource(source, lineDelimiter, ownerProject, validator);
    //method declaration
    source.append(Flags.toString(getFlags())).append(" ");
    if (!StringUtility.isNullOrEmpty(getReturnTypeSignature())) {
      source.append(SignatureUtility.getTypeReference(getReturnTypeSignature(), validator) + " ");
    }
    source.append(getElementName());
    source.append("(");
    // parameters
    Iterator<MethodParameter> parameterIterator = getParameters().iterator();
    if (parameterIterator.hasNext()) {
      MethodParameter param = parameterIterator.next();
      source.append(SignatureUtility.getTypeReference(param.getSignature(), validator)).append(" ").append(param.getName());
    }
    while (parameterIterator.hasNext()) {
      MethodParameter param = parameterIterator.next();
      source.append(", ").append(SignatureUtility.getTypeReference(param.getSignature(), validator)).append(" ").append(param.getName());
    }
    source.append(")");
    // exceptions
    Iterator<String> exceptionSigIterator = getExceptionSignatures().iterator();
    if (exceptionSigIterator.hasNext()) {
      // first
      source.append(" throws ").append(SignatureUtility.getTypeReference(exceptionSigIterator.next(), validator));
    }
    while (exceptionSigIterator.hasNext()) {
      source.append(", ").append(SignatureUtility.getTypeReference(exceptionSigIterator.next(), validator));

    }
    if (Flags.isInterface(getFlags()) || Flags.isAbstract(getFlags())) {
      source.append(";");
    }
    else {
      // content
      source.append("{").append(lineDelimiter);
      int beforeContent = source.length();
      createMethodBody(source, lineDelimiter, ownerProject, validator);
      if (beforeContent < source.length()) {
        source.append(lineDelimiter);
      }
      source.append("}");
    }

  }

  public void createMethodBody(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    if (getMethodBodySourceBuilder() != null) {
      getMethodBodySourceBuilder().createSource(this, source, lineDelimiter, ownerProject, validator);
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
  public void setParameters(List<MethodParameter> parameters) {
    m_parameters.clear();
    m_parameters.addAll(parameters);
  }

  @Override
  public boolean addParameter(MethodParameter parameter) {
    return m_parameters.add(parameter);
  }

  @Override
  public boolean removeParameter(MethodParameter parameter) {
    return m_parameters.remove(parameter);
  }

  @Override
  public List<MethodParameter> getParameters() {
    return Collections.unmodifiableList(m_parameters);
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
  public void setExceptionSignatures(String[] exceptionSignatures) {
    m_exceptionSignatures.clear();
    m_exceptionSignatures.addAll(Arrays.asList(exceptionSignatures));
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
