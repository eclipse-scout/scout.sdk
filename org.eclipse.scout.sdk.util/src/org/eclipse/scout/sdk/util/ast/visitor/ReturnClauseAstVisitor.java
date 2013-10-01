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
package org.eclipse.scout.sdk.util.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ReturnStatement;

/**
 * <h3>{@link ReturnClauseAstVisitor}</h3> An AST visitor decorator that delegates all visits inside a return statement
 * (e.g. "return xyz.abc;") to the wrapped visitor.
 * 
 * @author mvi
 * @since 3.10.0 30.09.2013
 */
public class ReturnClauseAstVisitor extends ASTVisitor {

  private final ASTVisitor m_visitor;

  public ReturnClauseAstVisitor(ASTVisitor visitor) {
    m_visitor = visitor;
  }

  @Override
  public boolean visit(ReturnStatement node) {
    node.accept(m_visitor);
    return false;
  }
}
