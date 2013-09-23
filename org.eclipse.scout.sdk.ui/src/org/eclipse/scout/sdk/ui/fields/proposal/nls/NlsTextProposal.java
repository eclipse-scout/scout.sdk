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
package org.eclipse.scout.sdk.ui.fields.proposal.nls;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider.NormalizedPattern;

/**
 * <h3>{@link NlsTextProposal}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 27.11.2012
 */
public class NlsTextProposal implements Comparable<NlsTextProposal> {

  public final static NlsTextProposal NEW_NLS_TEXT_PROPOSAL = new NlsTextProposal(null, null, true);

  public final static int MATCH_DEV_LANG_TRANSLATION = 1;
  public final static int MATCH_KEY = 2;
  public final static int MATCH_FOREIGN_LANG = 4;

  private final static Pattern REGEX_CR = Pattern.compile("\r", Pattern.LITERAL);
  private final static Pattern REGEX_LF = Pattern.compile("\n", Pattern.LITERAL);

  private final static String KEY_PREFIX = " (";
  private final static String FOREIGN_LANG_PREFIX = "=";

  private int m_matchKind;
  private String m_matchedForeignLangTranslation;

  private final boolean m_isNewEntryProposal;
  private final INlsEntry m_entry;
  private final Language m_developmentLanguage;
  private final String m_devLangTranslation;
  private final String[] m_foreignLangTranslations;

  public NlsTextProposal(INlsEntry entry, Language developmentLanguage) {
    this(entry, developmentLanguage, false);
  }

  private NlsTextProposal(INlsEntry entry, Language developmentLanguage, boolean isNewEntryProposal) {
    m_entry = entry;
    m_developmentLanguage = developmentLanguage;

    if (m_entry != null) {
      m_devLangTranslation = m_entry.getTranslation(developmentLanguage, true);

      Map<Language, String> allTranslations = entry.getAllTranslations();
      ArrayList<String> foreignLangTranslations = new ArrayList<String>(allTranslations.size());
      for (Entry<Language, String> e : allTranslations.entrySet()) {
        if (!developmentLanguage.equals(e.getKey())) {
          foreignLangTranslations.add(e.getValue());
        }
      }
      m_foreignLangTranslations = foreignLangTranslations.toArray(new String[foreignLangTranslations.size()]);
    }
    else {
      m_devLangTranslation = null;
      m_foreignLangTranslations = null;
    }

    m_isNewEntryProposal = isNewEntryProposal;
  }

  public INlsEntry getEntry() {
    return m_entry;
  }

  public boolean matches(NormalizedPattern pattern) {
    m_matchedForeignLangTranslation = null;
    if (matches(getDevLangTranslation(), pattern)) {
      m_matchKind = MATCH_DEV_LANG_TRANSLATION;
      return true;
    }
    if (matches(getKey(), pattern)) {
      m_matchKind = MATCH_KEY;
      return true;
    }
    for (String s : getForeignLangTranslations()) {
      if (matches(s, pattern)) {
        m_matchKind = MATCH_FOREIGN_LANG;
        m_matchedForeignLangTranslation = s;
        return true;
      }
    }
    m_matchKind = 0;
    return false;
  }

  public String getMatchedForeignLangTranslation() {
    return m_matchedForeignLangTranslation;
  }

  public int[] getMatchingRegions(NormalizedPattern pattern) {
    int offset = 0;
    switch (getMatchKind()) {
      case MATCH_DEV_LANG_TRANSLATION:
        return ContentProposalProvider.getMatchingRegions(this, getDevLangTranslation(), pattern);
      case MATCH_KEY:
        offset = getDevLangTranslation().length() + KEY_PREFIX.length();
        return adapt(ContentProposalProvider.getMatchingRegions(this, getKey(), pattern), offset);
      case MATCH_FOREIGN_LANG:
        offset = getDevLangTranslation().length() + KEY_PREFIX.length() + getKey().length() + FOREIGN_LANG_PREFIX.length();
        return adapt(ContentProposalProvider.getMatchingRegions(this, getMatchedForeignLangTranslation(), pattern), offset);
    }
    return null;
  }

  public String getDisplayText() {
    StringBuilder sb = new StringBuilder(getDevLangTranslation());
    sb.append(KEY_PREFIX);
    sb.append(getKey());
    if (getMatchKind() == MATCH_FOREIGN_LANG) {
      sb.append(FOREIGN_LANG_PREFIX);
      sb.append(getMatchedForeignLangTranslation());
    }
    sb.append(")");
    String ret = sb.toString();
    ret = REGEX_CR.matcher(ret).replaceAll("");
    ret = REGEX_LF.matcher(ret).replaceAll(" ");
    return ret;
  }

  private int[] adapt(int[] matchingRegions, int offset) {
    for (int i = 0; i < matchingRegions.length; i += 2) {
      matchingRegions[i] += offset;
    }
    return matchingRegions;
  }

  public int getMatchKind() {
    return m_matchKind;
  }

  public String getKey() {
    return m_entry.getKey();
  }

  public String getDevLangTranslation() {
    return m_devLangTranslation;
  }

  public String[] getForeignLangTranslations() {
    return m_foreignLangTranslations;
  }

  private static boolean matches(String text, NormalizedPattern pattern) {
    return SearchPattern.getMatchingRegions(pattern.getPattern(), text, pattern.getMatchKind()) != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NlsTextProposal)) {
      return false;
    }
    return CompareUtility.equals(m_entry, ((NlsTextProposal) obj).m_entry);
  }

  @Override
  public int hashCode() {
    if (m_entry == null) {
      return 0;
    }
    return m_entry.hashCode();
  }

  private String getSortString() {
    if (getMatchKind() == MATCH_DEV_LANG_TRANSLATION) {
      return StringUtility.lowercase(getDevLangTranslation());
    }
    else if (getMatchKind() == MATCH_KEY) {
      return StringUtility.lowercase(getKey());
    }
    else {
      return StringUtility.lowercase(getMatchedForeignLangTranslation());
    }
  }

  @Override
  public int compareTo(NlsTextProposal o) {
    return CompareUtility.compareTo(getSortString(), o.getSortString());
  }

  public boolean isNewEntryProposal() {
    return m_isNewEntryProposal;
  }

  public Language getDevelopmentLanguage() {
    return m_developmentLanguage;
  }
}
