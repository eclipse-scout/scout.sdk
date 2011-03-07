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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.util.ScoutUtility;

/**
 *
 */
public class MethodSourceBuilder implements ISourceBuilder {

  private String m_javaDoc;
  private List<MethodParameter> m_parameters;
  private List<String> m_exceptionSignatures;
  private List<AnnotationSourceBuilder> m_annotations;
  private String m_returnSignature;
  private String m_elementName;
  private int m_flags;
  private String m_simpleBody;

  public MethodSourceBuilder() {
    m_parameters = new ArrayList<MethodParameter>();
    m_exceptionSignatures = new ArrayList<String>();
    m_annotations = new ArrayList<AnnotationSourceBuilder>();
    // default
    m_flags = Flags.AccPublic;
    m_returnSignature = Signature.SIG_VOID;
  }

  @Override
  public String createSource(IImportValidator validator) {
    StringBuilder builder = new StringBuilder();
    String javaDoc = createJavaDoc(validator);
    if (!StringUtility.isNullOrEmpty(javaDoc)) {
      builder.append(javaDoc + ScoutUtility.NL);
    }
    for (AnnotationSourceBuilder annotation : getAnnotations()) {
      builder.append(annotation.createSource(validator) + ScoutUtility.NL);
    }
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
    builder.append("{" + ScoutUtility.NL);
    String value = createMethodBody(validator);
    if (!StringUtility.isNullOrEmpty(value)) {
      builder.append(value + ScoutUtility.NL);
    }
    builder.append("}" + ScoutUtility.NL);
    return builder.toString();
  }

  /**
   * overwrite this method to create an enhanced javadoc.
   * 
   * @param validator
   *          the import validator to validate imports for javadoc.
   * @return
   */
  protected String createJavaDoc(IImportValidator validator) {
    return getJavaDoc();
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

  private AnnotationSourceBuilder[] getAnnotations() {
    return m_annotations.toArray(new AnnotationSourceBuilder[m_annotations.size()]);
  }

  public void addAnnotation(AnnotationSourceBuilder annotation) {
    m_annotations.add(annotation);
  }

  /**
   * @param javaDoc
   *          the javaDoc to set
   */
  public void setJavaDoc(String javaDoc) {
    m_javaDoc = javaDoc;
  }

  /**
   * @return the javaDoc
   */
  public String getJavaDoc() {
    return m_javaDoc;
  }

}
