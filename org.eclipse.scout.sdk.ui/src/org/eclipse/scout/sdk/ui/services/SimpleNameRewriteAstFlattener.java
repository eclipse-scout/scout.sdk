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
package org.eclipse.scout.sdk.ui.services;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.dom.ASTFlattener;
import org.eclipse.scout.sdk.util.method.ISimpleNameAstFlattenerCallback;
import org.eclipse.scout.sdk.util.method.IAstRewriteFlattener;
import org.eclipse.scout.sdk.util.method.MethodReturnExpression;

/**
 * <h3>{@link SimpleNameRewriteAstFlattener}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 23.01.2014
 */
@SuppressWarnings("restriction")
public class SimpleNameRewriteAstFlattener implements IAstRewriteFlattener {

  private final ISimpleNameAstFlattenerCallback m_callback;

  public SimpleNameRewriteAstFlattener(ISimpleNameAstFlattenerCallback callback) {
    m_callback = callback;
  }

  @Override
  public String rewrite(ASTNode n) {
    ASTFlattener flattener = new ASTFlattener() {
      @Override
      public boolean visit(SimpleName node) {
        return MethodReturnExpression.visit(node, m_callback, fBuffer);
      }

      @Override
      public boolean visit(QualifiedName node) {
        return MethodReturnExpression.visit(node, fBuffer, this);
      }
    };
    n.accept(flattener);
    return flattener.getResult();
  }
}
