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
 * <h3>{@link ISimpleNameAstFlattenerProviderService}</h3> Service interface for providers of SimpleName AST rewrite
 * flatteners.<br>
 * This service is required to provide more sophisticated flattener algorithms.
 *
 * @author Matthias Villiger
 * @since 3.10.0 23.01.2014
 * @see SimpleName
 * @see IAstRewriteFlattener
 */
public interface ISimpleNameAstFlattenerProviderService {
  /**
   * Creates a new SimpleName AST rewrite flattener
   *
   * @param callback
   *          The callback that handles the rewrite of a {@link SimpleName}
   * @return The created flattener instance
   */
  IAstRewriteFlattener createAstFlattener(ISimpleNameAstFlattenerCallback callback);
}
