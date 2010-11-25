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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;

/**
 *
 */
public class FieldSourceBuilder implements ISourceBuilder {

  private String m_signature;
  private String m_elementName;
  private int m_flags;
  private String m_simpleValue;

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
    builder.append(ScoutSdkUtility.getSimpleTypeRefName(getSignature(), validator) + " ");
    builder.append(getElementName());
    String value = createValue(validator);
    if (!StringUtility.isNullOrEmpty(value)) {
      builder.append(" = " + value);
    }
    builder.append(";\n");
    return null;
  }

  protected String createValue(@SuppressWarnings("unused") IImportValidator validator) {
    if (!StringUtility.isNullOrEmpty(getSimpleValue())) {
      return getSimpleValue();
    }
    return null;
  }

  public void setSignature(String signature) {
    m_signature = signature;
  }

  public String getSignature() {
    return m_signature;
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

  public void setSimpleValue(String simpleValue) {
    m_simpleValue = simpleValue;
  }

  private String getSimpleValue() {
    return m_simpleValue;
  }

}
