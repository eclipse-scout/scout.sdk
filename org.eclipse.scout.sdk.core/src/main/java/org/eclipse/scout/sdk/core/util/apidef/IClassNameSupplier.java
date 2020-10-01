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
package org.eclipse.scout.sdk.core.util.apidef;

import org.eclipse.scout.sdk.core.util.JavaTypes;

@FunctionalInterface
public interface IClassNameSupplier {

  /**
   * @return The fully qualified name of this class.
   */
  String fqn();

  default String simpleName() {
    return JavaTypes.simpleName(fqn());
  }

  static IClassNameSupplier raw(CharSequence fqn) {
    String name = fqn == null ? null : fqn.toString();
    return () -> name;
  }
}
