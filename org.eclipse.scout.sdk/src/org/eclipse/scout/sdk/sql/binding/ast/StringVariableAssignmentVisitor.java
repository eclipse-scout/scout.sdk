/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.sql.binding.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;

/**
 * <h3>{@link StringVariableAssignmentVisitor}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public class StringVariableAssignmentVisitor extends DefaultAstVisitor {

  private final String m_variableName;
  private boolean m_nextVarName = false;
  private final ASTNode m_rootNode;
  private StringBuilder m_value;

  public StringVariableAssignmentVisitor(String variableName, String currentValue, ASTNode rootNode) {
    m_variableName = variableName;
    m_rootNode = rootNode;
    m_value = new StringBuilder();
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    m_nextVarName = true;
    return true;
  }

  @Override
  public boolean visit(Assignment node) {
    m_nextVarName = true;
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    if (m_nextVarName) {
      m_nextVarName = false;
    }
    else {
      if (!node.getFullyQualifiedName().equals(m_variableName)) {
        // resolve
        StringVariableResolveVisitor resolveVisitor = new StringVariableResolveVisitor(node.getFullyQualifiedName(), node, m_rootNode);
        m_rootNode.accept(resolveVisitor);
        m_value.append(" " + resolveVisitor.getValue() + " ");
      }
      //else
      // skip in order of collecting anything
    }
    return false;
  }

  @Override
  public boolean visit(StringLiteral node) {
    m_value.append(" " + node.getLiteralValue() + " ");
    return false;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return m_value.toString();
  }
}
