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
package org.eclipse.scout.sdk.core.model.spi;

import org.eclipse.scout.sdk.core.model.api.IPackage;

/**
 * <h3>{@link PackageSpi}</h3> Represents a package
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface PackageSpi extends JavaElementSpi {

  /**
   * @return The name of the package or <code>null</code> if it is the default package.
   */
  @Override
  String getElementName();

  @Override
  IPackage wrap();
}
