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
package org.eclipse.scout.sdk.operation.form.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;

public interface ISourceBuilder {

  void addInnerClass(String classSource);

  /**
   * add a field (java field like a string, number etc)
   */
  void addField(String field);

  /**
   * add a getter or setter for a bean property
   */
  void addPropertyMethod(String method);

  /**
   * add a getter for an internal form data field
   */
  void addFieldGetterMethod(String method);

  /**
   * setup structure of source
   */
  void build() throws CoreException;

  /**
   * append to parent container
   */
  void appendToParent(ISourceBuilder parentBuilder) throws CoreException;

  Document createDocument();

  IImportValidator getImportValidator();
}
