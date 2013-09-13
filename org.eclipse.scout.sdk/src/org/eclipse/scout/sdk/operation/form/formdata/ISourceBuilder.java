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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.signature.IImportValidator;

/**
 *
 */
public interface ISourceBuilder {
  public static final int TYPE_SOURCE_BUILDER = 1;
  public static final int METHOD_SOURCE_BUILDER = 2;
  public static final int ANNOTATION_SOURCE_BUILDER = 3;
  public static final int CONSTANT_INT_SOURCE_BUILDER = 3;
  public static final int PROPERTY_SOURCE_BUILDER = 5;

  String getElementName();

  String createSource(IImportValidator validator, String lineDelimiter) throws JavaModelException;

  int getType();

}
