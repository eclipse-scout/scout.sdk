/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.fields.proposal.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.NormalizedPattern;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils.ElementNameComparator;
import org.eclipse.scout.sdk.s2e.util.S2eUtils.PublicPrimaryTypeFilter;

import com.google.common.base.Objects;

/**
 * <h3>{@link TypeContentProvider}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class TypeContentProvider extends StrictHierarchyTypeContentProvider {

  private NormalizedPattern m_lastPattern;
  private int m_maxProposalCount;
  private final List<String> m_mostlyUsedTypes;

  public TypeContentProvider(IJavaProject project) {
    super(project, null);
    m_maxProposalCount = 100;
    m_mostlyUsedTypes = new ArrayList<>(Arrays.asList(
        IJavaRuntimeTypes.java_lang_Boolean,
        IJavaRuntimeTypes.java_lang_Byte,
        IJavaRuntimeTypes.java_lang_Character,
        IJavaRuntimeTypes.java_lang_CharSequence,
        IJavaRuntimeTypes.java_lang_Integer,
        IJavaRuntimeTypes.java_lang_Long,
        IJavaRuntimeTypes.java_lang_Number,
        IJavaRuntimeTypes.java_lang_String,
        IJavaRuntimeTypes.java_math_BigDecimal,
        IJavaRuntimeTypes.java_math_BigInteger,
        IJavaRuntimeTypes.java_util_Collection,
        IJavaRuntimeTypes.java_util_Date,
        IJavaRuntimeTypes.java_util_List,
        IJavaRuntimeTypes.java_util_Map,
        IJavaRuntimeTypes.java_util_Set));
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
    if (!Objects.equal(m_lastPattern, searchPattern)) {
      m_lastPattern = searchPattern;
      clearCache();
    }

    return super.getProposals(searchPattern, monitor);
  }

  protected boolean addProposalCandidate(IType candidate, Collection<IType> candidates) {
    if (candidates.size() >= getMaxProposalCount()) {
      return false;// no more space
    }
    if (S2eUtils.exists(candidate)) {
      candidates.add(candidate);
    }
    return candidates.size() < getMaxProposalCount();
  }

  @Override
  protected Collection<? extends Object> loadProposals(IProgressMonitor monitor) {
    if (StringUtils.isNotBlank(getBaseClassFqn()) && !IJavaRuntimeTypes.java_lang_Object.equals(getBaseClassFqn())) {
      // we have a hierarchy base type: use super implementation to calculate all possible children
      return super.loadProposals(monitor);
    }

    // we do not have a hierarchy defined: calc possible references
    Set<IType> result = new TreeSet<>(new ElementNameComparator());
    addMostlyUsedCandidates(result, monitor);

    int remainingSpace = getMaxProposalCount() - result.size();
    if (remainingSpace > 0) {
      addOtherCandidates(result, monitor);
    }
    return result;
  }

  protected void addMostlyUsedCandidates(Set<IType> candidates, IProgressMonitor monitor) {
    IJavaProject javaProject = getJavaProject();
    if (!S2eUtils.exists(javaProject)) {
      return;
    }

    IFilter<IType> filter = getTypeProposalFilter();
    for (String fqn : m_mostlyUsedTypes) {
      if (monitor.isCanceled()) {
        return;
      }
      int[] matchRegions = m_lastPattern.getMatchingRegions(Signature.getSimpleName(fqn));
      if (matchRegions != null) {
        try {
          IType type = javaProject.findType(fqn);
          if ((filter == null || filter.evaluate(type)) && !addProposalCandidate(type, candidates)) {
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
    IJavaProject javaProject = getJavaProject();
    if (!S2eUtils.exists(javaProject)) {
      return;
    }

    P_SearchRequestor requestor = new P_SearchRequestor(candidates, monitor);
    SearchPattern pat = SearchPattern.createPattern(m_lastPattern.getPattern(), IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, m_lastPattern.getMatchRule());
    IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject});
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
    private final IFilter<IType> m_typeProposalFilter;

    private P_SearchRequestor(Set<IType> collector, IProgressMonitor monitor) {
      m_collector = collector;
      m_monitor = monitor;
      m_typeProposalFilter = getTypeProposalFilter();
    }

    @Override
    public void acceptSearchMatch(SearchMatch m) throws CoreException {
      if (m_monitor.isCanceled()) {
        throw new OperationCanceledException("type lookup canceled because monitor has been canceled.");
      }
      if (!(m instanceof TypeDeclarationMatch)) {
        return;
      }

      TypeDeclarationMatch match = (TypeDeclarationMatch) m;
      IType type = (IType) match.getElement();
      if (!S2eUtils.exists(type)) {
        return;
      }
      if (m_typeProposalFilter != null && !m_typeProposalFilter.evaluate(type)) {
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
