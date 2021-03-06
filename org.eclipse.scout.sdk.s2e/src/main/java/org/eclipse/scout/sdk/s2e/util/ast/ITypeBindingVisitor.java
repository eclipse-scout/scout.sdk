/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
