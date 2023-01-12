/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.util.ast;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * <h3>{@link ITypeBindingVisitor}</h3>
 *
 * @since 5.2.0
 */
@FunctionalInterface
public interface ITypeBindingVisitor {

  /**
   * @param type
   *          a type binding
   * @return {@code true} to continue visiting types, or {@code false} to abort and return {@code false}
   */
  boolean visit(ITypeBinding type);
}
