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
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link IntegerPropertySourceParser}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 27.02.2013
 */
public class IntegerPropertySourceParser implements IPropertySourceParser<Integer> {

  @Override
  public Integer parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    Integer d = PropertyMethodSourceUtility.parseReturnParameterInteger(source, context, superTypeHierarchy);
    return d;
  }

  @Override
  public String formatSourceValue(Integer value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    if (value == null) {
      return "null";
    }
    else if (value.intValue() == Integer.MAX_VALUE) {
      return "Integer.MAX_VALUE";
    }
    else if (value.intValue() == -Integer.MAX_VALUE) {
      return "-Integer.MAX_VALUE";
    }
    return value.toString();
  }
}
