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

import java.util.Map;

/**
 * <h3>{@link ITypeGenericMapping}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.9.0 04.04.2013
 */
public interface ITypeGenericMapping {

  /**
   * @return The fully qualified name of the class the generic mappings belong to.
   */
  String getFullyQualifiedName();

  /**
   * Gets the signature of the generic parameter with the given name.
   *
   * @param paramName
   *          The name of the generic parameter (e.g. "T" or "VALUE_TYPE")
   * @return The signature of the generic type with the given name or null if the class that belongs to this instance
   *         does not define a generic type with the given name.
   */
  String getParameterSignature(String paramName);

  /**
   * Gets a unmodifiable map of all generic types with their corresponding signature.
   *
   * @return The generic type to signature mapping.
   */
  Map<String, String> getParameters();

  /**
   * Gets the parameter at the specified index.
   *
   * @param index
   *          The infex of the paramter to return
   * @return An array of size 2 or null. The array contains the name of the parameter at position 0 and the signature
   *         value at position 1.
   */
  String[] getParameter(int index);

  /**
   * @return The number of parameters in this mapping
   */
  int getParameterCount();

}
