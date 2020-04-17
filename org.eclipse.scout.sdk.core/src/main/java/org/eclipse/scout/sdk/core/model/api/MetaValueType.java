/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

/**
 * <h3>{@link MetaValueType}</h3>
 *
 * @since 5.1.0
 */
@SuppressWarnings("squid:S00115")
public enum MetaValueType {
  /**
   * {@link IMetaValue#as(Class)} is a {@link Character}
   */
  Char,
  /**
   * {@link IMetaValue#as(Class)} is a {@link Byte}
   */
  Byte,
  /**
   * {@link IMetaValue#as(Class)} is a {@link Integer}
   */
  Int,
  /**
   * {@link IMetaValue#as(Class)} is a {@link Short}
   */
  Short,
  /**
   * {@link IMetaValue#as(Class)} is a {@link Boolean}
   */
  Bool,
  /**
   * {@link IMetaValue#as(Class)} is a {@link Long}
   */
  Long,
  /**
   * {@link IMetaValue#as(Class)} is a {@link Double}
   */
  Double,
  /**
   * {@link IMetaValue#as(Class)} is a {@link Float}
   */
  Float,
  /**
   * {@link IMetaValue#as(Class)} is a {@link String}
   */
  String,
  /**
   * {@link IMetaValue#as(Class)} is a {@link IType}
   */
  Type,
  /**
   * {@link IMetaValue#as(Class)} is a {@link IField}
   */
  Enum,
  /**
   * {@link IMetaValue#as(Class)} is a {@link IAnnotation}
   */
  Annotation,
  /**
   * {@link IMetaValue#as(Class)} is a primitive array int[] or a typed object array such as String[] or an
   * {@link IAnnotation} array.
   * <p>
   * The meta value itself is a {@link IArrayMetaValue}
   */
  Array,
  /**
   * Represents a {@code null} value. Note that annotation values are never {@code null}, field initializers may be
   * {@code null}.
   */
  Null,

  Unknown
}
