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

import java.math.RoundingMode;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link RoundingModePropertyParser}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 18.12.2013
 */
public class RoundingModePropertyParser implements IPropertySourceParser<RoundingMode> {

  @Override
  public RoundingMode parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    return PropertyMethodSourceUtility.parseReturnParameterRoundingMode(source);
  }

  @Override
  public String formatSourceValue(RoundingMode value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    if (value == null) {
      return "null";
    }
    String ref = importValidator.getTypeName(SignatureCache.createTypeSignature(RoundingMode.class.getName()));
    return ref + "." + value.toString();
  }
}
