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
package org.eclipse.scout.sdk.core.sourcebuilder.typeparameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractJavaElementSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link TypeParameterSourceBuilder}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class TypeParameterSourceBuilder extends AbstractJavaElementSourceBuilder implements ITypeParameterSourceBuilder {

  private final List<String> m_boundsSignatures = new ArrayList<>();

  public TypeParameterSourceBuilder(ITypeParameter element) {
    super(element);
    for (IType t : element.bounds()) {
      addBoundSignature(SignatureUtils.getTypeSignature(t));
    }
  }

  /**
   * @param elementName
   */
  public TypeParameterSourceBuilder(String elementName) {
    super(elementName);
  }

  @Override
  public List<String> getBoundsSignatures() {
    return Collections.unmodifiableList(m_boundsSignatures);
  }

  @Override
  public void addBoundSignature(String boundSignature) {
    if (boundSignature != null) {
      m_boundsSignatures.add(boundSignature);
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
    if (StringUtils.isNotBlank(getElementName())) {
      source.append(getElementName());
    }
    else if (!m_boundsSignatures.isEmpty()) {
      source.append('?');
    }

    if (!m_boundsSignatures.isEmpty()) {
      source.append(" extends ");
      for (String sig : m_boundsSignatures) {
        source.append(validator.useSignature(sig));
        source.append(" & ");
      }
      source.setLength(source.length() - 3);
    }
  }

}
