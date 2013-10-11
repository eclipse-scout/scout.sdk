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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * <h3>{@link MethodBodyAstVisitor}</h3> An AST visitor decorator that delegates all visits that are part of the given
 * method body to the wrapped visitor
 * 
 * @author mvi
 * @since 3.10.0 30.09.2013
 */
public class MethodBodyAstVisitor extends DefaultAstVisitor {

  private final String m_declaringTypeFqn;
  private final IMethod m_method;
  private final ASTVisitor m_visitor;

  private boolean m_inMethod;
  private boolean m_inBody;

  public MethodBodyAstVisitor(IMethod methodToVisit, ASTVisitor visitor) {
    m_method = methodToVisit;
    m_declaringTypeFqn = methodToVisit.getDeclaringType().getFullyQualifiedName();
    m_visitor = visitor;
    m_inMethod = false;
    m_inBody = false;
  }

  @Override
  public boolean visitNode(ASTNode node) {
    if (m_inBody) {
      node.accept(m_visitor);
    }
    return false;
  }

  @Override
  public boolean visit(CompilationUnit node) {
    return true;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    boolean isMethodInteresting = false;
    IMethodBinding binding = node.resolveBinding();
    if (binding != null) {
      IJavaElement javaElement = binding.getJavaElement();
      if (javaElement != null) {
        isMethodInteresting = javaElement.equals(m_method);
      }
    }
    m_inMethod = isMethodInteresting;
    return isMethodInteresting;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    m_inMethod = false;
    m_inBody = false;
  }

  @Override
  public boolean visit(Block node) {
    if (m_inMethod) {
      m_inBody = true;
      return m_inBody;
    }
    return false;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    String curTypeFqn = ((IType) node.resolveBinding().getJavaElement()).getFullyQualifiedName();
    return m_declaringTypeFqn.startsWith(curTypeFqn);
  }
}
