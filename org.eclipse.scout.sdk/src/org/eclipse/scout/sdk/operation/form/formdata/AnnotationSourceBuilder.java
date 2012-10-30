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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 * <h3>{@link AnnotationSourceBuilder}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.02.2011
 */
public class AnnotationSourceBuilder implements ISourceBuilder {

  private String m_annotationSignature;
  private final List<String> m_parameters;

  public AnnotationSourceBuilder(String annotationSignature) {
    m_annotationSignature = annotationSignature;
    m_parameters = new ArrayList<String>();
  }

  @Override
  public int getType() {
    return ANNOTATION_SOURCE_BUILDER;
  }

  @Override
  public String createSource(IImportValidator validator) throws JavaModelException {
    StringBuilder source = new StringBuilder();
    source.append("@" + SignatureUtility.getTypeReference(getAnnotationSignature(), validator));
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
    return source.toString();
  }

  public void addParameter(String parameter) {
    m_parameters.add(parameter);
  }

  public String[] getParameters() {
    return m_parameters.toArray(new String[m_parameters.size()]);
  }

  @Override
  public String getElementName() {
    return getAnnotationSignature();
  }

  /**
   * @return the annotationSignature
   */
  public String getAnnotationSignature() {
    return m_annotationSignature;
  }

  /**
   * @param annotationSignature
   *          the annotationSignature to set
   */
  public void setAnnotationSignature(String annotationSignature) {
    m_annotationSignature = annotationSignature;
  }
}
