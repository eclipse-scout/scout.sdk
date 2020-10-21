/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public interface IClassNameSupplier {

  /**
   * @return The fully qualified name of this class.
   */
  String fqn();

  /**
   * @return The simple name of this class.
   */
  default String simpleName() {
    return JavaTypes.simpleName(fqn());
  }

  /**
   * Creates a {@link IClassNameSupplier} from a fully qualified {@link CharSequence}.
   * 
   * @param fqn
   *          The fully qualified class name. May be {@code null}.
   * @return A {@link IClassNameSupplier} using the fqn provided as fully qualified class name.
   */
  static IClassNameSupplier raw(CharSequence fqn) {
    var name = fqn == null ? null : fqn.toString();
    return () -> name;
  }
}
