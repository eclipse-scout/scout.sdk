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
package org.eclipse.scout.sdk.core.sourcebuilder;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AbstractJavaElementSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public abstract class AbstractJavaElementSourceBuilder implements IJavaElementSourceBuilder {

  private String m_elementName;
  private ISourceBuilder m_comment;

  public AbstractJavaElementSourceBuilder(IJavaElement element) {
    this(element.getElementName());
  }

  public AbstractJavaElementSourceBuilder(String elementName) {
    m_elementName = elementName;
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    if (getElementName() == null) {
      throw new IllegalArgumentException("element name is null!");
    }
    // comment
    createComment(source, lineDelimiter, context, validator);
  }

  protected void createComment(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    if (getComment() != null) {
      getComment().createSource(source, lineDelimiter, context, validator);
      source.append(lineDelimiter);
    }
  }

  @Override
  public void setElementName(String elementName) {
    m_elementName = elementName;
  }

  @Override
  public String getElementName() {
    return m_elementName;
  }

  @Override
  public void setComment(ISourceBuilder commentSourceBuilder) {
    m_comment = commentSourceBuilder;
  }

  @Override
  public ISourceBuilder getComment() {
    return m_comment;
  }

}
