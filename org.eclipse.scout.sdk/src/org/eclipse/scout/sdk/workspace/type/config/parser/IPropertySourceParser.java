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

/**
 * <h3>{@link IPropertySourceParser}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 25.02.2013
 */
public interface IPropertySourceParser<T> {

  /**
   * Parses the value out of the source input. The source input is a reference or a simple value.
   * 
   * @param source
   *          e.g. "astring" or 12 or Integer.MAX_VALUE
   * @param context
   *          the method context of the source. The context is used to resolve referenced values.
   * @param superTypeHierarchy
   * @return the value parsed from source.
   * @throws CoreException
   */
  T parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException;

  String formatSourceValue(T value, String lineDelimiter, IImportValidator importValidator) throws CoreException;

}
