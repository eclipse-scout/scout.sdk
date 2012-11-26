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
package org.eclipse.scout.sdk.ui.fields.proposal.signature;

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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.MoreElementsProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;

public class SignatureProposalProvider extends ContentProposalProvider {

  public static final String[] DEFAULT_PRIMITIV_SIGNATURES = new String[]{
      Signature.SIG_BOOLEAN,
      Signature.SIG_BYTE,
      Signature.SIG_CHAR,
      Signature.SIG_DOUBLE,
      Signature.SIG_FLOAT,
      Signature.SIG_INT,
      Signature.SIG_LONG,
      Signature.SIG_SHORT};

  public static final String[] DEFAULT_MOST_USED = new String[]{
      SignatureCache.createTypeSignature(ArrayList.class.getName()),
      SignatureCache.createTypeSignature(java.lang.Boolean.class.getName()),
      SignatureCache.createTypeSignature(Collection.class.getName()),
      SignatureCache.createTypeSignature(Date.class.getName()),
      SignatureCache.createTypeSignature(Double.class.getName()),
      SignatureCache.createTypeSignature(java.util.Enumeration.class.getName()),
      SignatureCache.createTypeSignature(java.lang.Float.class.getName()),
      SignatureCache.createTypeSignature(HashMap.class.getName()),
      SignatureCache.createTypeSignature(Integer.class.getName()),
      SignatureCache.createTypeSignature(List.class.getName()),
      SignatureCache.createTypeSignature(Long.class.getName()),
      SignatureCache.createTypeSignature(Map.class.getName()),
      SignatureCache.createTypeSignature(java.lang.Number.class.getName()),
      SignatureCache.createTypeSignature(Object.class.getName()),
      SignatureCache.createTypeSignature(java.lang.Runnable.class.getName()),
      SignatureCache.createTypeSignature(Set.class.getName()),
      SignatureCache.createTypeSignature(String.class.getName()),
      SignatureCache.createTypeSignature(TreeMap.class.getName()),
      SignatureCache.createTypeSignature(TreeSet.class.getName()),
      SignatureCache.createTypeSignature(java.util.Vector.class.getName())
  };

  private SearchEngine m_searchEngine;
  private final boolean m_supportsGenerics;
  private String[] m_primitivSignatures;
  private final IJavaSearchScope m_searchScope;
  private final String[] m_mostUsedSignatures;
  private final ILabelProvider m_labelProvider;
  private int m_maxProposalAmount = 100; // default

  public SignatureProposalProvider(IJavaSearchScope searchScope, ILabelProvider labelProvider, String[] mostUsedSignatures, boolean supportsGenerics) {
    m_searchScope = searchScope;
    m_labelProvider = labelProvider;
    m_mostUsedSignatures = mostUsedSignatures;
    m_supportsGenerics = supportsGenerics;
    m_searchEngine = new SearchEngine();
  }

  public IJavaSearchScope getSearchScope() {
    return m_searchScope;
  }

  public ILabelProvider getLabelProvider() {
    return m_labelProvider;
  }

  public String[] getMostUsedSignatures() {
    return m_mostUsedSignatures;
  }

  public String[] getPrimitivSignatures() {
    return m_primitivSignatures;
  }

  public void setPrimitivSignatures(String[] primitivSignatures) {
    m_primitivSignatures = primitivSignatures;
  }

  public boolean isSupportsGenerics() {
    return m_supportsGenerics;
  }

  public void setMaxProposalAmount(int maxProposalAmount) {
    m_maxProposalAmount = maxProposalAmount;
  }

  public int getMaxProposalAmount() {
    return m_maxProposalAmount;
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (!StringUtility.hasText(searchPattern)) {
      searchPattern = "*";
    }
    else {
      searchPattern = searchPattern.replaceAll("\\*$", "") + "*";
    }
    int counter = 0;
    ArrayList<Object> result = new ArrayList<Object>(Math.min(getMaxProposalAmount(), 100));
    char[] pattern = CharOperation.toLowerCase(searchPattern.toCharArray());
    if (getPrimitivSignatures() != null) {
      for (String sig : getPrimitivSignatures()) {
        if (CharOperation.match(pattern, getLabelProvider().getText(sig).toCharArray(), false)) {
          if (++counter > getMaxProposalAmount()) {
            return result.toArray(new Object[result.size()]);
          }
          result.add(sig);
        }
      }
    }

    if (m_mostUsedSignatures != null) {
      for (String signature : m_mostUsedSignatures) {
        if (CharOperation.match(pattern, getLabelProvider().getText(signature).toCharArray(), false)) {
          if (++counter > getMaxProposalAmount()) {
            return result.toArray(new Object[result.size()]);
          }
          result.add(signature);
        }
      }
    }

    Collection<Object> searchResult = collectTypes(searchPattern, getMaxProposalAmount() - counter - 1, monitor);
    if (searchResult.size() > 0) {
      if (result.size() > 0) {
        result.add(MoreElementsProposal.INSTANCE);
      }
      result.addAll(searchResult);
    }
    return result.toArray(new Object[result.size()]);
  }

  private Collection<Object> collectTypes(String searchPattern, int maxResultSize, IProgressMonitor monitor) {
    P_SearchRequestor searchRequestor = new P_SearchRequestor(monitor, maxResultSize);
    // do not allow empty search
    if (searchPattern.length() == 1) {
      return searchRequestor.getResult();
    }
    try {

      SearchPattern p = SearchPattern.createPattern(
          searchPattern,
          IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS,
          SearchPattern.R_PATTERN_MATCH);

      // pattern can be null if the user enters an in ill-formed string -> no proposals should be found then.
      if (p != null) {
        m_searchEngine.search(p, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, m_searchScope, searchRequestor, null);
      }
    }
    catch (CoreException e) {
      if (e.getStatus().matches(IStatus.ERROR)) {
        ScoutSdkUi.logWarning(e);
        return searchRequestor.getResult();
      }
      if (e.getStatus().matches(IStatus.CANCEL)) {
        return searchRequestor.getResult();
      }
    }
    return searchRequestor.getResult();
  }

  private class P_SearchRequestor extends SearchRequestor {
    private IProgressMonitor m_monitor;
    private TreeMap<CompositeObject, Object> m_foundTypes;
    private int m_requestedDecrementCounter;

    public P_SearchRequestor(IProgressMonitor monitor, int maxResultSize) {
      m_monitor = monitor;
      m_requestedDecrementCounter = maxResultSize;
      m_foundTypes = new TreeMap<CompositeObject, Object>();
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) throws CoreException {
      if (m_monitor.isCanceled()) {
        throw new CoreException(new Status(IStatus.CANCEL, ScoutSdkUi.PLUGIN_ID, "canceled by monitor"));
      }
      if (match instanceof TypeDeclarationMatch) {
        IType type = (IType) match.getElement();
        if (!m_supportsGenerics && (type.getTypeParameters().length > 0)) {
          return;
        }
        m_foundTypes.put(new CompositeObject("A", type.getElementName(), type.getFullyQualifiedName()), SignatureCache.createTypeSignature(type.getFullyQualifiedName()));
        if (--m_requestedDecrementCounter <= 0) {
          throw new CoreException(new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "stopped after " + getMaxProposalAmount() + ""));
        }

      }
    }

    public Collection<Object> getResult() {
      return m_foundTypes.values();
    }

  }
}
