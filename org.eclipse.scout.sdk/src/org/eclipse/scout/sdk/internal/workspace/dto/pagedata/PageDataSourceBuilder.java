/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.workspace.dto.pagedata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;

/**
 * <h3>{@link PageDataSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 28.08.2013
 */
public class PageDataSourceBuilder extends AbstractTableBeanSourceBuilder {

  private PageDataAnnotation m_pageDataAnnotation;

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public PageDataSourceBuilder(IType modelType, String elementName, PageDataAnnotation pageDataAnnotation, IProgressMonitor monitor) {
    super(modelType, elementName, false, monitor);
    m_pageDataAnnotation = pageDataAnnotation;
    setup(monitor);
  }

  @Override
  protected String computeSuperTypeSignature() throws CoreException {
    return getPageDataAnnotation().getSuperPageDataTypeSignature();
  }

  public PageDataAnnotation getPageDataAnnotation() {
    return m_pageDataAnnotation;
  }
}
