/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.apidef;

import org.eclipse.scout.sdk.core.java.JavaTypes;

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
