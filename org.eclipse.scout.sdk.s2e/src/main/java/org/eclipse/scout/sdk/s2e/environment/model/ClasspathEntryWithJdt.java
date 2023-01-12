/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.environment.model;

import java.nio.file.Path;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.scout.sdk.core.model.ecj.ClasspathEntry;

/**
 * <h3>{@link ClasspathEntryWithJdt}</h3>
 *
 * @since 7.0.0
 */
@SuppressWarnings("squid:S2160") // no need to override equals() and hashCode() because the root is not required
public class ClasspathEntryWithJdt extends ClasspathEntry {

  private final IPackageFragmentRoot m_root;

  protected ClasspathEntryWithJdt(IPackageFragmentRoot root, Path classpath, int mode, String encoding) {
    super(classpath, mode, encoding);
    m_root = root;
  }

  public IPackageFragmentRoot getRoot() {
    return m_root;
  }
}
