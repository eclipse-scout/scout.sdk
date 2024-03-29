/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.util.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * <h3>{@link DefaultAstVisitor}</h3>
 *
 * @since 1.0.8 2011-02-24
 */
public class DefaultAstVisitor extends ASTVisitor {

  public DefaultAstVisitor() {
    this(false);
  }

  public DefaultAstVisitor(boolean visitDocTags) {
    super(visitDocTags);
  }

  /**
   * Callback to visit each node type.
   *
   * @param node
   *          The visited node.
   * @return {@code true} if the children of this node should be visited, and {@code false} if the children of this node
   *         should be skipped.
   */
  protected boolean visitNode(ASTNode node) {
    return true;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ArrayAccess node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ArrayCreation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ArrayInitializer node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ArrayType node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(AssertStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(Assignment node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(Block node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(BlockComment node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(BooleanLiteral node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(BreakStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(CastExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(CatchClause node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(CharacterLiteral node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(CompilationUnit node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ConditionalExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ConstructorInvocation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ContinueStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(DoStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(EmptyStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(EnumConstantDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(FieldAccess node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ForStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(IfStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ImportDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(InfixExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(Initializer node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(InstanceofExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(Javadoc node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(LabeledStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(LineComment node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(MemberRef node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(MemberValuePair node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(MethodRef node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(MethodRefParameter node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(Modifier node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(NullLiteral node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(NumberLiteral node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ParameterizedType node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ParenthesizedExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(PostfixExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(PrefixExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(PrimitiveType node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(QualifiedName node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(QualifiedType node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ReturnStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SimpleName node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SimpleType node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(StringLiteral node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SuperConstructorInvocation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SuperFieldAccess node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SwitchCase node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SwitchStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(SynchronizedStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(TagElement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(TextElement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ThisExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(ThrowStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(TryStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(TypeDeclarationStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(TypeLiteral node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(TypeParameter node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(WhileStatement node) {
    return visitNode(node);
  }

  @Override
  public boolean visit(WildcardType node) {
    return visitNode(node);
  }

}
