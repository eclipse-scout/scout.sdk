/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import java.util.List;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.model.api.internal.PackageImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.java.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.Strings;

public class PackageWithEcj extends AbstractJavaElementWithEcj<IPackage> implements PackageSpi {
  private final String m_name;
  private final FinalValue<TypeSpi> m_packageInfo;

  protected PackageWithEcj(AbstractJavaEnvironment env, String name) {
    super(env);
    m_name = name;
    m_packageInfo = new FinalValue<>();
  }

  @Override
  public PackageSpi internalFindNewElement() {
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
  public SourceRange getSource() {
    return null;
  }

  @Override
  public TypeSpi getPackageInfo() {
    return m_packageInfo.computeIfAbsentAndGet(() -> getJavaEnvironment().findType(getElementName() + JavaTypes.C_DOT + JavaTypes.PackageInfo));
  }

  @Override
  public PackageSpi getParentPackage() {
    if (Strings.isBlank(m_name)) {
      // this is the default package. it has no parent package.
      return null;
    }
    var lastDot = m_name.lastIndexOf(JavaTypes.C_DOT);
    if (lastDot < 0) {
      // the default package is the parent
      return javaEnvWithEcj().createDefaultPackage();
    }
    return javaEnvWithEcj().createPackage(m_name.substring(0, lastDot));
  }

  @Override
  public List<? extends AnnotationSpi> getAnnotations() {
    // the AnnotationQuery retrieves package annotations from the package-info type. Use #getPackageInfo.
    // the PackageSpi must therefore not implement this method.
    throw new UnsupportedOperationException();
  }
}
