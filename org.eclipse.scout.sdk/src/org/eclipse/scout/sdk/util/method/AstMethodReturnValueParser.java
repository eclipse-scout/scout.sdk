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
package org.eclipse.scout.sdk.util.method;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.ast.visitor.DefaultAstVisitor;
import org.eclipse.scout.sdk.util.ast.visitor.MethodBodyAstVisitor;
import org.eclipse.scout.sdk.util.ast.visitor.ReturnClauseAstVisitor;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link AstMethodReturnValueParser}</h3> Full detail parser that calculates all information for a return clause in
 * the given method.
 * 
 * @author Matthias Villiger
 * @since 3.10.0 30.09.2013
 */
public final class AstMethodReturnValueParser implements IMethodReturnValueParser {

  public final static AstMethodReturnValueParser INSTANCE = new AstMethodReturnValueParser();

  private AstMethodReturnValueParser() {
  }

  @Override
  public MethodReturnExpression parse(final IMethod method) {
    final MethodReturnExpression ret = new MethodReturnExpression();
    AstUtility.visitMember(method, new MethodBodyAstVisitor(method, new ReturnClauseAstVisitor(new DefaultAstVisitor() {
      @Override
      public boolean visit(ReturnStatement node) {
        ret.setReturnExpression(node.getExpression());
        return true;
      }

      @Override
      public boolean visit(SimpleName node) {
        IBinding binding = node.resolveBinding();
        if (binding != null) {
          IJavaElement referencedElement = binding.getJavaElement();
          if (TypeUtility.exists(referencedElement)) {
            ret.addReferencedElement(node, referencedElement);
          }
        }
        return true;
      }
    })));
    return ret;
  }
}
