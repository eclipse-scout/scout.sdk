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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.scout.sdk.sql.binding.model.PropertyBasedBindBase;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.ast.VariableType;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;

/**
 * <h3>{@link TableHolderBindVisitor}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 26.02.2011
 */
public class TableHolderBindVisitor extends DefaultAstVisitor {

  private boolean m_debug = false;
  private String m_indent = "";
  private boolean m_canceled;

  private int m_mode = -1;
  private final ASTNode m_rootNode;
  private final IMethod m_serviceMethod;
  private VariableType m_variable;
  private P_Tracker m_tracker;

  public TableHolderBindVisitor(ASTNode rootNode, IMethod serviceMethod) {
    m_rootNode = rootNode;
    m_serviceMethod = serviceMethod;
  }

  @Override
  public void preVisit(ASTNode node) {
    if (m_debug && !m_canceled) {
      System.out.println(m_indent + "bindBaseRef " + node.getNodeType() + "  " + node + "     ");
      m_indent += "  ";
    }
  }

  @Override
  public void postVisit(ASTNode node) {
    if (m_debug && !m_canceled) {
      System.out.println(m_indent + "end " + node.getNodeType());
      m_indent = m_indent.replaceFirst("\\s\\s$", "");
    }
  }

  @Override
  public boolean visitNode(ASTNode node) {
    if (m_canceled) {
      return false;
    }
    switch (m_mode) {
      case -1:
        return true;
      default:
        m_variable = AstUtility.getTypeSignature(node, m_rootNode, m_serviceMethod);
        m_canceled = true;
        break;
    }
    return true;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    m_mode = node.getNodeType();
    if (m_tracker == null) {
      m_tracker = new P_Tracker();
    }
    return true;
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    if (m_mode == node.getNodeType()) {
      m_tracker = null;
      m_canceled = true;
      m_mode = -1;
    }
  }

  @Override
  public boolean visit(SimpleType node) {
    switch (m_mode) {
      case ASTNode.CLASS_INSTANCE_CREATION:
        if (m_tracker != null) {
          if (m_tracker.tableHolderFilterTypeName == null) {
            m_tracker.tableHolderFilterTypeName = node.getName().getFullyQualifiedName();
            return false;
          }
        }
        break;
    }
    return super.visit(node);
  }

  public PropertyBasedBindBase getBindBase() {
    PropertyBasedBindBase b = new PropertyBasedBindBase();
    if (m_variable != null) {
      String[] varSigs = m_variable.getAssignedTypeSignatures();
      if (varSigs.length > 0) {
        b.addAssignedSignatures(varSigs);
        return b;
      }
    }
    return null;
  }

  private class P_Tracker {
    private String tableHolderFilterTypeName;

  }

}
