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
package org.eclipse.scout.sdk.core.sourcebuilder.field;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link FieldSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class FieldSourceBuilder extends AbstractAnnotatableSourceBuilder implements IFieldSourceBuilder {

  private String m_signature;
  private String m_value;

  /**
   * @param elementName
   */
  public FieldSourceBuilder(String fieldName) {
    super(fieldName);
  }

  @Override
  public void validate() {
    super.validate();
    if (StringUtils.isEmpty(getSignature())) {
      throw new IllegalArgumentException("signature is null!");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
    source.append(Flags.toString(getFlags())).append(" ");
    // field type
    source.append(SignatureUtils.getTypeReference(getSignature(), validator)).append(" ");
    source.append(getElementName());
    // init value
    createInitValue(source, lineDelimiter, context, validator);
    source.append(";");
  }

  /**
   * can be overridden to assign a specific value to the field. Use
   * {@link SignatureUtils#getTypeReference(String, IImportValidator)} to determ class references (fully qualified vs.
   * simple name).
   */
  protected void createInitValue(StringBuilder sourceBuilder, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    if (!StringUtils.isEmpty(getValue())) {
      sourceBuilder.append(" = ").append(getValue());
    }
  }

  @Override
  public void setSignature(String signature) {
    m_signature = signature;
  }

  @Override
  public String getSignature() {
    return m_signature;
  }

  @Override
  public void setValue(String value) {
    m_value = value;
  }

  @Override
  public String getValue() {
    return m_value;
  }
}
