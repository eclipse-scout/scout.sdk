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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link BooleanPropertySourceParser}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 25.02.2013
 */
public class BooleanPropertySourceParser implements IPropertySourceParser<Boolean> {

  @Override
  public Boolean parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    return PropertyMethodSourceUtility.parseReturnParameterBoolean(source, context, superTypeHierarchy);
  }

  @Override
  public String formatSourceValue(Boolean value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    String sourceValue = Boolean.toString(value.booleanValue());
    return sourceValue;
  }
}
