/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal.content;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.util.NormalizedPattern;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.ElementNameComparator;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.PublicPrimaryTypeFilter;

/**
 * <h3>{@link TypeContentProvider}</h3>
 *
 * @since 5.2.0
 */
public class TypeContentProvider extends StrictHierarchyTypeContentProvider {

  @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
  private NormalizedPattern m_lastPattern;
  private int m_maxProposalCount;
  private final List<String> m_mostlyUsedTypes;

  public TypeContentProvider(IJavaProject project) {
    super(project, null);
    m_maxProposalCount = 100;
    m_mostlyUsedTypes = new ArrayList<>(Arrays.asList(
        JavaTypes.Boolean,
        JavaTypes.Byte,
        JavaTypes.Character,
        CharSequence.class.getName(),
        JavaTypes.Integer,
        JavaTypes.Long,
        Number.class.getName(),
        String.class.getName(),
        BigDecimal.class.getName(),
        BigInteger.class.getName(),
        Collection.class.getName(),
        List.class.getName(),
        Map.class.getName(),
        Set.class.getName()));
    setTypeProposalFilter(new PublicPrimaryTypeFilter());
  }

  @Override
  public synchronized void dispose() {
    super.dispose();
    m_lastPattern = null;
    m_mostlyUsedTypes.clear();
  }

  @Override
  public Collection<Object> getProposals(NormalizedPattern searchPattern, IProgressMonitor monitor) {
    if (!Objects.equals(m_lastPattern, searchPattern)) {
      m_lastPattern = searchPattern;
      clearCache();
    }

    return super.getProposals(searchPattern, monitor);
  }

  protected boolean addProposalCandidate(IType candidate, Collection<IType> candidates) {
    if (candidates.size() >= getMaxProposalCount()) {
      return false;// no more space
    }
    if (JdtUtils.exists(candidate)) {
      candidates.add(candidate);
    }
    return candidates.size() < getMaxProposalCount();
  }

  @Override
  protected Collection<?> loadProposals(IProgressMonitor monitor) {
    if (Strings.hasText(getBaseClassFqn()) && !Object.class.getName().equals(getBaseClassFqn())) {
      // we have a hierarchy base type: use super implementation to calculate all possible children
      return super.loadProposals(monitor);
    }

    // we do not have a hierarchy defined: calc possible references
    Set<IType> result = new TreeSet<>(new ElementNameComparator());
    addMostlyUsedCandidates(result, monitor);

    var remainingSpace = getMaxProposalCount() - result.size();
    if (remainingSpace > 0) {
      addOtherCandidates(result, monitor);
    }
    return result;
  }

  protected void addMostlyUsedCandidates(Collection<IType> candidates, IProgressMonitor monitor) {
    var javaProject = getJavaProject();
    if (!JdtUtils.exists(javaProject)) {
      return;
    }

    var filter = getTypeProposalFilter();
    for (var fqn : m_mostlyUsedTypes) {
      if (monitor.isCanceled()) {
        return;
      }
      var matches = m_lastPattern.getMatchingRegions(JavaTypes.simpleName(fqn)) != null;
      if (matches) {
        try {
          var type = javaProject.findType(fqn);
          if ((filter == null || filter.test(type)) && !addProposalCandidate(type, candidates)) {
            return;
          }
        }
        catch (JavaModelException e) {
          SdkLog.warning("Unable to find type {} in project {}.", fqn, javaProject.getElementName(), e);
        }
      }
    }
  }

  protected void addOtherCandidates(Set<IType> candidates, IProgressMonitor monitor) {
    if (m_lastPattern.isEmpty()) {
      // do not allow empty search
      return;
    }
    var javaProject = getJavaProject();
    if (!JdtUtils.exists(javaProject)) {
      return;
    }

    SearchRequestor requestor = new P_SearchRequestor(candidates, monitor);
    var pat = SearchPattern.createPattern(m_lastPattern.getPattern(), IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, m_lastPattern.getMatchRule());
    var searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject});
    try {
      new SearchEngine().search(pat, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, searchScope, requestor, monitor);
    }
    catch (OperationCanceledException e) {
      SdkLog.debug("SearchEngine lookup canceled.", e);
    }
    catch (CoreException e) {
      SdkLog.warning("Unable to search for type candidates in project '{}' matching pattern '{}'.", javaProject.getElementName(), m_lastPattern.getPattern(), e);
    }
  }

  public int getMaxProposalCount() {
    return m_maxProposalCount;
  }

  public void setMaxProposalCount(int maxProposalCount) {
    if (m_maxProposalCount == maxProposalCount) {
      return;
    }
    m_maxProposalCount = maxProposalCount;
    clearCache();
  }

  public void addMostlyUsedType(String type) {
    m_mostlyUsedTypes.add(type);
    clearCache();
  }

  public void setMostlyUsedTypes(Collection<String> types) {
    m_mostlyUsedTypes.clear();
    m_mostlyUsedTypes.addAll(types);
    clearCache();
  }

  public void removeMostlyUsedType(String type) {
    m_mostlyUsedTypes.remove(type);
    clearCache();
  }

  private final class P_SearchRequestor extends SearchRequestor {

    private final IProgressMonitor m_monitor;
    private final Set<IType> m_collector;
    private final Predicate<IType> m_typeProposalFilter;

    private P_SearchRequestor(Set<IType> collector, IProgressMonitor monitor) {
      m_collector = collector;
      m_monitor = monitor;
      m_typeProposalFilter = getTypeProposalFilter();
    }

    @Override
    public void acceptSearchMatch(SearchMatch m) {
      if (m_monitor.isCanceled()) {
        throw new OperationCanceledException("type lookup canceled because monitor has been canceled.");
      }
      if (!(m instanceof TypeDeclarationMatch match)) {
        return;
      }

      var type = (IType) match.getElement();
      if (!JdtUtils.exists(type)) {
        return;
      }
      if (m_typeProposalFilter != null && !m_typeProposalFilter.test(type)) {
        return;
      }
      if (m_collector.contains(type)) {
        return;
      }

      if (!addProposalCandidate(type, m_collector)) {
        throw new OperationCanceledException("type lookup canceled because max number of candidates reached.");
      }
    }
  }
}
