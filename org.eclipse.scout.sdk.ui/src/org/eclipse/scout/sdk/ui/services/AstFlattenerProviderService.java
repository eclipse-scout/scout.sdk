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
package org.eclipse.scout.sdk.ui.services;

import org.eclipse.scout.sdk.util.method.IAstRewriteFlattener;
import org.eclipse.scout.sdk.util.method.ISimpleNameAstFlattenerCallback;
import org.eclipse.scout.sdk.util.method.ISimpleNameAstFlattenerProviderService;

/**
 * <h3>{@link AstFlattenerProviderService}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 23.01.2014
 */
public class AstFlattenerProviderService implements ISimpleNameAstFlattenerProviderService {
  @Override
  public IAstRewriteFlattener createAstFlattener(ISimpleNameAstFlattenerCallback callback) {
    return new SimpleNameRewriteAstFlattener(callback);
  }
}
