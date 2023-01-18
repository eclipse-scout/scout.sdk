/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.annotation;

import java.util.Optional;

import org.eclipse.scout.sdk.core.java.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;

/**
 * <h3>{@link DataAnnotationDescriptor}</h3> Descriptor holding all meta data of a Data or PageData annotation.
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
   * Parses the possible available PageData or Data annotation on the given type. If the type is not annotated, the
   * returned {@link Optional} will be empty.
   */
  public static Optional<DataAnnotationDescriptor> of(IType type) {
    if (type == null) {
      return Optional.empty();
    }

    var dtoType = getDataAnnotationValue(type);
    if (dtoType.isEmpty()) {
      return Optional.empty();
    }

    Optional<IType> superType = Optional.empty();
    var curType = type.superClass();
    while (curType.isPresent()) {
      superType = getDataAnnotationValue(curType.orElseThrow());
      if (superType.isPresent()) {
        break;
      }
      curType = curType.orElseThrow().superClass();
    }

    return Optional.of(new DataAnnotationDescriptor(dtoType.orElseThrow(), superType.orElse(null), type));
  }

  /**
   * Checks whether the given owner is annotated with a Data annotation and if so, this method returns its
   * {@code value()} as {@link IType}.
   */
  private static Optional<IType> getDataAnnotationValue(IAnnotatable owner) {
    var dataType = DataAnnotation.valueOf(owner);
    if (dataType.isPresent()) {
      return dataType;
    }

    // fall back to legacy name:
    var pageDataApi = owner.javaEnvironment().requireApi(IScoutApi.class).PageData();
    return owner.annotations()
        .withName(pageDataApi.fqn())
        .first()
        .flatMap(annotation -> annotation.element(pageDataApi.valueElementName()))
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
