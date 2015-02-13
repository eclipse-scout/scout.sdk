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
package org.eclipse.scout.sdk.sourcebuilder.annotation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

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
    if (StringUtility.isNullOrEmpty(getSignature())) {
      throw new IllegalArgumentException("Signature required!");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    source.append("@" + SignatureUtility.getTypeReference(getSignature(), validator));
    if (m_parameters.size() > 0) {
      source.append("(");
      for (int i = 0; i < m_parameters.size(); i++) {
        source.append(m_parameters.get(i));
        if (i < (m_parameters.size() - 1)) {
          source.append(",");
        }
      }
      source.append(")");
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
    return CollectionUtility.arrayList(m_parameters);
  }

}
