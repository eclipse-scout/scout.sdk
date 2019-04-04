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

import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link DataAnnotationDescriptor}</h3> Descriptor holding all meta data of a {@link IScoutRuntimeTypes#Data} or
 * {@link IScoutRuntimeTypes#PageData} annotation.
 *
 * @since @since 3.10.0-M1
 */
public class DataAnnotationDescriptor {

  private final IType m_dataType;
  private final IType m_superDataType;
  private final IType m_annotationHolder;

  protected DataAnnotationDescriptor(IType dataType, IType superdataType, IType holder) {
    m_dataType = dataType;
    m_superDataType = superdataType;
    m_annotationHolder = holder;
  }

  /**
   * Parses the possible available {@link IScoutRuntimeTypes#PageData} or {@link IScoutRuntimeTypes#Data} annotation on
   * the given type. If the type is not annotated, the returned {@link Optional} will be empty.
   *
   * @since 3.10.0-M1
   */
  public static Optional<DataAnnotationDescriptor> of(IType type) {
    if (type == null) {
      return Optional.empty();
    }

    Optional<IType> dtoType = getDataAnnotationValue(type);
    if (!dtoType.isPresent()) {
      return Optional.empty();
    }

    Optional<IType> superType = Optional.empty();
    Optional<IType> curType = type.superClass();
    while (curType.isPresent()) {
      superType = getDataAnnotationValue(curType.get());
      if (superType.isPresent()) {
        break;
      }
      curType = curType.get().superClass();
    }

    return Optional.of(new DataAnnotationDescriptor(dtoType.get(), superType.orElse(null), type));
  }

  /**
   * Checks whether the given owner is annotated with a {@link IScoutRuntimeTypes#Data} annotation and if so, this
   * method returns its {@code value()} as {@link IType}.
   *
   * @since 3.10.0-M1
   */
  private static Optional<IType> getDataAnnotationValue(IAnnotatable owner) {
    Optional<IType> dataType = DataAnnotation.valueOf(owner);
    if (dataType.isPresent()) {
      return dataType;
    }

    // fall back to legacy name:
    return owner.annotations()
        .withName(IScoutRuntimeTypes.PageData)
        .first()
        .flatMap(annotation -> annotation.element("value"))
        .map(element -> element.value().as(IType.class));
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
  public Optional<IType> getSuperDataType() {
    return Optional.ofNullable(m_superDataType);
  }

  /**
   * @return The holder of the @Data annotation (e.g. PersonPage)
   */
  public IType getAnnotationHolder() {
    return m_annotationHolder;
  }
}
