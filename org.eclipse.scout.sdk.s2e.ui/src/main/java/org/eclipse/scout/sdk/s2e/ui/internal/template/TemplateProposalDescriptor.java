/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.ScoutModelHierarchy;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;

/**
 * <h3>{@link TemplateProposalDescriptor}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("squid:S00107")
public class TemplateProposalDescriptor {

  private static final Pattern CAMEL_PAT = Pattern.compile("([A-Z])");

  private final String m_proposalIfcTypeFqn;
  private final Deque<String> m_defaultSuperTypeFqns;
  private final Set<String> m_aliasNames;
  private final Class<? extends AbstractTypeProposal> m_proposalClass;

  private String m_defaultNameOfNewType;
  private String m_typeSuffix;
  private String m_imgId;
  private int m_relevance;
  private String m_displayName;

  protected TemplateProposalDescriptor(String proposalIfcTypeFqn, String defaultSuperTypeFqn, String defaultNameOfNewType, String typeSuffix, String imgId,
      int relevance, Class<? extends AbstractTypeProposal> proposal) {
    this(proposalIfcTypeFqn, defaultSuperTypeFqn, defaultNameOfNewType, typeSuffix, imgId, relevance, proposal, null);
  }

  protected TemplateProposalDescriptor(String proposalIfcTypeFqn, String defaultSuperTypeFqn, String defaultNameOfNewType, String typeSuffix, String imgId,
      int relevance, Class<? extends AbstractTypeProposal> proposal, Collection<String> alias) {
    this(proposalIfcTypeFqn, defaultSuperTypeFqn, defaultNameOfNewType, typeSuffix, imgId, relevance, proposal, alias, createDisplayNameFromIfc(proposalIfcTypeFqn));
  }

  protected TemplateProposalDescriptor(String proposalIfcTypeFqn, String defaultSuperTypeFqn, String defaultNameOfNewType, String typeSuffix, String imgId,
      int relevance, Class<? extends AbstractTypeProposal> proposal, Collection<String> alias, String displayName) {
    m_proposalIfcTypeFqn = proposalIfcTypeFqn;
    m_defaultSuperTypeFqns = new ArrayDeque<>();
    m_defaultSuperTypeFqns.add(defaultSuperTypeFqn);
    if (alias == null || alias.isEmpty()) {
      m_aliasNames = new HashSet<>(0);
    }
    else {
      m_aliasNames = new HashSet<>(alias);
    }
    m_defaultNameOfNewType = defaultNameOfNewType;
    m_typeSuffix = typeSuffix;
    m_imgId = imgId;
    m_relevance = relevance;
    m_displayName = displayName;
    m_proposalClass = proposal;
  }

  public boolean isActiveFor(Collection<String> possibleChildren, IJavaProject context, String searchString) {
    if (context == null) {
      return false;
    }
    if (possibleChildren == null || possibleChildren.isEmpty()) {
      return false;
    }

    for (String possibleChild : possibleChildren) {
      if (ScoutModelHierarchy.isSubtypeOf(m_proposalIfcTypeFqn, possibleChild) && acceptSearchString(searchString)) {
        return true;
      }
    }
    return false;
  }

  protected boolean acceptSearchString(String searchString) {
    if (Strings.isBlank(searchString)) {
      return true; // no filter
    }

    searchString = searchString.toLowerCase(Locale.ENGLISH);
    if (JavaTypes.simpleName(m_proposalIfcTypeFqn).toLowerCase(Locale.ENGLISH).contains(searchString)) {
      return true;
    }

    if (m_displayName.toLowerCase(Locale.ENGLISH).contains(searchString)) {
      return true;
    }

    for (String defaultSuperType : m_defaultSuperTypeFqns) {
      String simpleName = JavaTypes.simpleName(defaultSuperType);
      if (simpleName.startsWith(ISdkProperties.PREFIX_ABSTRACT)) {
        simpleName = simpleName.substring(ISdkProperties.PREFIX_ABSTRACT.length());
      }
      if (simpleName.toLowerCase(Locale.ENGLISH).contains(searchString)) {
        return true;
      }
    }

    for (String alias : m_aliasNames) {
      if (alias.toLowerCase(Locale.ENGLISH).contains(searchString)) {
        return true;
      }
    }
    return false;
  }

  public ICompletionProposal createProposal(ICompilationUnit icu, int pos, ISourceRange surroundingTypeNameRange, Future<EclipseEnvironment> provider, String searchString) {
    try {
      TypeProposalContext context = new TypeProposalContext();
      context.setProvider(provider);
      context.setDefaultName(getDefaultNameOfNewType());
      context.setDefaultSuperClasses(getDefaultSuperTypeFqns());
      context.setPosition(pos);
      context.setSuffix(getTypeSuffix());
      context.setProposalInterfaceFqn(getProposalInterfaceFqn());
      context.setIcu(icu);
      context.setSurroundingTypeNameRange(surroundingTypeNameRange);
      context.setSearchString(searchString);

      return (ICompletionProposal) m_proposalClass.getConstructors()[0].newInstance(getDisplayName(), getRelevance(), getImageId(), icu, context);
    }
    catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
      throw new SdkException("Unable to create proposal '" + m_proposalClass.getName() + "'.", e);
    }
  }

  protected static String createDisplayNameFromIfc(CharSequence ifcFqn) {
    String simpleName = JavaTypes.simpleName(ifcFqn);
    if (!simpleName.isEmpty() && simpleName.charAt(0) == 'I') {
      simpleName = simpleName.substring(1);
    }
    return CAMEL_PAT.matcher(simpleName).replaceAll(" $1");
  }

  public Deque<String> getDefaultSuperTypeFqns() {
    return m_defaultSuperTypeFqns;
  }

  public String getDefaultNameOfNewType() {
    return m_defaultNameOfNewType;
  }

  public void setDefaultNameOfNewType(String defaultNameOfNewType) {
    m_defaultNameOfNewType = defaultNameOfNewType;
  }

  public String getTypeSuffix() {
    return m_typeSuffix;
  }

  public void setTypeSuffix(String typeSuffix) {
    m_typeSuffix = typeSuffix;
  }

  public String getImageId() {
    return m_imgId;
  }

  public void setImageId(String imgId) {
    m_imgId = imgId;
  }

  public int getRelevance() {
    return m_relevance;
  }

  public void setRelevance(int relevance) {
    m_relevance = relevance;
  }

  public String getDisplayName() {
    return m_displayName;
  }

  public void setDisplayName(String displayName) {
    m_displayName = displayName;
  }

  public Class<? extends AbstractTypeProposal> getProposal() {
    return m_proposalClass;
  }

  public String getProposalInterfaceFqn() {
    return m_proposalIfcTypeFqn;
  }

  public Set<String> getAliasNames() {
    return m_aliasNames;
  }
}
