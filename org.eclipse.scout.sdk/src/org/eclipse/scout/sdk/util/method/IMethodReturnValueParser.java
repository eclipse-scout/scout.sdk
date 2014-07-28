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

import org.eclipse.jdt.core.IMethod;

/**
 * <h3>{@link IMethodReturnValueParser}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 30.09.2013
 */
public interface IMethodReturnValueParser {
  /**
   * parses the return expression of the given method
   *
   * @param m
   *          the method of which the return expression should be parsed.
   * @return the parsed expression. Depending on the parser used, the result may contain more or less meta information
   *         about the return clause.
   */
  MethodReturnExpression parse(IMethod m);
}
