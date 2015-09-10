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
package org.eclipse.scout.sdk.core.model.api.internal;

import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;

/**
 *
 */
public class PackageImplementor extends AbstractJavaElementImplementor<PackageSpi>implements IPackage {

  public PackageImplementor(PackageSpi spi) {
    super(spi);
  }

  @Override
  public String getName() {
    return m_spi.getName();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

}
