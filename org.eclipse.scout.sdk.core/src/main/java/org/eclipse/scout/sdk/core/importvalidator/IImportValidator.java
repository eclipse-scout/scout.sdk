/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.importvalidator;

import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * <h3>{@link IImportValidator}</h3> Validates imports to fully qualified references and returns the type reference to
 * use in the source code.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public interface IImportValidator {
  /**
   * Gets the type reference to the given {@link IType}.
   *
   * @param type
   *          The {@link IType} to which a reference should be returned.
   * @return The reference to the given {@link IType} to use in the source code.
   */
  String useType(IType type);

  /**
   * Gets the reference to the given fully qualified names.
   *
   * @param fullyQualifiedNames
   *          The fully qualified names. <br>
   *          E.g. <code>java.lang.Long</code> or <code>java.util.List&lt;java.lang.String&gt;</code>.
   * @return The references to the given names to use in the source code.
   */
  String useName(String fullyQualifiedNames);

  /**
   * Gets the reference to the given signature.
   *
   * @param signature
   *          The fully parameterized signature for which to return the reference.<br>
   *          E.g. <code>Ljava.util.Set&lt;Ljava.lang.String;&gt;;</code>.
   * @return The references to the given signature to use in the source code.
   */
  String useSignature(String signature);

  /**
   * @return The {@link IImportCollector} responsible to collect all used imports.
   */
  IImportCollector getImportCollector();

  /**
   * Sets a new {@link IImportCollector} to this validator.
   * 
   * @param collector
   *          The new {@link IImportCollector} to use.
   */
  void setImportCollector(IImportCollector collector);
}
