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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AbstractJavaElementSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public abstract class AbstractJavaElementSourceBuilder implements IJavaElementSourceBuilder {

  private final String m_elementName;
  private ICommentSourceBuilder m_commentSourceBuilder;

  public AbstractJavaElementSourceBuilder(String elementName) {
    m_elementName = elementName;
  }

  @Override
  public void validate() {
    if (StringUtils.isEmpty(getElementName())) {
      throw new IllegalArgumentException("element name is null or empty!");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    // comment
    createComment(source, lineDelimiter, context, validator);
  }

  protected void createComment(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    if (getCommentSourceBuilder() != null) {
      getCommentSourceBuilder().createSource(this, source, lineDelimiter, context, validator);
      source.append(lineDelimiter);
    }
  }

  @Override
  public String getElementName() {
    return m_elementName;
  }

  public void setCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder) {
    m_commentSourceBuilder = commentSourceBuilder;
  }

  @Override
  public ICommentSourceBuilder getCommentSourceBuilder() {
    return m_commentSourceBuilder;
  }

}
