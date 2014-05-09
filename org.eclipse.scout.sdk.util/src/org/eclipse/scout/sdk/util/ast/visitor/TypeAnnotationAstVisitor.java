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

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * <h3>{@link TypeAnnotationAstVisitor}</h3> An AST visitor decorator that delegates all visits that are part of the
 * given annotation to the wrapped visitor
 * 
 * @author Matthias Villiger
 * @since 4.0.0 09.05.2014
 */
public class TypeAnnotationAstVisitor extends DefaultAstVisitor {

  private final String m_declaringTypeFqn;
  private final IAnnotation m_annotation;
  private final ASTVisitor m_visitor;

  private boolean m_inAnnotation;

  public TypeAnnotationAstVisitor(IAnnotation annotationToVisit, IType declaringType, ASTVisitor visitor) {
    m_annotation = annotationToVisit;
    m_declaringTypeFqn = declaringType.getFullyQualifiedName();
    m_visitor = visitor;
    m_inAnnotation = false;
  }

  @Override
  public boolean visitNode(ASTNode node) {
    if (m_inAnnotation) {
      node.accept(m_visitor);
    }
    return false;
  }

  @Override
  public boolean visit(CompilationUnit node) {
    return true;
  }

  private boolean visitAnnotation(Annotation node) {
    boolean isAnnotationInteresting = false;
    IAnnotationBinding binding = node.resolveAnnotationBinding();
    if (binding != null) {
      IJavaElement javaElement = binding.getJavaElement();
      if (javaElement != null) {
        isAnnotationInteresting = javaElement.equals(m_annotation);
      }
    }
    m_inAnnotation = isAnnotationInteresting;
    return isAnnotationInteresting;
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    return visitAnnotation(node);
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    return visitAnnotation(node);
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    return visitAnnotation(node);
  }

  @Override
  public void endVisit(MarkerAnnotation node) {
    m_inAnnotation = false;
  }

  @Override
  public void endVisit(SingleMemberAnnotation node) {
    m_inAnnotation = false;
  }

  @Override
  public void endVisit(NormalAnnotation node) {
    m_inAnnotation = false;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    ITypeBinding binding = node.resolveBinding();
    if (binding != null) {
      String curTypeFqn = ((IType) binding.getJavaElement()).getFullyQualifiedName();
      return m_declaringTypeFqn.startsWith(curTypeFqn);
    }
    return true;
  }
}
