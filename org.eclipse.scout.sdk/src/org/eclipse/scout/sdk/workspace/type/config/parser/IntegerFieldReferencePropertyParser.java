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
import org.eclipse.scout.sdk.workspace.type.config.property.FieldProperty;

/**
 * <h3>{@link IntegerFieldReferencePropertyParser}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 01.03.2013
 */
public class IntegerFieldReferencePropertyParser extends FieldReferencePropertyParser<Integer> {

  /**
   * @param properties
   */
  public IntegerFieldReferencePropertyParser(List<FieldProperty<Integer>> properties) {
    super(properties);
  }

  @Override
  public Integer getReturnValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    return PropertyMethodSourceUtility.parseReturnParameterInteger(source, context, superTypeHierarchy);
  }
}
