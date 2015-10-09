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
package org.eclipse.scout.sdk.core.sourcebuilder.annotation;

import java.util.Map;

import org.eclipse.scout.sdk.core.sourcebuilder.ExpressionSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.IJavaElementSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;

/**
 * <h3>{@link IAnnotationSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface IAnnotationSourceBuilder extends IJavaElementSourceBuilder {

  /**
   * @return the fully qualified name
   */
  String getName();

  /**
   * see {@link ExpressionSourceBuilderFactory}
   * 
   * @return this
   */
  IAnnotationSourceBuilder putElement(String name, String value);

  /**
   * see {@link ExpressionSourceBuilderFactory}
   *
   * @param name
   * @param value
   * @return this
   */
  IAnnotationSourceBuilder putElement(String name, ISourceBuilder value);

  ISourceBuilder getElement(String name);

  Map<String, ISourceBuilder> getElements();

  boolean removeElement(String name);

}
