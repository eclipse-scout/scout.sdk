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
package org.eclipse.scout.sdk.sourcebuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.signature.IImportValidator;

/**
 * <h3>{@link AbstractJavaElementSourceBuilder}</h3> ...
 * 
 *  @author Andreas Hoegger
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
    if (StringUtility.isNullOrEmpty(getElementName())) {
      throw new IllegalArgumentException("element name is null or empty!");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    // comment
    createComment(source, lineDelimiter, ownerProject, validator);
  }

  protected void createComment(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    if (getCommentSourceBuilder() != null) {
      try {
        getCommentSourceBuilder().createSource(this, source, lineDelimiter, ownerProject, validator);
        source.append(lineDelimiter);
      }
      catch (Exception e) {
        ScoutSdk.logError("Could not create type comment for '" + getElementName() + "'.", e);
      }
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
