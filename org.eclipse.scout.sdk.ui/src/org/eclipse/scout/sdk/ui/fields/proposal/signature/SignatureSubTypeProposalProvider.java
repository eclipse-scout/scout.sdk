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
package org.eclipse.scout.sdk.ui.fields.proposal.signature;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;

/**
 * <h3>{@link SignatureSubTypeProposalProvider}</h3> Proposal provider that returns all signature proposals that are
 * subtypes of a given base type.
 * 
 * @author Matthias Villiger
 * @since 3.10.0 24.01.2014
 */
public class SignatureSubTypeProposalProvider extends SignatureProposalProvider {

  private IType m_baseType;
  private final IJavaProject m_classpath;

  public SignatureSubTypeProposalProvider(IType baseType, IJavaProject classpath) {
    super(SearchEngine.createJavaSearchScope(new IJavaElement[]{classpath}), new SignatureLabelProvider(), null, false);
    setBaseType(baseType);
    m_classpath = classpath;
  }

  public SignatureSubTypeProposalProvider(String baseTypeSig, IJavaProject classpath) {
    this(TypeUtility.getTypeBySignature(baseTypeSig), classpath);
  }

  @Override
  protected Collection<Object> collectTypes(String searchPattern, int maxResultSize, IProgressMonitor monitor) {
    if (!isBaseTypeDefined()) {
      return super.collectTypes(searchPattern, maxResultSize, monitor);
    }

    TreeSet<Object> signatures = new TreeSet<Object>();
    if (TypeUtility.exists(getBaseType())) {
      NormalizedPattern normalizedPattern = createNormalizedSearchPattern(searchPattern);
      String parentTypeSig = SignatureCache.createTypeSignature(getBaseType().getFullyQualifiedName());
      int[] matchRegions = getMatchingRegions(parentTypeSig, getLabelProvider().getText(parentTypeSig), normalizedPattern);
      if (matchRegions != null) {
        signatures.add(parentTypeSig);
      }

      IPrimaryTypeTypeHierarchy hier = TypeUtility.getPrimaryTypeHierarchy(getBaseType());
      for (IType t : hier.getAllSubtypes(getBaseType(), null, TypeComparators.getTypeNameComparator())) {
        String curSig = SignatureCache.createTypeSignature(t.getFullyQualifiedName());
        matchRegions = getMatchingRegions(curSig, getLabelProvider().getText(curSig), normalizedPattern);
        if (matchRegions != null && TypeUtility.isOnClasspath(t, m_classpath)) {
          signatures.add(curSig);

          if (signatures.size() == maxResultSize) {
            break;
          }
        }
      }
    }
    return signatures;
  }

  public IType getBaseType() {
    return m_baseType;
  }

  private boolean isBaseTypeDefined() {
    return TypeUtility.exists(getBaseType()) && !Object.class.getName().equals(getBaseType().getFullyQualifiedName());
  }

  public void setBaseType(IType baseType) {
    m_baseType = baseType;
    if (isBaseTypeDefined()) {
      setMostUsedSignatures(null);
    }
    else {
      setMostUsedSignatures(DEFAULT_MOST_USED);
    }
  }
}
