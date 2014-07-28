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
package org.eclipse.scout.sdk.workspace.dto.pagedata;

import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;

/**
 * Describes a parsed {@link IRuntimeClasses#PageData} annotation.
 *
 * @since 3.10.0-M1
 */
public class PageDataAnnotation {

  private final String m_pageDataTypeSignature;
  private final String m_superPageDataTypeSignature;

  public PageDataAnnotation(String pageDataTypeSignature, String superPageDataTypeSignature) {
    m_pageDataTypeSignature = pageDataTypeSignature;
    m_superPageDataTypeSignature = superPageDataTypeSignature;
  }

  public String getPageDataTypeSignature() {
    return m_pageDataTypeSignature;
  }

  public String getSuperPageDataTypeSignature() {
    return m_superPageDataTypeSignature;
  }
}
