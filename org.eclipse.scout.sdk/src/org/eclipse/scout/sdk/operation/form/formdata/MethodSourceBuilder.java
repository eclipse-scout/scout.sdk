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
package org.eclipse.scout.sdk.operation.form.formdata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;

/**
 *
 */
public class MethodSourceBuilder implements ISourceBuilder {

  private List<MethodParameter> m_parameters;
  private List<String> m_exceptionSignatures;
  private String m_returnSignature;
  private String m_elementName;
  private int m_flags;
  private String m_simpleBody;

  public MethodSourceBuilder() {
    m_parameters = new ArrayList<MethodParameter>();
    m_exceptionSignatures = new ArrayList<String>();
    // default
    m_flags = Flags.AccPublic;
  }

  @Override
  public String createSource(IImportValidator validator) {
    StringBuilder builder = new StringBuilder();
    if (Flags.isPublic(getFlags())) {
      builder.append("public ");
    }
    if (Flags.isPrivate(getFlags())) {
      builder.append("private ");
    }
    if (Flags.isProtected(getFlags())) {
      builder.append("protected ");
    }
    if (Flags.isStatic(getFlags())) {
      builder.append("static ");
    }
    if (Flags.isFinal(getFlags())) {
      builder.append("final ");
    }
    builder.append(ScoutSdkUtility.getSimpleTypeRefName(getReturnSignature(), validator) + " ");
    builder.append(getElementName() + "(");
    MethodParameter[] params = getParameters();
    for (int i = 0; i < params.length; i++) {
      builder.append(ScoutSdkUtility.getSimpleTypeRefName(params[i].getSignature(), validator) + " ");
      builder.append(params[i].getName());
      if (i < (params.length - 1)) {
        builder.append(", ");
      }
    }
    builder.append(") ");
    String[] exceptions = getExceptionSignatures();
    if (exceptions.length > 0) {
      builder.append("throws ");
      for (int i = 0; i < exceptions.length; i++) {
        builder.append(ScoutSdkUtility.getSimpleTypeRefName(exceptions[i], validator));
        if (i < (exceptions.length - 1)) {
          builder.append(", ");
        }
      }
      builder.append(" ");
    }
    builder.append("{\n");
    String value = createMethodBody(validator);
    if (!StringUtility.isNullOrEmpty(value)) {
      builder.append(value + "\n");
    }
    builder.append("}\n");
    return null;
  }

  protected String createMethodBody(@SuppressWarnings("unused") IImportValidator validator) {
    if (!StringUtility.isNullOrEmpty(getSimleBody())) {
      return getSimleBody();
    }
    return null;
  }

  public void setElementName(String elementName) {
    m_elementName = elementName;
  }

  @Override
  public String getElementName() {
    return m_elementName;
  }

  public void setFlags(int flags) {
    m_flags = flags;
  }

  public int getFlags() {
    return m_flags;
  }

  public void setSimpleBody(String simpleBody) {
    m_simpleBody = simpleBody;
  }

  private String getSimleBody() {
    return m_simpleBody;
  }

  public void setReturnSignature(String returnSignature) {
    m_returnSignature = returnSignature;
  }

  public String getReturnSignature() {
    return m_returnSignature;
  }

  private String[] getExceptionSignatures() {
    return m_exceptionSignatures.toArray(new String[m_exceptionSignatures.size()]);
  }

  public void addExceptionSignature(String signature) {
    m_exceptionSignatures.add(signature);
  }

  private MethodParameter[] getParameters() {
    return m_parameters.toArray(new MethodParameter[m_parameters.size()]);
  }

  public void addParameter(MethodParameter param) {
    m_parameters.add(param);
  }

}
