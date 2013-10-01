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
package org.eclipse.scout.sdk.util.ast;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.ast.visitor.TypeSignatureResolveVisitor;
import org.eclipse.scout.sdk.util.ast.visitor.VariableResolveVisitor;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link AstUtility}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 27.02.2011
 */
public final class AstUtility {

  private AstUtility() {
  }

  public static VariableType getTypeSignature(ASTNode node, ASTNode rootNode, IJavaElement containerElement) {
    TypeSignatureResolveVisitor visitor = new TypeSignatureResolveVisitor(rootNode, containerElement);
    node.accept(visitor);
    VariableType var = new VariableType("");
    var.setTypeSignature(visitor.getTypeSignature());
    var.addAssignedSignatures(visitor.getAssignedSignatures());
    return var;
  }

  public static VariableType resolveVariable(String variableName, IJavaElement containerElement, ASTNode rootNode, ASTNode stopNode) {
    VariableResolveVisitor visitor = new VariableResolveVisitor(variableName, containerElement, rootNode, stopNode);
    rootNode.accept(visitor);
    VariableType var = new VariableType(variableName);
    var.addAssignedSignatures(visitor.getAssignedTypesSignatures());
    var.setTypeSignature(visitor.getVariableTypeSignature());
    return var;
  }

  /**
   * Visits the given member using the given ast visitor.
   * 
   * @param member
   * @param visitor
   */
  public static void visitMember(final IMember member, final ASTVisitor visitor) {
    visitMember(member, visitor, null);
  }

  /**
   * Visits the given member using the given ast visitor.
   * 
   * @param member
   * @param visitor
   * @param monitor
   */
  public static void visitMember(final IMember member, final ASTVisitor visitor, final IProgressMonitor monitor) {
    ISourceRange r = null;
    try {
      r = member.getSourceRange();
    }
    catch (JavaModelException e) {
      // could not find source range
      SdkUtilActivator.logWarning("Could not calculate source range for member '" + member.toString() + "'.", e);
    }
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setCompilerOptions(member.getJavaProject().getOptions(true));
    parser.setIgnoreMethodBodies(false);
    if (r != null) {
      parser.setSourceRange(r.getOffset(), r.getLength());
    }
    parser.setResolveBindings(true);
    if (member.isBinary()) {
      parser.setSource(member.getClassFile());
    }
    else {
      parser.setSource(member.getCompilationUnit());
    }
    if (monitor != null && monitor.isCanceled()) {
      return;
    }
    ASTNode ast = parser.createAST(monitor);
    if (monitor != null && monitor.isCanceled()) {
      return;
    }

    ast.accept(visitor);
  }

  public static String resolveReturnValueSignature(String typeSignature, String methodName) {
    try {
      if (!StringUtility.isNullOrEmpty(typeSignature)) {
        IType refType = TypeUtility.getTypeBySignature(typeSignature);
        if (TypeUtility.exists(refType)) {
          IMethod method = TypeUtility.getMethod(refType, methodName);
          if (TypeUtility.exists(method)) {
            return SignatureUtility.getResolvedSignature(method.getReturnType(), refType);
          }
        }
      }
    }
    catch (CoreException e) {
      SdkUtilActivator.logError("could not parse return type of method '" + methodName + "' on type '" + typeSignature + "'");
    }
    return null;
  }
}
