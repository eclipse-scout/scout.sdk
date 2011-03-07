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
package org.eclipse.scout.sdk.jdt.ast;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.ast.visitor.TypeSignatureResolveVisitor;
import org.eclipse.scout.sdk.jdt.ast.visitor.VariableResolveVisitor;
import org.eclipse.scout.sdk.util.ScoutSignature;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>{@link AstUtility}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 27.02.2011
 */
public final class AstUtility {

  public static VariableType getTypeSignature(ASTNode node, ASTNode rootNode, IJavaElement containerElement) {
    TypeSignatureResolveVisitor visitor = new TypeSignatureResolveVisitor(rootNode, node, containerElement);
    node.accept(visitor);
    VariableType var = new VariableType("");
    var.setTypeSignature(visitor.getTypeSignature());
    var.addAssignedSignatures(visitor.getAssignedSignatures());
    return var;
  }

  public static VariableType resolveVariable(String variableName, IJavaElement containerElement, ASTNode rootNode, ASTNode stopNode) {
//    System.out.println("---- start resolve '" + variableName + "'");
    VariableResolveVisitor visitor = new VariableResolveVisitor(variableName, containerElement, rootNode, stopNode);
    rootNode.accept(visitor);
    VariableType var = new VariableType(variableName);
    var.addAssignedSignatures(visitor.getAssignedTypesSignatures());
    var.setTypeSignature(visitor.getVariableTypeSignature());
//    System.out.println("*** found variable '" + variableName + "' (" + var.getTypeSignature() + ")");
//    String[] assignedTypeSignatures = var.getAssignedTypeSignatures();
//    for (int i = 0; i < assignedTypeSignatures.length; i++) {
//      System.out.println("  " + i + ": " + assignedTypeSignatures[i]);
//    }
//    System.out.println("---- end resolve '" + variableName + "'");
    return var;
  }

  public static String resolveReturnValueSignature(String typeSignature, String methodName) {
    try {
      if (!StringUtility.isNullOrEmpty(typeSignature)) {
        IType refType = ScoutSdk.getTypeBySignature(typeSignature);
        if (TypeUtility.exists(refType)) {
          IMethod method = TypeUtility.getMethod(refType, methodName);
          if (TypeUtility.exists(method)) {
            return ScoutSignature.getResolvedSignature(method.getReturnType(), refType);
          }
        }
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("could not parse return type of method '" + methodName + "' on type '" + typeSignature + "'");
    }
    return null;
  }
}
