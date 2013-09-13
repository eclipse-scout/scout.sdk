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
 * <h3>{@link LongPropertySourceParser}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 27.02.2013
 */
public class LongPropertySourceParser implements IPropertySourceParser<Long> {

  @Override
  public Long parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    Long d = PropertyMethodSourceUtility.parseReturnParameterLong(source, context, superTypeHierarchy);
    return d;
  }

  @Override
  public String formatSourceValue(Long value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    if (value == null) {
      return "null";
    }
    else if (value.longValue() == Long.MAX_VALUE) {
      return "Long.MAX_VALUE";
    }
    else if (value.longValue() == Long.MIN_VALUE) {
      return "Long.MIN_VALUE";
    }
    String sourceVal = value.toString();

    return sourceVal + "L";
  }
}
