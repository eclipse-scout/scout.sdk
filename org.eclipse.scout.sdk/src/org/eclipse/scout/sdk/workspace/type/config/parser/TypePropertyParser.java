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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link TypePropertyParser}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.9.0 22.05.2013
 */
public class TypePropertyParser implements IPropertySourceParser<IType> {

  @Override
  public IType parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    IType referedType = PropertyMethodSourceUtility.parseReturnParameterClass(source, context);
    return referedType;
  }

  @Override
  public String formatSourceValue(IType value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    if (value != null) {
      return importValidator.getTypeName(SignatureCache.createTypeSignature(value.getFullyQualifiedName())) + ".class";
    }
    else {
      return "null";
    }
  }

}
