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
package org.eclipse.scout.sdk.core.sourcebuilder.field;

import org.eclipse.scout.sdk.core.sourcebuilder.IAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;

/**
 * <h3>{@link IFieldSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface IFieldSourceBuilder extends IAnnotatableSourceBuilder {

  /**
   * @return
   */
  String getSignature();

  /**
   * @return
   */
  String getValue();

  /**
   * @param commentSourceBuilder
   */
  void setCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder);

  /**
   * @param flags
   */
  void setFlags(int flags);

  /**
   * @return
   */
  int getFlags();

  /**
   * @param builder
   */
  void addAnnotationSourceBuilder(IAnnotationSourceBuilder builder);

  /**
   * @param signature
   */
  void setSignature(String signature);

  /**
   * @param value
   */
  void setValue(String value);

  /**
   * @param sortKey
   * @param builder
   */
  void addSortedAnnotationSourceBuilder(CompositeObject sortKey, IAnnotationSourceBuilder builder);

  /**
   * @param childOp
   * @return
   */
  boolean removeAnnotationSourceBuilder(IAnnotationSourceBuilder childOp);

}
