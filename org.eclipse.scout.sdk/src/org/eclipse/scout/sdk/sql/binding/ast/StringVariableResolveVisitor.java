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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;

/**
 * <h3>{@link StringVariableResolveVisitor}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public class StringVariableResolveVisitor extends DefaultAstVisitor {

  private final ASTNode m_stopNode;
  private boolean m_canceled = false;
  private final ASTNode m_rootNode;
  private StringBuilder m_value;
  private final String m_variableName;

  public StringVariableResolveVisitor(String variableName, ASTNode stopNode, ASTNode rootNode) {
    m_variableName = variableName;
    m_stopNode = stopNode;
    m_rootNode = rootNode;
    m_value = new StringBuilder();
  }

  @Override
  public boolean visitNode(ASTNode node) {
    if (!m_canceled) {
      m_canceled = node.equals(m_stopNode);
    }
    return !m_canceled;
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    if (node.getName().getFullyQualifiedName().equals(m_variableName)) {
      StringVariableAssignmentVisitor visitor = new StringVariableAssignmentVisitor(m_variableName, m_value.toString(), m_rootNode);
      node.accept(visitor);
      m_value.append(" " + visitor.getValue() + " ");
    }
    return false;
  }

  @Override
  public boolean visit(Assignment node) {
    if (node.getLeftHandSide().getNodeType() == ASTNode.SIMPLE_NAME) {
      SimpleName sn = (SimpleName) node.getLeftHandSide();
      if (sn.getFullyQualifiedName().equals(m_variableName)) {
        StringVariableAssignmentVisitor visitor = new StringVariableAssignmentVisitor(m_variableName, m_value.toString(), m_rootNode);
        node.accept(visitor);
        m_value.append(" " + visitor.getValue() + " ");
      }
    }
    return false;
  }

  public String getValue() {
    return m_value.toString();
  }

}
