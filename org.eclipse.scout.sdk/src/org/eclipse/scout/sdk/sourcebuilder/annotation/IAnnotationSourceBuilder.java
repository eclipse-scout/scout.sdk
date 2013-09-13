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
package org.eclipse.scout.sdk.sourcebuilder.annotation;

import org.eclipse.scout.sdk.sourcebuilder.ISourceBuilder;

/**
 * <h3>{@link IAnnotationSourceBuilder}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 07.03.2013
 */
public interface IAnnotationSourceBuilder extends ISourceBuilder {

  /**
   * @return
   */
  String getSignature();

  /**
   * @param parameter
   * @return
   */
  boolean addParameter(String parameter);

  /**
   * @param parameter
   * @return
   */
  boolean removeParameter(String parameter);

  /**
   * @return
   */
  String[] getParameters();

}
