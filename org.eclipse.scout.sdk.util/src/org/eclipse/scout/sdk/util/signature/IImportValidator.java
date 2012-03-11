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
package org.eclipse.scout.sdk.util.signature;

import org.eclipse.jdt.core.Signature;

public interface IImportValidator {

  /**
   * To get the scope qualified name to use for this signature.
   * 
   * @see Signature
   * @param singleTypeSignature
   *          a simple type signature e.g. 'Ljava.lang.String;'
   * @return a simple name if the import is not used so far (e.g. String) or the fully qualified name if there is an
   *         other type with the same simple name in the scope (e.g. java.lang.String).
   */
  String getTypeName(String singleTypeSignature);

  /**
   * @return the imports has to be created of this validator
   */
  String[] getImportsToCreate();

  /**
   * @param fqn
   *          fully qualified type name
   */
  void addImport(String fqn);

}
