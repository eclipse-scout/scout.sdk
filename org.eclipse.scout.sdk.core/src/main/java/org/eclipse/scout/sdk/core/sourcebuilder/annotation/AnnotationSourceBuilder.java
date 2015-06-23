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
package org.eclipse.scout.sdk.core.sourcebuilder.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AnnotationSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class AnnotationSourceBuilder implements IAnnotationSourceBuilder {

  private final String m_signature;
  private final List<String> m_parameters;

  public AnnotationSourceBuilder(String signature) {
    m_signature = signature;
    m_parameters = new ArrayList<>();
  }

  @Override
  public void validate() {
    if (StringUtils.isEmpty(getSignature())) {
      throw new IllegalArgumentException("Signature required!");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    source.append("@" + SignatureUtils.getTypeReference(getSignature(), validator));
    if (m_parameters.size() > 0) {
      source.append('(');
      source.append(m_parameters.get(0));
      for (int i = 1; i < m_parameters.size(); i++) {
        source.append(", ");
        source.append(m_parameters.get(i));
      }
      source.append(')');
    }
  }

  @Override
  public String getSignature() {
    return m_signature;
  }

  @Override
  public boolean addParameter(String parameter) {
    return m_parameters.add(parameter);
  }

  @Override
  public boolean removeParameter(String parameter) {
    return m_parameters.remove(parameter);
  }

  @Override
  public List<String> getParameters() {
    return new ArrayList<>(m_parameters);
  }
}
