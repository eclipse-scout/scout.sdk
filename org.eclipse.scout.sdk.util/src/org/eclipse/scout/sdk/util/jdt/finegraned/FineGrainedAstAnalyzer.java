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
package org.eclipse.scout.sdk.util.jdt.finegraned;

import java.util.HashSet;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.util.ast.AstUtility;

class FineGrainedAstAnalyzer {
  private IJavaElementDelta m_delta;
  private ICompilationUnit m_icu;

  public FineGrainedAstAnalyzer(IJavaElementDelta delta) {
    m_delta = delta;
    m_icu = (ICompilationUnit) delta.getElement();
  }

  public FineGrainedJavaElementDelta[] calculateDeltas(String oldContent, String newContent) {
    if (CompareUtility.equals(oldContent, newContent)) {
      return new FineGrainedJavaElementDelta[0];
    }
    CompilationUnit oldAst = null;
    if (oldAst == null) {
      ASTParser parser = AstUtility.newParser();
      parser.setCompilerOptions(JavaCore.getOptions());
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setSource(oldContent.toCharArray());
      oldAst = (CompilationUnit) parser.createAST(null);
    }
    CompilationUnit newAst = m_delta.getCompilationUnitAST();
    if (newAst == null) {
      ASTParser parser = AstUtility.newParser();
      parser.setCompilerOptions(JavaCore.getOptions());
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setSource(newContent.toCharArray());
      newAst = (CompilationUnit) parser.createAST(null);
    }
    return calculateDeltas(oldAst, newAst);
  }

  public FineGrainedJavaElementDelta[] calculateDeltas(CompilationUnit oldAst, CompilationUnit newAst) {
    final HashSet<FineGrainedJavaElementDelta> set = new HashSet<FineGrainedJavaElementDelta>();
    FineGrainedAstMatcher matcher = new FineGrainedAstMatcher() {
      @Override
      protected boolean processDelta(boolean match, ASTNode node, Object other) {
        if (!match) {
          try {
            IJavaElement e = m_icu.getElementAt(node.getStartPosition());
            if (e != null) {
              set.add(new FineGrainedJavaElementDelta(e));
            }
          }
          catch (JavaModelException e1) {
            // nop
          }
        }
        return true;
      }
    };
    newAst.subtreeMatch(matcher, oldAst);
    return set.toArray(new FineGrainedJavaElementDelta[set.size()]);
  }

}
