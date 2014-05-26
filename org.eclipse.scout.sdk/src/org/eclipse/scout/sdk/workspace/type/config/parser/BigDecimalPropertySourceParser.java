/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.type.config.parser;

import java.math.BigDecimal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link BigDecimalPropertySourceParser}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 02.12.2013
 */
public class BigDecimalPropertySourceParser implements IPropertySourceParser<BigDecimal> {

  @Override
  public BigDecimal parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    BigDecimal bd = PropertyMethodSourceUtility.parseReturnParameterBigDecimal(source);
    return bd;
  }

  @Override
  public String formatSourceValue(BigDecimal value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    if (value == null) {
      return "null";
    }
    return "new " + importValidator.getTypeName(SignatureCache.createTypeSignature(BigDecimal.class.getName())) + "(\"" + value.toPlainString() + "\")";
  }
}
