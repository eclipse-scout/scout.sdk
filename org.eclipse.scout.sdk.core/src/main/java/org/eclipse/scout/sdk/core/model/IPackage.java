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
package org.eclipse.scout.sdk.core.model;

/**
 * <h3>{@link IPackage}</h3> Represents a package
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IPackage {

  /**
   * @return The name of the package or <code>null</code> if it is the default package.
   */
  String getName();

  /**
   * The shared instance representing the default package.
   */
  IPackage DEFAULT_PACKAGE = new IPackage() {
    @Override
    public String getName() {
      return null;
    }
  };

}
