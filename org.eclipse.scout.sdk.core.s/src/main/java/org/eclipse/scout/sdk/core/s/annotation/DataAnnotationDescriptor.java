/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.annotation;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link DataAnnotationDescriptor}</h3> Descriptor holding all meta data of a {@link IScoutRuntimeTypes#Data} or
 * {@link IScoutRuntimeTypes#PageData} annotation.
 *
 * @author Matthias Villiger
 * @since @since 3.10.0-M1
 */
public class DataAnnotationDescriptor {

  private final IType m_dataType;
  private final IType m_superDataType;
  private final IAnnotatable m_annotationHolder;

  public DataAnnotationDescriptor(IType dataType, IType superdataType, IAnnotatable holder) {
    m_dataType = dataType;
    m_superDataType = superdataType;
    m_annotationHolder = holder;
  }

  /**
   * @return The DTO class this annotation references (e.g. PersonPageData)
   */
  public IType getDataType() {
    return m_dataType;
  }

  /**
   * @return The DTO super class as defined by a model super class having a @Data annotation (e.g.
   *         AbstractPersonPageData).
   */
  public IType getSuperDataType() {
    return m_superDataType;
  }

  /**
   * @return The holder of the @Data annotation (e.g. PersonPage)
   */
  public IAnnotatable getAnnotationHolder() {
    return m_annotationHolder;
  }
}
