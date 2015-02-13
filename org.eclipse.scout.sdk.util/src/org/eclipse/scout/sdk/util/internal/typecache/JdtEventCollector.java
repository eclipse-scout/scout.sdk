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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.jdt.finegrained.AbstractFineGrainedAstMatcher;

/**
 *
 */
public class JdtEventCollector {

  private final ICompilationUnit m_icu;
  private final Map<IJavaElement, JdtEvent> m_events;
  private long m_lastModification;
  private CompilationUnit m_ast;

  JdtEventCollector(ICompilationUnit icu) {
    m_icu = icu;
    m_lastModification = icu.getResource().getModificationStamp();
    m_events = new HashMap<>();
    m_ast = createAst();
  }

  private CompilationUnit createAst() {
    ASTParser parser = AstUtility.newParser();
    parser.setCompilerOptions(JavaCore.getOptions());
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(m_icu);
    return (CompilationUnit) parser.createAST(null);
  }

  public Set<IJavaElement> updateAst() {
    final CompilationUnit newAst = createAst();
    final Set<IJavaElement> set = new HashSet<>();
    AbstractFineGrainedAstMatcher matcher = new AbstractFineGrainedAstMatcher() {
      @Override
      protected boolean processDelta(boolean match, ASTNode node, Object other) {
        if (!match) {
          try {
            IJavaElement javaElement = newAst.getJavaElement();
            if (javaElement instanceof ICompilationUnit) {
              IJavaElement e = ((ICompilationUnit) javaElement).getElementAt(node.getStartPosition());
              if (e != null) {
                set.add(e);
              }
            }
          }
          catch (JavaModelException e1) {
            SdkUtilActivator.logError(e1);
          }
        }
        return true;
      }
    };
    newAst.subtreeMatch(matcher, m_ast);
    m_ast = newAst;
    return set;
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

  public List<JdtEvent> removeAllEvents(long resourceTimestamp) {
    List<JdtEvent> values = CollectionUtility.arrayList(m_events.values());
    m_events.clear();
    m_lastModification = resourceTimestamp;
    return values;
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
