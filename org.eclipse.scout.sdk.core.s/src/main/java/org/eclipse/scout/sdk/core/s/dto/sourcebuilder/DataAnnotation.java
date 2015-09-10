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
package org.eclipse.scout.sdk.core.s.dto.sourcebuilder;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;

/**
 * Describes a parsed {@link IRuntimeClasses#Data} or {@link IRuntimeClasses#PageData} annotation.
 *
 * @since 3.10.0-M1
 */
public class DataAnnotation {

  private final IType m_dataType;
  private final IType m_superDataType;
  private final IAnnotatable m_annotationHolder;

  public DataAnnotation(IType dataType, IType superdataType, IAnnotatable holder) {
    m_dataType = dataType;
    m_superDataType = superdataType;
    m_annotationHolder = holder;
  }

  public IType getDataType() {
    return m_dataType;
  }

  public IType getSuperDataType() {
    return m_superDataType;
  }

  public IAnnotatable getAnnotationHolder() {
    return m_annotationHolder;
  }
}
