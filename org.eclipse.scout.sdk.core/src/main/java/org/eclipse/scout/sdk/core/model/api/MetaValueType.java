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
package org.eclipse.scout.sdk.core.model.api;

/**
 * <h3>{@link MetaValueType}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public enum MetaValueType {
  /**
   * {@link IMetaValue#getObject()} is a {@link Character}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  Char,
  /**
   * {@link IMetaValue#getObject()} is a {@link Byte}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  Byte,
  /**
   * {@link IMetaValue#getObject()} is a {@link Integer}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  Int,
  /**
   * {@link IMetaValue#getObject()} is a {@link Short}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  Short,
  /**
   * {@link IMetaValue#getObject()} is a {@link Boolean}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  Bool,
  /**
   * {@link IMetaValue#getObject()} is a {@link Long}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  Long,
  /**
   * {@link IMetaValue#getObject()} is a {@link Double}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  Double,
  /**
   * {@link IMetaValue#getObject()} is a {@link Float}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  Float,
  /**
   * {@link IMetaValue#getObject()} is a {@link Float}
   * <p>
   * The meta value itself is a {@link IMetaValue}
   */
  String,
  /**
   * {@link IMetaValue#getObject()} is a {@link IType}
   */
  Type,
  /**
   * {@link IMetaValue#getObject()} is a {@link IField}
   */
  Enum,
  /**
   * {@link IMetaValue#getObject()} is a {@link IAnnotation}
   */
  Annotation,
  /**
   * {@link IMetaValue#getObject()} is a primitive array int[] or a typed object array such as String[]
   * <p>
   * The meta value itself is a {@link IArrayMetaValue}
   */
  Array,
  /**
   * note that annotation values are never null, field initializers may be null
   */
  Null,
  Unknown;
}
