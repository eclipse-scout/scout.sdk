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
package org.eclipse.scout.sdk.util.internal.typecache;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.jdt.finegraned.AbstractFineGrainedAstMatcher;
import org.eclipse.scout.sdk.util.jdt.finegraned.FineGrainedJavaElementDelta;

/**
 *
 */
public class JdtEventCollector {

  private final ICompilationUnit m_icu;
  private long m_lastModification;
  private CompilationUnit m_ast;
  private HashMap<IJavaElement, JdtEvent> m_events;

  JdtEventCollector(ICompilationUnit icu) {
    m_icu = icu;
    m_lastModification = icu.getResource().getModificationStamp();
    m_events = new HashMap<IJavaElement, JdtEvent>();
    m_ast = createAst();
  }

  private CompilationUnit createAst() {
    ASTParser parser = AstUtility.newParser();
    parser.setCompilerOptions(JavaCore.getOptions());
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(getCompilationUnit());
    return (CompilationUnit) parser.createAST(null);
  }

  public FineGrainedJavaElementDelta[] updateAst() {
    final CompilationUnit newAst = createAst();
    final HashSet<FineGrainedJavaElementDelta> set = new HashSet<FineGrainedJavaElementDelta>();
    AbstractFineGrainedAstMatcher matcher = new AbstractFineGrainedAstMatcher() {
      @Override
      protected boolean processDelta(boolean match, ASTNode node, Object other) {
        if (!match) {
          try {

            IJavaElement e = ((ICompilationUnit) newAst.getJavaElement()).getElementAt(node.getStartPosition());
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
    newAst.subtreeMatch(matcher, m_ast);
    m_ast = newAst;
    return set.toArray(new FineGrainedJavaElementDelta[set.size()]);
  }

  public void addEvent(JdtEvent e) {
    m_events.put(e.getElement(), e);
  }

  public boolean containsEventFor(IJavaElement element) {
    return m_events.containsKey(element);
  }

  public boolean hasEvents() {
    return !m_events.isEmpty();
  }

  public JdtEvent[] getEvents() {
    return m_events.values().toArray(new JdtEvent[m_events.size()]);
  }

  public JdtEvent[] removeAllEvents(long resourceTimestamp) {
    JdtEvent[] events = m_events.values().toArray(new JdtEvent[m_events.size()]);
    m_events.clear();
    m_lastModification = resourceTimestamp;
    return events;
  }

  public boolean isEmpty() {
    return m_events.size() == 0;
  }

  public long getLastModification() {
    return m_lastModification;
  }

  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }

  public CompilationUnit getAst() {
    return m_ast;
  }

}
