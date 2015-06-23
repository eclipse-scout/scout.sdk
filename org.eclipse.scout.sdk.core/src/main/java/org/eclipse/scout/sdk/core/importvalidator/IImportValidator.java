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
package org.eclipse.scout.sdk.core.importvalidator;

import java.util.Set;

/**
 * <h3>{@link IImportValidator}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.7.0
 */
public interface IImportValidator {

  /**
   * To get the scope qualified name to use for this signature.
   *
   * @param singleTypeSignature
   *          a signature e.g. 'Ljava.lang.String;'
   * @return a simple name if the import is not used so far (e.g. String) or the fully qualified name if there is an
   *         other type with the same simple name in the scope (e.g. java.lang.String).
   */
  String getTypeName(String signature);

  /**
   * Gets the list of imports to be created.<br>
   * This list consists of all names added using {@link #addImport(String)} or that have automatically been added using
   * {@link #getTypeName(String)}.
   *
   * @return An array containing all fully qualified names that needs to be created
   */
  Set<String> getImportsToCreate();

  /**
   * Adds the given fully qualified name to the list of imports to be created.<br>
   * Any already existing mapping for the same simple name of the given qualified name will be replaced.
   *
   * @param fqn
   *          The fully qualified name to add.
   */
  void addImport(String fqn);

}
