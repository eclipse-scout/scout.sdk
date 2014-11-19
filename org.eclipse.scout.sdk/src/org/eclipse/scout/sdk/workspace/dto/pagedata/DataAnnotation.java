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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;

/**
 * Describes a parsed {@link IRuntimeClasses#Data} or {@link IRuntimeClasses#PageData} annotation.
 *
 * @since 3.10.0-M1
 */
public class DataAnnotation {

  private final String m_dataTypeSignature;
  private final String m_superDataTypeSignature;
  private final IJavaElement m_annotationHolder;

  public DataAnnotation(String dataTypeSignature, String superdataTypeSignature, IJavaElement holder) {
    m_dataTypeSignature = dataTypeSignature;
    m_superDataTypeSignature = superdataTypeSignature;
    m_annotationHolder = holder;
  }

  public String getDataTypeSignature() {
    return m_dataTypeSignature;
  }

  public String getSuperDataTypeSignature() {
    return m_superDataTypeSignature;
  }

  public IJavaElement getAnnotationHolder() {
    return m_annotationHolder;
  }
}
