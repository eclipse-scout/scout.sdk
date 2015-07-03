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
package org.eclipse.scout.sdk.s2e.id;

import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link IUniqueIdProvider}</h3>
 *
 * @author Provider for unique Ids
 * @since 5.1.0
 */
public interface IUniqueIdProvider {
  /**
   * Gets the next id from the provider.
   *
   * @param context
   *          Properties describing the calling context.
   * @param genericSignature
   *          The signature describing the requested data type. The resulting {@link String} must be valid for the given
   *          data type.
   * @return The next id or <code>null</code> if the provider is unable to create an id for the given input.
   */
  String getNextId(PropertyMap context, String genericSignature);
}
