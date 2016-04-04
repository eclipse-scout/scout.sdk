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
package org.eclipse.scout.sdk.core.sourcebuilder.field;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractMemberSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ExpressionSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link FieldSourceBuilder}</h3> static section in type is the field with element name "" and type null
 *
 * @author Andreas Hoegger
 * @since 3.10.0 2013-03-07
 */
public class FieldSourceBuilder extends AbstractMemberSourceBuilder implements IFieldSourceBuilder {

  private String m_signature;
  private ISourceBuilder m_value;

  /**
   * @param elementName
   */
  public FieldSourceBuilder(IField element) {
    super(element);
    setSignature(SignatureUtils.getTypeSignature(element.dataType()));
    if (element.sourceOfInitializer().isAvailable()) {
      setValue(new RawSourceBuilder(element.sourceOfInitializer().toString()));
    }
    else if (element.constantValue() != null) {
      setValue(ExpressionSourceBuilderFactory.createFromMetaValue(element.constantValue()));
    }
  }

  /**
   * @param elementName
   */
  public FieldSourceBuilder(String elementName) {
    super(elementName);
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);

    if (!getElementName().isEmpty() && StringUtils.isEmpty(getSignature())) {
      throw new IllegalArgumentException("signature is null!");
    }

    if (getElementName().isEmpty()) {
      // for a static constructor
      getValue().createSource(source, lineDelimiter, context, validator);
      return;
    }

    source.append(Flags.toString(getFlags()));
    if (getFlags() != 0) {
      source.append(' ');
    }
    // field type
    source.append(validator.useSignature(getSignature())).append(' ');
    source.append(getElementName());
    // init value
    if (getValue() != null) {
      source.append(" = ");
      getValue().createSource(source, lineDelimiter, context, validator);
    }
    source.append(';');
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
  public void setValue(ISourceBuilder value) {
    m_value = value;
  }

  @Override
  public ISourceBuilder getValue() {
    return m_value;
  }
}
