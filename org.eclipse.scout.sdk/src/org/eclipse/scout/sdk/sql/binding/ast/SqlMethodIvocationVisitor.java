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
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sql.binding.model.IBindBase;
import org.eclipse.scout.sdk.sql.binding.model.ISqlToken;
import org.eclipse.scout.sdk.sql.binding.model.ParameterFragmentToken;
import org.eclipse.scout.sdk.sql.binding.model.SqlStatement;
import org.eclipse.scout.sdk.sql.binding.model.StringFragmentToken;
import org.eclipse.scout.sdk.sql.binding.model.UnresolvedBindBase;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;

/**
 * <h3>{@link SqlMethodIvocationVisitor}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public class SqlMethodIvocationVisitor extends DefaultAstVisitor {
  private static final int MODE_QUALIFIER = 1;
  private static final int MODE_METHOD_NAME = 2;
  private static final int MODE_STATEMENT = 3;
  private static final int MODE_BIND_VAR = 4;

  private List<SqlStatement> m_statements;
  private int m_unusedParametersAfterStatement;
  private int m_currentMode = MODE_QUALIFIER;
  private final ASTNode m_methodNode;
  private final IMethod m_serviceMethod;

  private SqlStatement m_currentStatment;
  private MethodInvocation m_methodInvocation;
  private boolean m_debug = false;

  public SqlMethodIvocationVisitor(ASTNode methodNode, IMethod serviceMethod) {
    m_methodNode = methodNode;
    m_serviceMethod = serviceMethod;
    m_statements = new ArrayList<SqlStatement>();
  }

  private String m_indent = "";

  @Override
  public void preVisit(ASTNode node) {
    if (m_debug) {
      ScoutSdk.logInfo(m_indent + "findSqlStatements " + node.getNodeType() + "  " + node + "     ");
      m_indent += "  ";
    }
  }

  @Override
  public void postVisit(ASTNode node) {
    if (m_debug) {
      m_indent = m_indent.replaceFirst("\\s\\s$", "");
      ScoutSdk.logInfo(m_indent + "end " + node.getNodeType());
    }
  }

  @Override
  public boolean visitNode(ASTNode node) {
    if (m_currentStatment != null) {
      switch (m_currentMode) {
        case MODE_QUALIFIER:
        case MODE_METHOD_NAME:
          m_currentMode++;
          return false;
        case MODE_STATEMENT:
          P_MethodParameterStatementVisitor statementVisitor = new P_MethodParameterStatementVisitor(m_currentStatment);
          node.accept(statementVisitor);
          if (m_currentStatment.hasTokens()) {
            resolveStatement(m_currentStatment, node);
          }
          else {
            m_currentStatment = null;
            m_currentMode = MODE_QUALIFIER;
          }
          if (m_unusedParametersAfterStatement > 0) {
            m_unusedParametersAfterStatement--;
          }
          else {
            m_currentMode++;
          }
          return false;
        case MODE_BIND_VAR:
          BindBasesVisitor bindBaseVisitor = new BindBasesVisitor(m_serviceMethod, m_methodNode);
          node.accept(bindBaseVisitor);
          IBindBase[] bindBases = bindBaseVisitor.getBindBases();
          if (bindBases.length == 0) {
            m_currentStatment.addBindBase(new UnresolvedBindBase(node));
          }
          else {
            for (IBindBase b : bindBases) {
              m_currentStatment.addBindBase(b);
            }
          }
          return false;

        default:
          break;
      }
    }
    return true;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    // TODO Auto-generated method stub
    return super.visit(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    if (m_methodInvocation == null) {
      if ("select".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 0;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("selectLimited".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 1;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("selectInto".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 0;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("selectIntoLimited".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 1;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("selectStreaming".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 1;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("selectStreamingLimited".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 2;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("insert".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 0;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("update".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 0;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("delete".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 0;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("callStoredProcedure".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 0;
        m_currentStatment = new SqlStatement();
        return true;
      }
      else if ("createPlainText".equals(node.getName().getFullyQualifiedName())) {
        m_methodInvocation = node;
        m_unusedParametersAfterStatement = 0;
        m_currentStatment = new SqlStatement();
        return true;
      }
      return false;
    }
    return super.visit(node);
  }

  @Override
  public void endVisit(MethodInvocation node) {
    if (node.equals(m_methodInvocation)) {
      if (m_currentStatment != null) {
        if (m_currentStatment.hasTokens()) {
          // resolve location in resouce
          try {
            ISourceRange sourceRange = m_serviceMethod.getSourceRange();
            int start = sourceRange.getOffset() + m_methodInvocation.getStartPosition();
            int length = m_methodInvocation.getLength();
            m_currentStatment.setOffset(start);
            m_currentStatment.setLength(length);
          }
          catch (JavaModelException e) {
            ScoutSdk.logWarning("could not determ location in resource '" + m_serviceMethod.getElementName() + "' on type '" + m_serviceMethod.getDeclaringType().getFullyQualifiedName() + "'.");
          }
          m_statements.add(m_currentStatment);
        }
        m_currentStatment = null;
        m_currentMode = MODE_QUALIFIER;
      }
      m_methodInvocation = null;
    }
  }

  private void resolveStatement(SqlStatement statement, ASTNode stopNode) {
    for (ISqlToken token : statement.getTokens()) {
      if (token.getType() == ISqlToken.TYPE_PARAMETER) {
        StringVariableResolveVisitor visitor = new StringVariableResolveVisitor(((ParameterFragmentToken) token).getParameterName(), stopNode, m_methodNode);
        m_methodNode.accept(visitor);
        StringFragmentToken newToken = new StringFragmentToken(visitor.getValue());
        statement.replaceToken(token, newToken);
      }
    }
  }

  public SqlStatement[] getStatements() {
    return m_statements.toArray(new SqlStatement[m_statements.size()]);
  }

  private class P_MethodParameterStatementVisitor extends DefaultAstVisitor {
    private SqlStatement m_statement;

    public P_MethodParameterStatementVisitor(SqlStatement statement) {
      m_statement = statement;
    }

    @Override
    public boolean visit(InfixExpression node) {
      return node.getOperator().equals(Operator.PLUS);
    }

    @Override
    public boolean visit(StringLiteral node) {
      m_statement.addToken(new StringFragmentToken(" " + node.getLiteralValue() + " "));
      return false;
    }

    @Override
    public boolean visit(SimpleName node) {
      // try to find var
      m_statement.addToken(new ParameterFragmentToken(node.getFullyQualifiedName()));
      return false;
    }

  } // end class P_MethodParameterStatementVisitor

}
