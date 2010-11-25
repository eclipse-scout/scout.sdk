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
package org.eclipse.scout.sdk.ui.fields.proposal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

public class SignatureProposalProvider implements IContentProposalProvider {

  private static final IContentProposalEx[] m_primitives = new IContentProposalEx[]{new SignatureProposal(Signature.SIG_BOOLEAN),
      new SignatureProposal(Signature.SIG_INT),
      new SignatureProposal(Signature.SIG_DOUBLE), new SignatureProposal(Signature.SIG_BYTE)};

  private static final IContentProposalEx[] m_simpleList = new IContentProposalEx[]{
      new SignatureProposal(Signature.createTypeSignature(ArrayList.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(java.lang.Boolean.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(Collection.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(Date.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(Double.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(java.util.Enumeration.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(java.lang.Float.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(HashMap.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(Integer.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(List.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(Long.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(Map.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(java.lang.Number.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(Object.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(java.lang.Runnable.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(Set.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(String.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(TreeMap.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(TreeSet.class.getName(), true)),
      new SignatureProposal(Signature.createTypeSignature(java.util.Vector.class.getName(), true))
  };

  private SearchEngine m_searchEngine;
  private final boolean m_usePrimitives;
  private final boolean m_supportsGenerics;
  private final IJavaSearchScope m_searchScope;

  public SignatureProposalProvider(IJavaSearchScope searchScope, boolean usePrimitives, boolean supportsGenerics) {
    m_searchScope = searchScope;
    m_usePrimitives = usePrimitives;
    m_supportsGenerics = supportsGenerics;
    m_searchEngine = new SearchEngine();
  }

  public boolean supportsExpertMode() {
    return true;
  }

  public IContentProposalEx[] getProposals(String content, int cursorPosition, IProgressMonitor monitor) {
    ArrayList<IContentProposalEx> props = new ArrayList<IContentProposalEx>();
    String patStr = content.substring(0, cursorPosition) + "*";
    char[] pattern = CharOperation.toLowerCase(patStr.toCharArray());
    if (m_usePrimitives) {
      for (IContentProposalEx prop : m_primitives) {
        if (CharOperation.match(pattern, prop.getLabel(false, false).toCharArray(), false)) {
          props.add(prop);
        }
      }
    }
    for (IContentProposalEx prop : m_simpleList) {
      if (CharOperation.match(pattern, prop.getLabel(false, false).toCharArray(), false)) {
        props.add(prop);
      }
    }

    return props.toArray(new IContentProposalEx[props.size()]);
  }

  public IContentProposalEx[] getProposalsExpertMode(String contents, int position, IProgressMonitor monitor) {
    ArrayList<IContentProposalEx> collector = new ArrayList<IContentProposalEx>();
    if (m_usePrimitives) {
      collectPrimitivProposals(contents, position, collector);
    }
    collectTypes(contents, position, collector, monitor);
    return collector.toArray(new IContentProposalEx[collector.size()]);
  }

  private void collectPrimitivProposals(String contents, int position, ArrayList<IContentProposalEx> collector) {
    String prefix = contents.substring(0, position);
    for (IContentProposalEx prop : m_primitives) {
      if (prop.getLabel(false, true).startsWith(prefix)) {
        collector.add(prop);
      }
    }
  }

  private void collectTypes(String contents, int position, ArrayList<IContentProposalEx> collector, IProgressMonitor monitor) {
    if (position < 1) return;
    P_SearchRequestor searchRequestor = new P_SearchRequestor(monitor);
    try {

      // IJavaSearchScope searchScope=SearchEngine.createWorkspaceScope();
      //
      m_searchEngine.search(SearchPattern.createPattern(
          contents.substring(0, position) + "*",
          IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS,
          SearchPattern.R_PATTERN_MATCH), new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, m_searchScope, searchRequestor, null);
    }
    catch (CoreException e) {
      if (e.getStatus().matches(IStatus.ERROR)) {
        ScoutSdkUi.logWarning(e);
        return;
      }
      if (e.getStatus().matches(IStatus.CANCEL)) {
        return;
      }
    }
    ArrayList<IContentProposalEx> props = new ArrayList<IContentProposalEx>(searchRequestor.getResult());
    if (props.get(0) instanceof ISeparatorProposal) {
      props.remove(0);
    }
    else if (props.get(props.size() - 1) instanceof ISeparatorProposal) {
      props.remove(props.size() - 1);
    }
    collector.addAll(props);
  }

  private class P_SearchRequestor extends SearchRequestor {
    private IProgressMonitor m_monitor;
    private TreeMap<CompositeObject, IContentProposalEx> m_foundTypes = new TreeMap<CompositeObject, IContentProposalEx>();
    private int counter = 0;

    public P_SearchRequestor(IProgressMonitor monitor) {
      m_monitor = monitor;
      m_foundTypes.put(new CompositeObject("B"), new P_Separator());
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) throws CoreException {
      if (m_monitor.isCanceled()) {
        throw new CoreException(new Status(IStatus.CANCEL, ScoutSdk.PLUGIN_ID, "canceled by monitor"));
      }
      if (match instanceof TypeDeclarationMatch) {

        IType type = (IType) match.getElement();
        if (!m_supportsGenerics && (type.getTypeParameters().length > 0)) {
          return;
        }
        m_foundTypes.put(new CompositeObject("A", type.getElementName(), type.getFullyQualifiedName()), new SignatureProposal(Signature.createTypeSignature(type.getFullyQualifiedName(), true)));
        if (counter++ > 98) {
          throw new CoreException(new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, "stopped after 100"));
        }

      }
    }

    public Collection<IContentProposalEx> getResult() {
      return m_foundTypes.values();
    }

    private class P_Separator implements ISeparatorProposal {

      public String getLabel(boolean selected, boolean expertMode) {
        return "------------ common used ------------------";
      }

      public Image getImage(boolean selected, boolean expertMode) {
        return ScoutSdkUi.getImage(ScoutSdkUi.IMG_TYPE_SEPARATOR);
      }

      public int getCursorPosition(boolean selected, boolean expertMode) {
        return 0;
      }
    }
  }
}
