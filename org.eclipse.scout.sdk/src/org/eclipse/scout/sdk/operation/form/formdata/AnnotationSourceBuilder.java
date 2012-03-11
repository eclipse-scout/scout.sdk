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
 * <h3>{@link AnnotationSourceBuilder}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.02.2011
 */
public class AnnotationSourceBuilder implements ISourceBuilder {

  private String m_annotationSignature;

  public AnnotationSourceBuilder() {

  }

  @Override
  public int getType() {
    return ANNOTATION_SOURCE_BUILDER;
  }

  public AnnotationSourceBuilder(String annotationSignature) {
    m_annotationSignature = annotationSignature;
  }

  @Override
  public String createSource(IImportValidator validator) {
    return "@" + validator.getTypeName(getAnnotationSignature());
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
