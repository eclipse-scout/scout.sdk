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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractMemberSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.IMethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.typeparameter.ITypeParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.typeparameter.TypeParameterSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link MethodSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class MethodSourceBuilder extends AbstractMemberSourceBuilder implements IMethodSourceBuilder {

  private String m_returnTypeSignature;
  private final List<IMethodParameterSourceBuilder> m_parameters = new ArrayList<>();
  private final List<ITypeParameterSourceBuilder> m_typeParameters = new ArrayList<>();
  private final List<String> m_exceptionSignatures = new ArrayList<>();
  private ISourceBuilder m_body;

  public MethodSourceBuilder(IMethod element) {
    super(element);
    for (ITypeParameter t : element.typeParameters()) {
      addTypeParameter(new TypeParameterSourceBuilder(t));
    }
    setReturnTypeSignature(SignatureUtils.getTypeSignature(element.returnType()));
    for (IMethodParameter param : element.parameters().list()) {
      addParameter(new MethodParameterSourceBuilder(param));
    }
    for (IType t : element.exceptionTypes()) {
      addExceptionSignature(SignatureUtils.getTypeSignature(t));
    }
    ISourceRange body = element.sourceOfBody();
    if (body != null) {
      setBody(new RawSourceBuilder(body.toString()));
    }
  }

  /**
   * @param elementName
   */
  public MethodSourceBuilder(String elementName) {
    super(elementName);
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
    //method declaration
    source.append(Flags.toString(getFlags())).append(' ');

    // type parameters
    if (!m_typeParameters.isEmpty()) {
      source.append(ISignatureConstants.C_GENERIC_START);
      for (ITypeParameterSourceBuilder p : m_typeParameters) {
        p.createSource(source, lineDelimiter, context, validator);
        source.append(", ");
      }
      source.setLength(source.length() - 2);
      source.append(ISignatureConstants.C_GENERIC_END);
    }

    if (!StringUtils.isEmpty(getReturnTypeSignature())) {//constructor
      source.append(validator.useSignature(getReturnTypeSignature()) + " ");
    }
    source.append(getElementName());
    source.append('(');
    // parameters
    if (!m_parameters.isEmpty()) {
      for (IMethodParameterSourceBuilder param : m_parameters) {
        param.createSource(source, lineDelimiter, context, validator);
        source.append(", ");
      }
      source.setLength(source.length() - 2);
    }
    source.append(')');
    // exceptions
    Iterator<String> exceptionSigIterator = getExceptionSignatures().iterator();
    if (exceptionSigIterator.hasNext()) {
      // first
      source.append(" throws ").append(validator.useSignature(exceptionSigIterator.next()));
    }
    while (exceptionSigIterator.hasNext()) {
      source.append(", ").append(validator.useSignature(exceptionSigIterator.next()));

    }
    if (Flags.isInterface(getFlags()) || Flags.isAbstract(getFlags())) {
      source.append(';');
    }
    else {
      // content
      source.append('{').append(lineDelimiter);
      int beforeContent = source.length();
      if (getBody() != null) {
        getBody().createSource(source, lineDelimiter, context, validator);
      }
      if (beforeContent < source.length()) {
        source.append(lineDelimiter);
      }
      source.append('}');
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
  public boolean addParameter(IMethodParameterSourceBuilder parameter) {
    return m_parameters.add(parameter);
  }

  @Override
  public boolean removeParameter(String elementName) {
    for (Iterator<IMethodParameterSourceBuilder> it = m_parameters.iterator(); it.hasNext();) {
      if (elementName.equals(it.next().getElementName())) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public List<IMethodParameterSourceBuilder> getParameters() {
    return Collections.unmodifiableList(m_parameters);
  }

  @Override
  public void addTypeParameter(ITypeParameterSourceBuilder typeParameter) {
    m_typeParameters.add(typeParameter);
  }

  @Override
  public boolean removeTypeParameter(String elementName) {
    for (Iterator<ITypeParameterSourceBuilder> it = m_typeParameters.iterator(); it.hasNext();) {
      if (elementName.equals(it.next().getElementName())) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public List<ITypeParameterSourceBuilder> getTypeParameters() {
    return Collections.unmodifiableList(m_typeParameters);
  }

  @Override
  public String getMethodIdentifier() {
    List<String> methodParamSignatures = new ArrayList<>(m_parameters.size());
    for (IMethodParameterSourceBuilder param : m_parameters) {
      methodParamSignatures.add(param.getDataTypeSignature());
    }
    return SignatureUtils.createMethodIdentifier(getElementName(), methodParamSignatures);
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
  public ISourceBuilder getBody() {
    return m_body;
  }

  @Override
  public void setBody(ISourceBuilder body) {
    m_body = body;
  }
}
