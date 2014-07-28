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

import org.eclipse.jdt.core.dom.SimpleName;

/**
 * <h3>{@link ISimpleNameAstFlattenerCallback}</h3> Rewrite strategy for {@link SimpleName} AST nodes.
 *
 * @author Matthias Villiger
 * @since 3.10.0 23.01.2014
 */
public interface ISimpleNameAstFlattenerCallback {
  /**
   * Rewrites the given node.
   *
   * @param node
   *          The {@link SimpleName} to rewrite
   * @param buffer
   *          The buffer that holds the rewritten equivalent of the given node.
   * @return true if the rewrite has been done. False if the callback did no rewrite and the default string equivalent
   *         should be applied instead.
   */
  boolean rewriteElement(SimpleName node, StringBuffer buffer);
}
