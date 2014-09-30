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
package org.eclipse.scout.sdk.ui.fields.javacode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ui.fields.proposal.ISeparatorProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

public class JavaCodeFieldContentProvider implements IContentProposalProvider {

  private final IJavaSearchScope m_seachScope;
  private SearchEngine m_searchEngine;

  public JavaCodeFieldContentProvider(IJavaSearchScope seachScope) {
    m_seachScope = seachScope;
    m_searchEngine = new SearchEngine();

  }

  public IContentProposal[] findExactMatch(String input) {
    String searchText = parseSearchText(input);
    if (searchText == null) {
      return new IContentProposal[]{};
    }
    ArrayList<IContentProposal> collector = new ArrayList<IContentProposal>();
    collectTypes(searchText, collector, SearchPattern.R_PATTERN_MATCH, null);
    return collector.toArray(new IContentProposal[collector.size()]);
  }

  @Override
  public IContentProposal[] getProposals(String contents, int position) {
    String searchText = parseSearchText(contents.substring(0, position));
    if (searchText == null) {
      return new IContentProposal[]{};
    }
    searchText = searchText + "*";
    // SearchPattern.R_PATTERN_MATCH
    ArrayList<IContentProposal> proposals = new ArrayList<IContentProposal>();
    collectTypes(searchText, proposals, SearchPattern.R_PATTERN_MATCH, null);
    return proposals.toArray(new IContentProposal[proposals.size()]);
  }

  private String parseSearchText(String contents) {
    Matcher m = Pattern.compile("([a-zA-Z.$_0-9]+)$").matcher(contents);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  private void collectTypes(String searchText, ArrayList<IContentProposal> collector, int matchStrategy, IProgressMonitor monitor) {
    P_SearchRequestor searchRequestor = new P_SearchRequestor(monitor);
    try {
      m_searchEngine.search(
          SearchPattern.createPattern(
              searchText,
              IJavaSearchConstants.TYPE,
              IJavaSearchConstants.DECLARATIONS,
              matchStrategy
              ),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          m_seachScope,
          searchRequestor
          ,
          null
          );
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
    ArrayList<IContentProposal> props = new ArrayList<IContentProposal>(searchRequestor.getResult());
    if (props.size() > 0) {
      if (props.get(0) instanceof ISeparatorProposal) {
        props.remove(0);
      }
      else if (props.get(props.size() - 1) instanceof ISeparatorProposal) {
        props.remove(props.size() - 1);
      }
    }
    collector.addAll(props);
  }

  private class P_SearchRequestor extends org.eclipse.jdt.core.search.SearchRequestor {
    private TreeMap<CompositeObject, IContentProposal> m_foundTypes = new TreeMap<CompositeObject, IContentProposal>();
    private int m_counter = 0;

    public P_SearchRequestor(IProgressMonitor monitor) {
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) throws CoreException {
      if (match instanceof TypeDeclarationMatch) {
        IType type = (IType) match.getElement();
        if (type.getFullyQualifiedName().contains(".internal.")) {
          return;
        }
        if (type.getDeclaringType() != null) {
          return;
        }
        if (type.isBinary()) {
          m_foundTypes.put(new CompositeObject("C", type.getElementName(), type.getFullyQualifiedName()), new JavaTypeProposal(type));
        }
        else {
          m_foundTypes.put(new CompositeObject("A", type.getElementName(), type.getFullyQualifiedName()), new JavaTypeProposal(type));
        }
        if (m_counter++ > 98) {
          throw new CoreException(new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "stopped after 50"));
        }
      }
    }

    public Collection<IContentProposal> getResult() {
      return m_foundTypes.values();
    }
  }

}
