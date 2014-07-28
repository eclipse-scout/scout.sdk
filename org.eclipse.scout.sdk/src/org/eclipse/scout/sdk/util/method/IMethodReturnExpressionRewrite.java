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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.scout.sdk.util.signature.IImportValidator;

/**
 * <h3>{@link IMethodReturnExpressionRewrite}</h3> Rewrite callback used if the default {@link SimpleName} rewrite
 * cannot be performed.
 *
 * @author Matthias Villiger
 * @since 3.10.0 23.01.2014
 * @see MethodReturnExpression
 */
public interface IMethodReturnExpressionRewrite {
  /**
   * Callback for {@link SimpleName} nodes that could not be rewritten automatically.
   *
   * @param node
   *          The AST node
   * @param element
   *          The corresponding {@link IJavaElement} that is referred by the given node.
   * @param validator
   *          The import validator to use for the rewrite.
   * @param classPath
   *          The classpath the resulting code must be valid with.
   * @param buffer
   *          The buffer to apply the rewritten flattened node.
   * @return true if the rewrite has been done. False if the callback did no rewrite and the default string equivalent
   *         should be applied instead.
   */
  boolean rewriteElement(SimpleName node, IJavaElement element, IImportValidator validator, IJavaProject classPath, StringBuffer buffer);
}
