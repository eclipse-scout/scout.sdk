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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 * Source builder for java field declarations (without getter and setter methods). By default, a private non-static
 * and non-final field is created.
 * 
 * @since 3.8.2
 */
public class FieldSourceBuilder implements ISourceBuilder {

  private String m_elementName;
  private String m_signature;
  private String m_assignment;
  private int m_flags;

  public FieldSourceBuilder() {
    // defaults
    m_flags = Flags.AccPrivate;
  }

  @Override
  public String createSource(IImportValidator validator) throws JavaModelException {
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
    builder.append(SignatureUtility.getTypeReference(getSignature(), validator));
    builder.append(" ");
    builder.append(getElementName());
    if (getAssignment() != null) {
      builder.append(" = ");
      builder.append(getAssignment());
    }
    builder.append(";");
    return builder.toString();
  }

  @Override
  public int getType() {
    return PROPERTY_SOURCE_BUILDER;
  }

  @Override
  public String getElementName() {
    return m_elementName;
  }

  public void setElementName(String elementName) {
    m_elementName = elementName;
  }

  public String getSignature() {
    return m_signature;
  }

  public void setSignature(String signature) {
    m_signature = signature;
  }

  public String getAssignment() {
    return m_assignment;
  }

  public void setAssignment(String assignment) {
    m_assignment = assignment;
  }

  public int getFlags() {
    return m_flags;
  }

  public void setFlags(int flags) {
    m_flags = flags;
  }
}
