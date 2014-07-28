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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;
import org.eclipse.scout.sdk.util.IRegEx;
import org.eclipse.scout.sdk.util.jdt.AbstractElementChangedListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link AbstractCachedTypeContentProposalProvider}</h3> A content provider with cached elements that will be
 * discarded when the given type changes.<br>
 * Important: This content provider must be disposed when no longer needed!
 *
 * @author Matthias Villiger
 * @since 3.10.0 30.01.2014
 */
public abstract class AbstractCachedTypeContentProposalProvider extends ContentProposalProvider {
  private final ILabelProvider m_labelProvider;

  private IType m_type;
  private Set<?> m_proposals;
  private AbstractElementChangedListener m_listener;

  protected AbstractCachedTypeContentProposalProvider(ILabelProvider labelProvider) {
    m_labelProvider = labelProvider;
  }

  public synchronized IType getType() {
    return m_type;
  }

  public synchronized void setType(IType t) {
    m_type = t;
    reset();
  }

  protected void reset() {
    if (m_listener != null) {
      JavaCore.removeElementChangedListener(m_listener);
      m_listener = null;
    }
    m_proposals = null;
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (!StringUtility.hasText(searchPattern)) {
      searchPattern = "*";
    }
    else {
      searchPattern = IRegEx.STAR_END.matcher(searchPattern).replaceAll("") + "*";
    }
    char[] pattern = CharOperation.toLowerCase(searchPattern.toCharArray());
    Set<?> elements = getElements();
    ArrayList<Object> collector = new ArrayList<Object>(elements.size());
    for (Object proposal : elements) {
      if (CharOperation.match(pattern, m_labelProvider.getText(proposal).toCharArray(), false)) {
        collector.add(proposal);
      }
    }
    return collector.toArray(new Object[collector.size()]);
  }

  @Override
  public synchronized void dispose() {
    reset();
    super.dispose();
  }

  private synchronized Set<?> getElements() {
    if (m_proposals == null) {
      boolean typeExists = TypeUtility.exists(m_type);
      if (m_listener == null && typeExists && !m_type.isBinary()) {
        m_listener = new P_ElementListener(m_type, this);
        JavaCore.addElementChangedListener(m_listener);
      }
      if (typeExists) {
        m_proposals = computeProposals();
      }
    }
    return m_proposals;
  }

  protected abstract Set<?> computeProposals();

  private static final class P_ElementListener extends AbstractElementChangedListener {

    private final IType m_type;
    private final AbstractCachedTypeContentProposalProvider m_provider;

    private P_ElementListener(IType type, AbstractCachedTypeContentProposalProvider provider) {
      m_type = type;
      m_provider = provider;
    }

    @Override
    protected boolean visitModify(int flags, IJavaElement e, CompilationUnit ast) {
      if (e != null && e.getElementType() == IJavaElement.TYPE) {
        IType eventType = (IType) e;
        if (TypeUtility.exists(eventType) && m_type.equals(eventType)) {
          synchronized (m_provider) {
            m_provider.m_proposals = null;
          }
          return false;
        }
      }
      return super.visitModify(flags, e, ast);
    }
  }
}
