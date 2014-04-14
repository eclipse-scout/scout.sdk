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
package org.eclipse.scout.sdk.workspace.type.config.parser;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.scout.sdk.workspace.type.config.property.AbstractSourceProperty;

/**
 * <h3>{@link IntegerSourcePropertyParser}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 06.03.2013
 */
public class IntegerSourcePropertyParser extends AbstractSourcePropertyParser<Integer> {

  /**
   * @param properties
   */
  public IntegerSourcePropertyParser(List<AbstractSourceProperty<Integer>> properties) {
    super(properties);
  }

  @Override
  public Integer getReturnValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    return PropertyMethodSourceUtility.parseReturnParameterInteger(source, context, superTypeHierarchy);
  }
}
