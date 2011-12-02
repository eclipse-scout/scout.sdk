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
package org.eclipse.scout.sdk.util.ast.visitor;

import java.util.HashSet;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 * <h3>{@link VariableResolveVisitor}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 27.02.2011
 */
public class VariableResolveVisitor extends DefaultAstVisitor {

  private boolean m_debug = false;
  private boolean m_canceled;
  public String m_indent = "";

  public String m_variableTypeSignature;
  private HashSet<String /*signatures*/> m_assignedTypes;

  private P_Variable m_currentVariable;
  private final String m_variableName;
  private final ASTNode m_rootNode;
  private final ASTNode m_stopNode;
  private final IJavaElement m_containerElement;
  private int m_mode = -1;

  public VariableResolveVisitor(String variableName, IJavaElement containerElement, ASTNode rootNode, ASTNode stopNode) {
    m_variableName = variableName;
    m_containerElement = containerElement;
    m_rootNode = rootNode;
    m_stopNode = stopNode;
    m_assignedTypes = new HashSet<String>();

  }

  @Override
  public void preVisit(ASTNode node) {
    if (node.equals(m_stopNode)) {
      m_canceled = true;
    }
    if (m_debug && !m_canceled) {
      SdkUtilActivator.logInfo(m_indent + "varResolve " + node.getNodeType() + "  " + node + "     ");
      m_indent += "  ";
    }
  }

  @Override
  public void postVisit(ASTNode node) {
    if (m_debug && !m_canceled) {
      m_indent = m_indent.replaceFirst("\\s\\s$", "");
      SdkUtilActivator.logInfo(m_indent + "end " + node.getNodeType());
    }
  }

  @Override
  public boolean visitNode(ASTNode node) {
    if (m_canceled) {
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    if (m_canceled) {
      return false;
    }
    m_mode = node.getNodeType();
    m_currentVariable = new P_Variable();
    return true;
  }

  @Override
  public void endVisit(SingleVariableDeclaration node) {
    if (m_canceled) {
      return;
    }
    if (m_mode == node.getNodeType()) {
      if (m_currentVariable != null) {
        store(m_currentVariable);
        m_currentVariable = null;
      }
      m_mode = -1;
    }
  }

  @Override
  public boolean visit(SimpleType node) {
    if (m_canceled) {
      return false;
    }
    if (m_currentVariable != null) {
      switch (m_mode) {
        case ASTNode.VARIABLE_DECLARATION_STATEMENT:
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
        case ASTNode.ASSIGNMENT:
          if (m_currentVariable.variableTypeName == null) {
            try {
              String resolvedSignature = SignatureUtility.getResolvedSignature(Signature.createTypeSignature(node.getName().getFullyQualifiedName(), false), JdtUtility.findDeclaringType(m_containerElement));
              m_currentVariable.variableTypeName = resolvedSignature;
            }
            catch (JavaModelException e) {
              e.printStackTrace();
            }
            return false;
          }

          break;
        default:
          break;
      }
    }
    return super.visit(node);
  }

  @Override
  public boolean visit(SimpleName node) {
    if (m_canceled) {
      return false;
    }
    if (m_currentVariable != null) {
      switch (m_mode) {
        case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
        case ASTNode.ASSIGNMENT:
          if (m_currentVariable.variableName == null) {
            if (m_variableName.equals(node.getFullyQualifiedName())) {
              m_currentVariable.variableName = m_variableName;
            }
            else {
              m_currentVariable = null;
            }
            return false;
          }
          break;

        default:
          break;
      }
    }

    return super.visit(node);
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    if (m_canceled) {
      return false;
    }
    m_mode = node.getNodeType();
    m_currentVariable = new P_Variable();
    return true;
  }

  @Override
  public void endVisit(VariableDeclarationStatement node) {
    if (m_canceled) {
      return;
    }
    if (m_mode == node.getNodeType()) {
      if (m_currentVariable != null) {
        store(m_currentVariable);
        m_currentVariable = null;
      }
      m_mode = -1;
    }
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    if (m_canceled) {
      return false;
    }
    m_mode = node.getNodeType();
    if (m_currentVariable == null) {
      m_currentVariable = new P_Variable();
    }
    return true;
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    if (m_canceled) {
      return;
    }
    if (m_mode == node.getNodeType()) {
      if (m_currentVariable != null) {
        store(m_currentVariable);
        m_currentVariable = null;
      }
      m_mode = -1;
    }
  }

  @Override
  public boolean visit(Assignment node) {
    if (m_canceled) {
      return false;
    }
    m_mode = node.getNodeType();
    if (m_currentVariable == null) {
      m_currentVariable = new P_Variable();
    }
    return true;
  }

  @Override
  public void endVisit(Assignment node) {
    if (m_canceled) {
      return;
    }
    if (m_mode == node.getNodeType()) {
      if (m_currentVariable != null) {
        store(m_currentVariable);
        m_currentVariable = null;
      }
      m_mode = -1;
    }
  }

  @Override
  public boolean visit(MethodInvocation node) {
    if (m_canceled) {
      return false;
    }
    if (m_currentVariable != null) {
      TypeSignatureResolveVisitor innerVisitor = new TypeSignatureResolveVisitor(m_rootNode, node, m_containerElement);
      node.accept(innerVisitor);
      String[] assignedSignatures = innerVisitor.getAssignedSignatures();
      if (assignedSignatures.length > 0) {
        for (String as : assignedSignatures) {
          m_assignedTypes.add(as);
        }
      }
      if (m_variableTypeSignature == null && innerVisitor.getTypeSignature() != null) {
        m_variableTypeSignature = innerVisitor.getTypeSignature();
      }
      return false;
    }
    return super.visit(node);
  }

  private void store(P_Variable var) {
    if (var != null && var.isAssigned()) {
      if (m_variableTypeSignature == null) {
        m_variableTypeSignature = var.variableTypeName;
      }
      for (String s : var.assignedTypeName) {
        m_assignedTypes.add(s);
      }
    }
  }

  /**
   * @return the assignedTypes
   */
  public String[] getAssignedTypesSignatures() {
    return m_assignedTypes.toArray(new String[m_assignedTypes.size()]);
  }

  /**
   * @return the variableTypeSignature
   */
  public String getVariableTypeSignature() {
    return m_variableTypeSignature;
  }

  private class P_Variable {
    public String variableTypeName;
    public String variableName;
    public HashSet<String> assignedTypeName = new HashSet<String>();

    public boolean isAssigned() {
      return variableName != null && (assignedTypeName != null || !variableTypeName.isEmpty());
    }
  }
}
