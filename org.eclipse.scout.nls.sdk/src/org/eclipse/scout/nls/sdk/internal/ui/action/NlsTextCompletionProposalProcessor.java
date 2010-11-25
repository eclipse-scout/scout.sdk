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
package org.eclipse.scout.nls.sdk.internal.ui.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.workspace.NlsWorkspace;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <h4>TextCompletionProposalProcessor</h4>
 */
public class NlsTextCompletionProposalProcessor implements IContentAssistProcessor {
  private final Image m_image;

  private static final class Proposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4 {

    private final INlsEntry m_nlsEntry;
    private final String m_prefix;
    private final int m_offset;
    private final Image m_image;

    public Proposal(INlsEntry nlsEntry, String prefix, int offset, Image image) {
      m_nlsEntry = nlsEntry;
      m_prefix = prefix;
      m_offset = offset;
      m_image = image;
    }

    public void apply(IDocument document) {
      apply(null, '\0', 0, m_offset);
    }

    public Point getSelection(IDocument document) {
      return new Point(m_offset - m_prefix.length() + m_nlsEntry.getKey().length(), 0);
    }

    public String getAdditionalProposalInfo() {
      // html
      Map<Language, String> allTranslations = m_nlsEntry.getAllTranslations();
      if (allTranslations != null && allTranslations.size() > 0) {
        StringBuilder b = new StringBuilder();
        for (Entry<Language, String> e : allTranslations.entrySet()) {
          b.append("'<b>" + e.getValue() + "</b>' [" + e.getKey().getDispalyName() + "]<br>");
        }
        return b.toString();
      }
      else {
        return null;
      }

    }

    public String getDisplayString() {
      return m_nlsEntry.getKey();
    }

    public Image getImage() {
      return m_image;
    }

    public IContextInformation getContextInformation() {
      return null;
    }

    public void apply(IDocument document, char trigger, int offset) {
      int offDiff = offset - m_offset;
      ReplaceEdit replaceEdit = new ReplaceEdit(m_offset - m_prefix.length(), m_prefix.length() + offDiff, m_nlsEntry.getKey());
      try {
        replaceEdit.apply(document);
      }
      catch (Exception e) {
        NlsCore.logWarning(e);
      }
    }

    public boolean isValidFor(IDocument document, int offset) {
      return validate(document, offset, null);
    }

    public char[] getTriggerCharacters() {
      return null;
    }

    public int getContextInformationPosition() {
      return 0;
    }

    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
      apply(viewer.getDocument(), trigger, offset);
    }

    public void selected(ITextViewer viewer, boolean smartToggle) {
    }

    public void unselected(ITextViewer viewer) {
    }

    public boolean validate(IDocument document, int offset, DocumentEvent event) {
      try {
        String prefix = m_prefix + document.get(m_offset, offset - m_offset);
        return m_nlsEntry.getKey().toLowerCase().startsWith(prefix.toLowerCase());
      }
      catch (BadLocationException e) {
        NlsCore.logWarning(e);
        return false;
      }
    }

    public IInformationControlCreator getInformationControlCreator() {
      return null;
    }

    public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
      return m_nlsEntry.getKey();
    }

    public int getPrefixCompletionStart(IDocument document, int completionOffset) {
      return m_offset - m_prefix.length();
    }

    public boolean isAutoInsertable() {
      return true;
    }

  }

  private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
  private static final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];

  private final Map<String, IProject> m_projectMap = new HashMap<String, IProject>();

  public NlsTextCompletionProposalProcessor() {
    m_image = NlsCore.getImage(NlsCore.ICON_COMMENT);
  }

  /**
   * Searches the nls project according to the offset.
   * <p>
   * It searches there for the nls-class (normaly this would be Texts). With the class name it would search for the
   * qname in the current document. With help of the {@link SearchEngine} it retrieves the actual type in which the
   * project can be found. In the {@link NlsWorkspace} it finds the corresponding {@link INlsProject}.
   *
   * @param viewer
   *          the viewer whose document is used to compute the proposals
   * @param offset
   *          an offset within the document for which completions should be computed
   * @return corresponding {@link INlsProject} or <code>null</code> if not found
   */
  private INlsProject searchNlsProject(ITextViewer viewer, int offset) {
    int tmpOffset = offset;
    IDocument doc = viewer.getDocument();
    if (doc == null || tmpOffset > doc.getLength()) {
      return null;
    }
    String qName = null;
    IProject project = null;

    try {
      int length = 0;
      while (--tmpOffset >= 0 && Character.isLetterOrDigit(doc.getChar(tmpOffset))) {
        length++;
      }

      String s = ".get(\"";
      String s2 = doc.get(tmpOffset - s.length() + 1, s.length());
      if (StringUtility.find(s2, s) == 0) {
        int typeOffset = tmpOffset - s.length() + 1;
        int typeLength = 0;
        while (--typeOffset >= 0
            && (Character.isJavaIdentifierPart(doc.getChar(typeOffset)) || doc.getChar(typeOffset) == '.')) {
          typeLength++;
        }
        String typeName = doc.get(typeOffset + 1, typeLength);

        int qNameOffset = doc.get().indexOf(typeName);
        int qNameLength = 0;
        while (--qNameOffset >= 0 && !Character.isWhitespace(doc.getChar(qNameOffset))) {
          qNameLength++;
        }

        qName = doc.get(qNameOffset + 1, qNameLength) + typeName;
      }
      if (StringUtility.hasText(qName)) {
        project = m_projectMap.get(qName);
        if (project == null) {
          SearchEngine searchEngine = new SearchEngine();
          final List<IType> matchList = new ArrayList<IType>();
          try {
            searchEngine.search(
                SearchPattern.createPattern(
                    qName,
                    IJavaSearchConstants.TYPE,
                    IJavaSearchConstants.DECLARATIONS,
                    SearchPattern.R_EXACT_MATCH),
                new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
                SearchEngine.createWorkspaceScope(),
                new SearchRequestor() {
                  @Override
                  public void acceptSearchMatch(SearchMatch match) throws CoreException {
                    if (match instanceof TypeDeclarationMatch) {
                      TypeDeclarationMatch typeMatch = (TypeDeclarationMatch) match;
                      IType t = (IType) typeMatch.getElement();
                      matchList.add(t);
                    }
                  }
                }, null);
            if (matchList.size() > 0) {
              IType type = matchList.get(0);
              project = type.getResource().getProject();
              m_projectMap.put(qName, project);
            }

          }
          catch (CoreException e) {
            NlsCore.logWarning(e);
          }
        }
      }
    }
    catch (BadLocationException e) {
      NlsCore.logWarning(e);
    }
    try {
      if (project != null) {
        return NlsCore.getNlsWorkspace().findNlsProject(project, new NullProgressMonitor());
      }
    }
    catch (CoreException e) {
      NlsCore.logWarning(e);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
    try {
      if (!isNlsLocation(viewer, offset)) {
        return NO_PROPOSALS;
      }
      String prefix = getPrefix(viewer, offset);
      INlsProject nlsProject = searchNlsProject(viewer, offset);
      if (nlsProject == null) {
        return NO_PROPOSALS;
      }
      List<INlsEntry> suggestions = createSuggestionsFromNlsProject(nlsProject, prefix);

      List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
      for (INlsEntry e : suggestions) {
        result.add(new Proposal(e, prefix, offset, m_image));
      }
      return result.toArray(new ICompletionProposal[result.size()]);

    }
    catch (BadLocationException x) {
      // ignore and return no proposals
      return NO_PROPOSALS;
    }
  }

  private boolean isNlsLocation(ITextViewer viewer, int offset) throws BadLocationException {
    IDocument doc = viewer.getDocument();
    if (doc == null || offset > doc.getLength()) {
      return false;
    }

    int length = 0;
    while (--offset >= 0 && Character.isLetterOrDigit(doc.getChar(offset))) {
      length++;
    }
    String s = ".get(\"";
    String s2 = doc.get(offset - s.length() + 1, s.length());
    if (StringUtility.find(s2, s) == 0) {
      return true;
    }
    return false;
  }

  private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException {
    IDocument doc = viewer.getDocument();
    if (doc == null || offset > doc.getLength()) {
      return null;
    }

    int length = 0;
    while (--offset >= 0 && Character.isLetterOrDigit(doc.getChar(offset))) {
      length++;
    }

    return doc.get(offset + 1, length);
  }

  /**
   * Return the list of suggestions from the current document. First the document is searched backwards from the caret
   * position and then forwards.
   *
   * @param nlsProject
   *          the nls project for the current document
   * @param prefix
   *          the completion prefix
   * @return all possible completions that were found in the current document
   * @throws BadLocationException
   *           if accessing the document fails
   */
  private ArrayList<INlsEntry> createSuggestionsFromNlsProject(INlsProject nlsProject, String prefix) throws BadLocationException {
    ArrayList<INlsEntry> completions = new ArrayList<INlsEntry>();

    INlsEntry[] entries = nlsProject.getEntries(prefix, false);
    for (INlsEntry entry : entries) {
      try {
        completions.add(entry);
      }
      catch (Exception e) {
        NlsCore.logWarning(e);
      }
    }
    return completions;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
   */
  public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
    // no context informations for hippie completions
    return NO_CONTEXTS;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
   */
  public char[] getCompletionProposalAutoActivationCharacters() {
    return null;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
   */
  public char[] getContextInformationAutoActivationCharacters() {
    return null;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
   */
  public IContextInformationValidator getContextInformationValidator() {
    return null;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
   */
  public String getErrorMessage() {
    return null; // no custom error message
  }
}
