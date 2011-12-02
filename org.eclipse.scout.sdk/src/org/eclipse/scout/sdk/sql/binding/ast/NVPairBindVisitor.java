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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sql.binding.model.BindBaseNVPair;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;

/**
 * <h3>{@link NVPairBindVisitor}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 26.02.2011
 */
public class NVPairBindVisitor extends DefaultAstVisitor {

  private boolean m_debug = false;
  private String m_indent = "";
  private boolean m_canceled;
  List<String> m_segments;
  private final ASTNode m_rootNode;
  private String m_bindName;
  private ASTNode m_valueNode;
  private final IMethod m_serviceMethod;

  public NVPairBindVisitor(ASTNode rootNode, IMethod serviceMethod) {
    m_rootNode = rootNode;
    m_serviceMethod = serviceMethod;
    m_segments = new ArrayList<String>();
  }

  @Override
  public void preVisit(ASTNode node) {
    if (m_debug && !m_canceled) {
      ScoutSdk.logInfo(m_indent + "bindBaseRef " + node.getNodeType() + "  " + node + "     ");
      m_indent += "  ";
    }
  }

  @Override
  public void postVisit(ASTNode node) {
    if (m_debug && !m_canceled) {
      m_indent = m_indent.replaceFirst("\\s\\s$", "");
      ScoutSdk.logInfo(m_indent + "end " + node.getNodeType());
    }
  }

  @Override
  public boolean visitNode(ASTNode node) {
    if (m_canceled) {
      return false;
    }
    if (m_bindName != null) {
      m_valueNode = node;
      m_canceled = true;
    }
    return !m_canceled;
  }

  @Override
  public boolean visit(StringLiteral node) {
    if (m_canceled) {
      return false;
    }
    if (m_bindName == null) {
      m_bindName = node.getLiteralValue();
      return false;
    }
    else {
      return super.visit(node);
    }

  }

  @Override
  public boolean visit(SimpleName node) {
    if (m_canceled) {
      return false;
    }
    if (node.getFullyQualifiedName().equals("NVPair")) {
      return false;
    }
    if (m_bindName == null) {
      StringVariableResolveVisitor subVisitor = new StringVariableResolveVisitor(node.getFullyQualifiedName(), node, m_rootNode);
      m_rootNode.accept(subVisitor);
      m_bindName = subVisitor.getValue();
      return false;
    }
    else {
      return super.visit(node);
    }

  }

  public BindBaseNVPair getBindBase() {
    if (m_bindName != null && m_valueNode != null) {
      return new BindBaseNVPair(m_bindName, m_valueNode);
    }
    return null;
  }

}
