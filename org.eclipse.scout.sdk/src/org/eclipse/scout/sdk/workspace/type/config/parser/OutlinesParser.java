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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link OutlinesParser}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 30.08.2013
 */
public class OutlinesParser implements IPropertySourceParser<List<IType>> {

  @Override
  public String formatSourceValue(List<IType> value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    return OutlineNewOperation.getOutlinesMethodBody(value, importValidator, lineDelimiter);
  }

  @Override
  public List<IType> parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    return ScoutTypeUtility.getTypeOccurenceInMethod(context);
  }
}
