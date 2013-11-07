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
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link StringPropertySourceParser}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 27.02.2013
 */
public class StringPropertySourceParser implements IPropertySourceParser<String> {

  @Override
  public String parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    String value = PropertyMethodSourceUtility.parseReturnParameterString(source, context, superTypeHierarchy);
    if (value == null) {
      value = "";
    }
    return value;
  }

  @Override
  public String formatSourceValue(String value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    return JdtUtility.toStringLiteral(value);
  }
}
