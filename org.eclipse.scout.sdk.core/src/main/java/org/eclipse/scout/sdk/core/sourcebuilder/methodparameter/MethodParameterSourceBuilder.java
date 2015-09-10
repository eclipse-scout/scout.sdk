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
package org.eclipse.scout.sdk.core.sourcebuilder.methodparameter;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link MethodParameterSourceBuilder}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class MethodParameterSourceBuilder extends AbstractAnnotatableSourceBuilder implements IMethodParameterSourceBuilder {
  private int m_flags;
  private String m_dataTypeSignature;

  public MethodParameterSourceBuilder(IMethodParameter element) {
    super(element);
    setFlags(element.getFlags());
    setDataTypeSignature(SignatureUtils.getTypeSignature(element.getDataType()));
  }

  /**
   * @param elementName
   */
  public MethodParameterSourceBuilder(String elementName, String dataTypeSignature) {
    super(elementName);
    setDataTypeSignature(dataTypeSignature);
  }

  @Override
  public int getFlags() {
    return m_flags;
  }

  @Override
  public void setFlags(int flags) {
    m_flags = flags;
  }

  @Override
  public String getDataTypeSignature() {
    return m_dataTypeSignature;
  }

  @Override
  public void setDataTypeSignature(String dataTypeSignature) {
    m_dataTypeSignature = dataTypeSignature;
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
    if (m_flags != Flags.AccDefault) {
      source.append(Flags.toString(m_flags)).append(" ");
    }
    source.append(SignatureUtils.useSignature(m_dataTypeSignature, validator)).append(" ");
    source.append(getElementName());
  }

}
