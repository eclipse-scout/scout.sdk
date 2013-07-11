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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.scout.commons.CompositeObject;

/**
 *
 */
public interface ITypeSourceBuilder extends ISourceBuilder {

  public static final int CATEGORY_METHOD_PROPERTY = 0;
  public static final int CATEGORY_METHOD_FIELD_GETTER = 2;
  public static final int CATEGORY_MEHTOD = 3;
  public static final int CATEGORY_TYPE_PROPERTY = 4;
  public static final int CATEGORY_TYPE_FIELD = 5;
  public static final int CATEGORY_TYPE_TABLE_COLUMN = 6;
  public static final int CATEGORY_TYPE_COMPOSER_ATTRIBUTE = 7;
  public static final int CATEGORY_TYPE_COMPOSER_ENTITY = 8;

  /**
   * @param builder
   * @param category
   */
  void addBuilder(ISourceBuilder builder, int category);

  /**
   * @param builder
   * @param key
   */
  void addBuilder(ISourceBuilder builder, CompositeObject key);

  ISourceBuilder[] getSourceBuilders(int type);

  /**
   * @param superTypeSignature
   */
  void setSuperTypeSignature(String superTypeSignature);

  /**
   * @param elementName
   */
  void setElementName(String elementName);

  /**
   * @param flags
   */
  void setFlags(int flags);

  /**
   * @param annotation
   */
  void addAnnotation(AnnotationSourceBuilder annotation);

  /**
   * @return
   */
  AnnotationSourceBuilder[] getAnnotations();

}
