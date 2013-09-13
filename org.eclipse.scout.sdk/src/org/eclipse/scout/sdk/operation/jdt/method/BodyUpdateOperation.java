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
package org.eclipse.scout.sdk.operation.jdt.method;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.scout.commons.holders.Holder;

/**
 * <h3>{@link BodyUpdateOperation}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 21.02.2013
 */
public class BodyUpdateOperation {
  private final IMethod m_method;

  public BodyUpdateOperation(IMethod method) {
    m_method = method;

  }

  public void run() throws JavaModelException {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setCompilerOptions(JavaCore.getOptions());
    parser.setBindingsRecovery(true);
    parser.setResolveBindings(true);
    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
    parser.setSource(m_method.getSource().toCharArray());
    ASTNode node = parser.createAST(null);
    final Holder<ISourceRange> range = new Holder<ISourceRange>(ISourceRange.class, new SourceRange(0, 0));
    node.accept(new ASTVisitor() {
      boolean a = false;

      @Override
      public boolean visit(MethodDeclaration n) {
        a = true;
        return true;
      }

      @Override
      public boolean visit(Block n) {
        if (a) {
          range.setValue(new SourceRange(n.getStartPosition(), n.getLength()));
          return false;
        }
        return true;
      }

    });
  }

}
