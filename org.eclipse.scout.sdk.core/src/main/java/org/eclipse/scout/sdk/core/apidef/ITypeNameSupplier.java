/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.apidef;

import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * Represents an object having a fully qualified class name.
 */
@FunctionalInterface
public interface ITypeNameSupplier {

  /**
   * @return The fully qualified name of this type.
   */
  String fqn();

  /**
   * @return The simple name of this type.
   */
  default String simpleName() {
    return JavaTypes.simpleName(fqn());
  }

  /**
   * Creates a {@link ITypeNameSupplier} from a fully qualified {@link CharSequence}.
   * 
   * @param fqn
   *          The fully qualified type name. May be {@code null}.
   * @return A {@link ITypeNameSupplier} using the fqn provided as fully qualified class name.
   */
  static ITypeNameSupplier of(CharSequence fqn) {
    var name = fqn == null ? null : fqn.toString();
    return () -> name;
  }
}
