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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 * <h3>{@link AnnotationSourceBuilder}</h3> ...
 * 
 * @author aho
 * @since 3.10.0 07.03.2013
 */
public class AnnotationSourceBuilder implements IAnnotationSourceBuilder {

  private final String m_signature;
  private final List<String> m_parameters;

  public AnnotationSourceBuilder(String signature) {
    m_signature = signature;
    m_parameters = new ArrayList<String>();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getSignature())) {
      throw new IllegalArgumentException("Signature required!");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    source.append("@" + SignatureUtility.getTypeReference(getSignature(), validator));
    String[] params = getParameters();
    if (params != null && params.length > 0) {
      source.append("(");
      for (int i = 0; i < params.length; i++) {
        source.append(params[i]);
        if (i < (params.length - 1)) {
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
  public String[] getParameters() {
    return m_parameters.toArray(new String[m_parameters.size()]);
  }

}
