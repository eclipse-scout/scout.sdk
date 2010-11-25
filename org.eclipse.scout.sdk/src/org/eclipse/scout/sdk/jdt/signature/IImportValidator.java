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
package org.eclipse.scout.sdk.jdt.signature;

import org.eclipse.jdt.core.Signature;

public interface IImportValidator {

  /**
   * to evaluate if an type can be imported and used as simple type name within
   * a context.
   * 
   * @see Signature
   * @param singleTypeSignature
   *          a simple type signature e.g. 'Ljava.lang.String;'
   * @return
   */
  String getSimpleTypeRef(String singleTypeSignature);

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
