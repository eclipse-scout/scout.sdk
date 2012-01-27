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

import org.eclipse.scout.sdk.util.signature.IImportValidator;

/**
 * Simple source builder to add an integer (int primitive type) constant.
 * 
 * @since 29.09.2011
 */
public class ConstantIntSourceBuilder implements ISourceBuilder {
  private int m_constantValue;
  private String m_elementName;

  @Override
  public String createSource(IImportValidator validator) {
    //int is native, nothing to validate.
    return "public static final int " + getElementName() + " = " + getConstantValue() + ";";
  }

  @Override
  public int getType() {
    return CONSTANT_INT_SOURCE_BUILDER;
  }

  @Override
  public String getElementName() {
    return m_elementName;
  }

  public void setElementName(String elementName) {
    m_elementName = elementName;
  }

  public int getConstantValue() {
    return m_constantValue;
  }

  public void setConstantValue(int constantValue) {
    m_constantValue = constantValue;
  }
}
