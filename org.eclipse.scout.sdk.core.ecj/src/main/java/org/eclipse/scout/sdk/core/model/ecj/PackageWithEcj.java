/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.PackageImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;

/**
 *
 */
public class PackageWithEcj extends AbstractJavaElementWithEcj<IPackage> implements PackageSpi {
  private final String m_name;

  protected PackageWithEcj(AbstractJavaEnvironment env, String name) {
    super(env);
    m_name = name;
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    return getJavaEnvironment().getPackage(getElementName());
  }

  @Override
  protected IPackage internalCreateApi() {
    return new PackageImplementor(this);
  }

  @Override
  public String getElementName() {
    return m_name;
  }

  @Override
  public ISourceRange getSource() {
    return null;
  }
}
