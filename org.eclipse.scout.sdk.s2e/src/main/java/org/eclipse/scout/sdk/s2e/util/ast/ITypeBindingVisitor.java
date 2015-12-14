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
package org.eclipse.scout.sdk.s2e.util.ast;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * <h3>{@link ITypeBindingVisitor}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public interface ITypeBindingVisitor {

  /**
   * @param type
   *          a type binding
   * @return <code>true</code> to continue visiting types, or <code>false</code> to abort and return <code>false</code>
   */
  boolean visit(ITypeBinding type);
}
