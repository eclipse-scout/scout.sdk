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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link OutlinesParser}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 30.08.2013
 */
public class OutlinesParser implements IPropertySourceParser<IType[]> {

  @Override
  public String formatSourceValue(IType[] value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    StringBuilder sourceBuilder = new StringBuilder();
    sourceBuilder.append("new Class[]{");
    if (value.length > 0) {
      for (int i = 0; i < value.length; i++) {
        sourceBuilder.append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature((value[i]).getFullyQualifiedName()), importValidator)).append(".class");
        if (i < (value.length - 1)) {
          sourceBuilder.append(",").append(lineDelimiter).append("  ");
        }
      }
    }
    sourceBuilder.append(lineDelimiter).append("}");
    return sourceBuilder.toString();
  }

  @Override
  public IType[] parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    return ScoutTypeUtility.getTypeOccurenceInMethod(context);
  }
}
