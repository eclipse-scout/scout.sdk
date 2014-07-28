/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.method;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * <h3>{@link IAstRewriteFlattener}</h3> AST flattener that can rewrite AST nodes while flattening.
 *
 * @author Matthias Villiger
 * @since 3.10.0 23.01.2014
 * @see ISimpleNameAstFlattenerCallback
 * @see ISimpleNameAstFlattenerProviderService
 */
public interface IAstRewriteFlattener {
  /**
   * Flattens the given node recursively allowing to rewrite some nodes if required.
   *
   * @param n
   *          The node to rewrite.
   * @return The string equivalent for the given node with all rewrites applied.
   */
  String rewrite(ASTNode n);
}
